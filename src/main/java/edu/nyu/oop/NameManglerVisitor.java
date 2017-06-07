package edu.nyu.oop;

// xtc imports
import edu.nyu.oop.util.PrintUtilities;
import edu.nyu.oop.util.SymbolTableUtil;
import org.slf4j.Logger;
import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;
import xtc.type.AliasT;
import xtc.type.Type;
import xtc.type.VariableT;
import xtc.util.SymbolTable.Scope;

// Utility imports
import edu.nyu.oop.util.NodeUtil;
import xtc.util.SymbolTable;

// General imports
import java.util.*;

// NOTE: We deal with overloaded methods and constructor conversion here
// TODO method mangling of arrays in the parameter fails
class NameManglerVisitor extends Visitor {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
    private SymbolTable table;
    private boolean IS_DEBUG = false;

    // Mapping methods
    private Map<String, GNode> mangledMethodMap;
    private Set<String> overloadedSet;

    // Mapping fields
    private Map<String, String> getMangledFieldName;
    private Set<String> mangledFieldSet;

    // Class check
    private Set<String> classSet;

    private ArrayList<String> methodList = new ArrayList<String>();
    private ArrayList<String> mangledfieldList = new ArrayList<String>();

    public void visit(Node n) {
        for (Object o : n) {
            if (o instanceof Node) dispatch((Node) o);
        }
    } // End of the visit method

    /***************************** Helper Methods here ****************************/

    // Used when checking if a class body child is a method or field.
    // Probably has other uses with detecting anonymous class-level scope because magic.
    private boolean hasBlock(GNode n) {
        return NodeUtil.dfs(n, "Block") != null;
    } // End of the hasBlock method

    void mangleAST(GNode n) {
        if (IS_DEBUG)
            logger.debug("******************** START OF PHASE 1.5: Java AST Mangling ********************");

        if (IS_DEBUG)
            logger.debug("\t\tBuilding symbol table");
        // Creates the symbol table first
        table = new ManglerSymbolTableBuilder().getTable(n);
        if (IS_DEBUG)
            logger.debug("\t\tBuilt symbol table");

        super.dispatch(n);

        if (IS_DEBUG)
            logger.debug("******************** END OF PHASE 1.5: Java AST Mangling ********************\n");
    } // End of the getData method

    private void setMapping(GNode n) {
        GNode classBody = n.getGeneric(5);
        // Generates the required mapping and sets
        Map<String, List<GNode>> getMethodNode = new HashMap<>();
        Set<String> methodSet = new HashSet<>();
        Map<String, GNode> getFieldNode = new HashMap<>();
        Set<String> fieldSet = new HashSet<>();

        String className = n.getString(1);

        for (int i = 0; i < classBody.size(); ++i) {
            if (classBody.get(i) instanceof Node) {
                switch (classBody.getGeneric(i).getName()) {
                    case "MethodDeclaration":
                        // Method children layout
                        // Node 0: Modifiers <-- method modifiers parent node
                        // Node 2: Type <-- method return type node
                        // Node 3: "toString" <-- method name (String!)
                        // Node 4: FormalParameters node <-- method parameters parent node
                        // Node 7: Block <-- actual method implementation
                        GNode method = classBody.getGeneric(i);
                        String methodName = method.getString(3);

                        // Adds a new listing if it's the first
                        if (getMethodNode.get(methodName) == null)
                            getMethodNode.put(methodName, new ArrayList<GNode>());

                        // Adds the node to the mapping
                        getMethodNode.get(methodName).add(method);
                        methodSet.add(methodName);
                        break;
                    case "FieldDeclaration":
                        // Field children layout
                        // Node 0: Modifiers node
                        // Node 1: Type node
                        // Node 2: Declarators node
                        GNode field = classBody.getGeneric(i);
                        GNode declarators = field.getGeneric(2);
                        GNode declarator = declarators.getGeneric(0);
                        String fieldName = declarator.getString(0);
                        getFieldNode.put(fieldName, field);
                        fieldSet.add(fieldName);
                        break;
                    case "ConstructorDeclaration":
                        // Don't really need to map constructors since a dfsAll will do the same thing
                        break;
                    default:
                        logger.debug("TODO: Add to NameManglerVisitor::visitClassDeclaration: " + classBody.getGeneric(i).getName());
                }
            }
        }

        mapMethods(getMethodNode, className);
        mapFields(fieldSet, methodSet, className);
    } // End of the setMapping method

    private void mapFields(Set<String> fieldSet, Set<String> methodSet, String className) {
        for (String fieldName : fieldSet) {
            if (methodSet.contains(fieldName)) {
                if (IS_DEBUG)
                    logger.debug("\t\tField: '" + fieldName + "' needs to be mangled");
                getMangledFieldName.put(className + "." + fieldName, fieldName + "FIELDDECLARATION");
                mangledFieldSet.add(className + "." + fieldName);
            }
            else {
                if (IS_DEBUG)
                    logger.debug("\t\tField: '" + fieldName + "' does not need to be mangled");
            }
        }
    } // End of the mangleFields method

    private void mapMethods(Map<String, List<GNode>> getMethodNode, String className) {
        for (Map.Entry<String, List<GNode>> entry : getMethodNode.entrySet()) {
            String methodName = entry.getKey();
            List<GNode> methodNodes = entry.getValue();

            // Deals with overloaded
            if (methodNodes.size() > 1) {
                if (IS_DEBUG)
                    logger.debug("\t\tName-mangling required for method: " + methodName);

                for (GNode methodNode : methodNodes) {
                    String mangledMethodName = mangleMethodName(methodName, methodNode);

                    if (IS_DEBUG)
                        logger.debug("\t\tMangled name is set to: " + mangledMethodName);

                    methodNode.set(3, mangledMethodName);
                    mangledMethodMap.put(className + "." + methodName, methodNode);
                    overloadedSet.add(className + "." + methodName);
                }
            }
            else {
                // No overloaded methods, adds it to the final mangled method list
                mangledMethodMap.put(className + "." + methodName, methodNodes.get(0));
            }
        } // End of name-mangling for all methods
    } // End of the mangleMethods method


    public void visitCompilationUnit(GNode n) {
        if (IS_DEBUG)
            logger.debug("\t\tEntered Compilation Unit");

        setup(n);

        SymbolTableUtil.enterScope(table, n);
        for (int i = 1; i < n.size(); i++) {
            GNode child = n.getGeneric(i);
            dispatch(child);
        }
        SymbolTableUtil.exitScope(table, n);

        if (IS_DEBUG)
            logger.debug("\t\tExited Compilation Unit");

    } // End of the visitCompilationUnit method

    private void setup(GNode n) {
        classSet = new HashSet<>();
        List<Node> classes = NodeUtil.dfsAll(n, "ClassDeclaration");
        for (Node preMangledClass : classes) {
            String className = preMangledClass.getString(1);
            classSet.add(className);
        }
        // Required for methods
        mangledMethodMap = new HashMap<>();
        overloadedSet = new HashSet<>();
        // Checks to see if there are any fields that need to be name-mangled (same name as methods)
        getMangledFieldName = new HashMap<>();
        mangledFieldSet = new HashSet<>();
    } // End of the setup method

    public void visitPackageDeclaration(final GNode n) {
        if (IS_DEBUG)
            logger.debug("\t\tEntered Package Declaration");

        SymbolTableUtil.enterScope(table, n);
        visit(n);
        SymbolTableUtil.exitScope(table, n);

        if (IS_DEBUG)
            logger.debug("\t\tExited Package Declaration");
    } // End of the visitPackageDeclaration

    public void visitClassDeclaration(GNode n) {
        // ClassDeclaration children node
        // Node 0: Modifiers node
        // Node 1: class name (String!)
        // Node 5: ClassBody node
        String className = n.getString(1);

        if (IS_DEBUG)
            logger.debug("\t\t******************** Mangling AST for class: " + className + "'s scope ********************");
        SymbolTableUtil.enterScope(table, n);
        setMapping(n);

        GNode classBody = n.getGeneric(5);
        dispatch(classBody);

        // Converts constructors to methods
        if (!className.startsWith("Test"))
            convertConstructors(n); // TODO check if we still need this

        SymbolTableUtil.exitScope(table, n);

        if (IS_DEBUG)
            logger.debug("\t\t******************** Mangled AST for class: " + className + "'s scope ********************");
    } // End of the visitClassDeclaration method

    public void visitClassBody(GNode n) {
        if (IS_DEBUG)
            logger.debug("\t\t\tEntered ClassBody");

        SymbolTableUtil.enterScope(table, n);
        visit(n);
        SymbolTableUtil.exitScope(table, n);

        if (IS_DEBUG)
            logger.debug("\t\t\tExited ClassBody");
    } // End of the visitClassBody method

    public void visitMethodDeclaration(GNode method) {
        // Method children layout
        // Node 0: Modifiers <-- method modifiers parent node
        // Node 2: Type <-- method return type node
        // Node 3: "toString" <-- method name (String!)
        // Node 4: FormalParameters node <-- method parameters parent node
        // Node 7: Block <-- actual method implementation
        String methodName = method.getString(3);
        methodList.add(methodName);

        if (IS_DEBUG)
            logger.debug("\t\t\t******************** Entered Method: " + methodName + " ********************");

        SymbolTableUtil.enterScope(table, method);

        visit(method);
        SymbolTableUtil.exitScope(table, method);

        if (IS_DEBUG)
            logger.debug("\t\t\t******************** Exited Method: " + methodName + " ********************");

    } // End of the visitMethodDeclaration method

    public void visitConstructorDeclaration(GNode constructor) {
        if (IS_DEBUG)
            logger.debug("\t\t\t******************** Entered Constructor ********************");

        SymbolTableUtil.enterScope(table, constructor);
        visit(constructor);
        SymbolTableUtil.exitScope(table, constructor);

        if (IS_DEBUG)
            logger.debug("\t\t\t******************** Exited Constructor ********************");

    } // End of the visitConstructorDeclaration method

    public void visitFieldDeclaration(GNode field) {
        // Field children layout
        // Node 0: Modifiers node
        // Node 1: Type node
        // Node 2: Declarators node
        GNode declarators = field.getGeneric(2);
        GNode declarator = declarators.getGeneric(0);
        String fieldName = declarator.getString(0);


        if (IS_DEBUG)
            logger.debug("\t\t\t******************** Entered Field: " + fieldName + " ********************");

        Scope parent = table.current().getParent();
        String className = parent.getName();
        // Only alters if this is a class field node

        if (classSet.contains(className) && mangledFieldSet.contains(className + "." + fieldName)) {

            if (IS_DEBUG)
                logger.debug("\t\tMangling class field declaration: '" + fieldName + "' to '" + getMangledFieldName.get(className + "." + fieldName) + "'");
            declarator.set(0, getMangledFieldName.get(className + "." + fieldName));
        }

        else if (methodList.contains(fieldName)) {
            String mangledFieldName = "main" + fieldName;
            declarator.set(0, mangledFieldName);
            mangledfieldList.add(fieldName);
        }

        else {
            // These are method/constructor-block level field declarations, not class field declarations, so
            // no need to mangle them.
        }

        if (IS_DEBUG)
            logger.debug("\t\t\t******************** Exited Field: " + fieldName + " ********************");

    } // End of the visitFieldDeclaration

    public void visitPrimaryIdentifier(GNode identifier) {
        if (IS_DEBUG)
            logger.debug("\t\t\tEntered PrimaryIdentifier");
        String name = identifier.getString(0);

        if (table.current().lookup(name) != null) {
            String className = null;

            Object lookup = table.current().lookup(name);
            if (lookup instanceof AliasT)
                className = PrintUtilities.extractClass((AliasT) lookup);
            else if (lookup instanceof VariableT)
                className = PrintUtilities.extractClass((VariableT) lookup);

            if (mangledFieldSet.contains(className + "." + name)) {
                if (IS_DEBUG)
                    logger.debug("\t\t\t\tMangling identifier from: '" + name + "' to: '" + getMangledFieldName.get(className + "." + name) + "'");
                identifier.set(0, getMangledFieldName.get(className + "." + name));
            }
            else if (mangledfieldList.contains(name)){
                identifier.set(0, "main" + name);
            }
            else {
                if (IS_DEBUG)
                    logger.debug("\t\t\t\tIdentifier: " + name + " does not require mangling");
            }
        }
        else {
            if (IS_DEBUG)
                logger.debug("\t\t\t\tPrimaryIdentifier was not found in scope");
        }

        if (IS_DEBUG)
            logger.debug("\t\t\tExited PrimaryIdentifier");
    } // End of the primary identifier



    public void visitCallExpression(GNode expression) {
        if (IS_DEBUG)
            logger.debug("\t\t\tEntered CallExpression");

        // CallExpression children layout #1: Nested call
        // Node 0: CallExpression node <-- nested call

        // CallExpression children layout #2: instance OR static method call
        // Node 0: PrimaryIdentifier
        // Node 2: method call name (String!)
        // Node 3: Arguments node

        // CallExpression children layout #3: static method call
        // Node 2: method call name (String!)
        // Node 3: Arguments node

        String callName = expression.getString(2);
        Scope parent = table.current().getParent();

        if (expression.get(0) instanceof Node) {
            // Deals with cases #1 and #2
            switch (expression.getGeneric(0).getName()) {
                case "CallExpression":
                    if (IS_DEBUG)
                        logger.debug("\t\tCalling nested method call");
                    dispatch(expression.getGeneric(0));
                    break;
                case "PrimaryIdentifier":
                    String identifier = expression.getGeneric(0).getString(0);
                    if (classSet.contains(identifier)) {
                        if (IS_DEBUG)
                            logger.debug("\t\tCalling static method: " + callName + " from class: " + identifier);
                        // NOTE: No need to deal with mangling since static methods can't be overloaded
                    }
                    else {
                        if (IS_DEBUG)
                            logger.debug("\t\tCalling instance method: " + callName);

                        if (table.current().lookup(identifier) == null) {
                            if (IS_DEBUG)
                                logger.debug("\t\tInstance method is called from a class field variable");
                            // TODO don't think we need to handle this, but placing a mark just in case

                        }
                        else {
                            // identifier was found
                            String className = PrintUtilities.extractClass((Type) table.current().lookup(identifier));

                            // Since partials are included we need to do manual comparison
                            boolean isFoundInSet = false;
                            for (String methodName : mangledMethodMap.keySet()) {
                                if (methodName.equals(className + "." + methodName)) {
                                    isFoundInSet = true;
                                    break;
                                }
                            }

                            // We check here if the method is overloaded
                            if (!isFoundInSet) {
                                if (IS_DEBUG)
                                    logger.debug("\t\tInstance method is not overloaded");
                            }
                            else {
                                if (IS_DEBUG)
                                    logger.debug("\t\tInstance method *is* overloaded, replacing method call '" + callName + "'");
                                GNode arguments = expression.getGeneric(3);

                                String newMethodCall = dynamicDispatchMethodCall(callName, arguments);
                                if (IS_DEBUG)
                                    logger.debug("\t\tMethod '" + callName + "' replaced with: '" + newMethodCall + "'");

                                expression.set(2, newMethodCall);
                            }
                        }
                    }
                    break;
                case "SelectionExpression":
                    if (IS_DEBUG)
                        logger.debug("\t\tCalling selection expression: " + expression.toString());
                    dispatch(expression.getGeneric(0));
                    break;
                default:
                    logger.debug("TODO: Add to NameManglerVisitor::visitCallExpression: " + expression.getGeneric(0).getName());
                    break;
            }
        }
        else {
            // Deals with case #3
            logger.debug("TODO: NameManglerVisitor::visitCallExpression need to deal with null");
        }

        if (IS_DEBUG)
            logger.debug("\t\t\tExited CallExpression");
    } // End of the visitCallExpression


    //TODO This will only match exact arguments, will need to add the dynamic portion later
    //TODO This will only match exact arguments, will need to add the dynamic portion later
    //TODO This will only match exact arguments, will need to add the dynamic portion later
    //TODO This will only match exact arguments, will need to add the dynamic portion later
    //TODO This will only match exact arguments, will need to add the dynamic portion later
    //TODO This will only match exact arguments, will need to add the dynamic portion later
    //TODO This will only match exact arguments, will need to add the dynamic portion later
    //TODO This will only match exact arguments, will need to add the dynamic portion later
    private String dynamicDispatchMethodCall(String callName, GNode arguments) {
        // Here is where the magic happens and we need to figure out the new call name
        StringBuilder methodCall = new StringBuilder();

        // Our formula for mangling method calls is as follows:
        // ORIGINAL_METHOD_NAME + MANGLEDCALL + Arguments
        methodCall.append(callName);
        methodCall.append("MANGLEDMETHOD");

        for (int i = 0; i < arguments.size(); ++i)
            methodCall.append(convertArgument(arguments.getGeneric(i)));

        return methodCall.toString();
    } // End of the dynamicDispatchMethodCall


    private String dimensionConversion(String s) {
        return s == null ? "" : "Array";
    } // End of the dimensionConversion method

    private String typeConversion(String type) {
        return type.substring(0, 1).toUpperCase() + type.substring(1);
    } // End of the typeConversion method

    private String mangleMethodName(String methodName, GNode methodNode) {
        List<Node> parameters = NodeUtil.dfsAll((GNode) methodNode.get(4), "FormalParameter");

        StringBuilder mangledName = new StringBuilder();
        mangledName.append(methodName);

        // Parameter node children layout
        // Node 0: Modifiers node
        // Node 1: Type node
        // Node 2: unknown (null)
        // Node 3: "args" <-- name of the parameter (String!)
        // Node 4: unknown (null)

        for (Node parameter : parameters) {
            GNode parameterTypeNode = (GNode) parameter.get(1);

            // Type node children layout
            // Node 0: QualifiedIdentifier node / primitive node (.get(0) gives the type in String form)
            // Node 1: Dimensions node (.get(0) gives either null or "[" if it's an array)

            // Adds the type name to the mangled name
            GNode typeParentNode = (GNode) parameterTypeNode.get(0);
            mangledName.append(typeConversion((String) typeParentNode.get(0)));

            // Adds the type dimension to the mangled name
            GNode dimensionParentNode = (GNode) parameterTypeNode.get(1);
            if (dimensionParentNode != null)
                mangledName.append(dimensionConversion((String) dimensionParentNode.get(0)));
        } // End of iterating through each parameter

        return mangledName.toString();
    } // End of the mangleMethodName method



    private void convertConstructors(GNode n) {
        if (IS_DEBUG)
            logger.debug("\t\tConverting constructor(s) now");

        String className = (String) n.get(1);
        List<Node> constructorNodes = NodeUtil.dfsAll(n, "ConstructorDeclaration");

        GNode classBody = (GNode) n.get(5);

        // Generates an empty no-argument constructor if none is explicitly created
        if (constructorNodes.size() == 0) {
            if (IS_DEBUG)
                logger.debug("\t\tAdding empty no-argument constructor");

            GNode constructor = GNode.create("ConstructorDeclaration");

            GNode modifiers = GNode.create("Modifiers");
            constructor.add(0, modifiers);
            constructor.add(1, null);
            constructor.add(2, className);

            GNode parameters = GNode.create("FormalParameters");
            constructor.add(3, parameters);
            constructor.add(4, null);

            GNode block = GNode.create("Block");
            constructor.add(5, block);

            if (!classBody.hasVariable())
                classBody = GNode.ensureVariable(classBody); // Converts from a fixed-size to a variable-size

            classBody.add(constructor);
            constructorNodes.add(constructor);
        }

        Map<GNode, GNode> constructorToMethodMapping = new HashMap<>();
        for (Node constructorNode : constructorNodes) {
            if (IS_DEBUG)
                logger.debug("\t\tConverting constructor to method with parameters: " + constructorNode.get(3).toString());
            constructorToMethodMapping.put((GNode) constructorNode, convertConstructorToMethod((GNode) constructorNode, className));
        }

        // Replaces the constructor nodes with the method ones

        if (!classBody.hasVariable())
            classBody = GNode.ensureVariable(classBody); // Converts from a fixed-size to a variable-size

        for (int i = 0; i < classBody.size(); ++i) {
            if (constructorToMethodMapping.containsKey(classBody.getGeneric(i)))
                classBody.set(i, constructorToMethodMapping.get(classBody.getGeneric(i)));
        }

        // Adds the empty constructor that just calls the super
        GNode constructor = GNode.create("ConstructorDeclaration");

        GNode modifiers = GNode.create("Modifiers");
        constructor.add(modifiers);
        constructor.add(null);

        constructor.add(2, className);

        GNode parameters = GNode.create("FormalParameters");
        constructor.add(3, parameters);
        constructor.add(null);

        GNode block = GNode.create("Block");
        constructor.add(block);

        classBody.add(constructor);

        n.set(5, classBody);
    } // End of the mangleConstructors method

    private GNode convertConstructorToMethod(GNode constructorNode, String className) {
        // Constructor children layout
        // Node 0: Modifiers <-- constructor modifiers parent node
        // Node 2: "A" <-- constructor name (String!)
        // Node 3: FormalParameters node <-- constructor parameters parent node
        // Node 5: Block <-- actual method implementation (not needed here)

        // Method children layout
        // Node 0: Modifiers <-- method modifiers parent node
        // Node 2: Type node <-- method return type node
        // Node 3: "toString" <-- method name (String!)
        // Node 4: FormalParameters node <-- method parameters parent node
        // Node 7: Block <-- actual method implementation

        // Since there is no .setName method for GNodes, we create a new one
        GNode methodVersion = GNode.create("MethodDeclaration");
        for (int i = 0; i < 8; ++i)
            methodVersion.add(i, null);

        // We need to map:
        // Node 5 -> Node 7 (constructor implementation)
        methodVersion.set(7, constructorNode.get(5));

        // Node 3 -> Node 4 (constructor parameters)
        methodVersion.set(4, constructorNode.get(3));

        // Node 2 -> Node 3 (method name)
        methodVersion.set(3, "__init");

        // Create new Type node at node 2 (void return type)
        methodVersion.set(2, GNode.create("VoidType"));

        // Add static modifier in modifiers if not there
        GNode modifiersNode = constructorNode.getGeneric(0);
        if (!modifiersNode.hasVariable())
            modifiersNode = GNode.ensureVariable(modifiersNode);

        if (!modifiersNode.toString().contains("static")) {
            GNode staticNode = GNode.create("Modifier");
            staticNode.add("static");
            modifiersNode.add(staticNode);
        }

        methodVersion.set(0, modifiersNode);

        return methodVersion;
    } // End of the convertConstructorToMethod method

    private String convertArgument(GNode argumentNode) {
        switch (argumentNode.getName()) {
            case "IntegerLiteral":
                return "Int";
            case "FloatingPointLiteral":
                return "Float";
            case "BooleanLiteral":
                return "Boolean";
            case "CharacterLiteral":
                return "Character";
            case "StringLiteral":
                return "String";
            case "PrimaryIdentifier":
                // The below expression is the class name after a symbol table lookup
                return PrintUtilities.extractClass((Type) table.current().lookup(argumentNode.getString(0)));
            default:
                logger.debug("TODO: NameMangler::convertArgument should include: " + argumentNode.getName());
                return argumentNode.getName();
        }
    } // End of the convertArgument method

} // End of the NameManglerVisitor
