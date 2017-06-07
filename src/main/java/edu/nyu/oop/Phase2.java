package edu.nyu.oop;

// xtc imports
import org.slf4j.Logger;
import xtc.tree.GNode;

// Utility imports
import edu.nyu.oop.util.StateCollectors.*;
import edu.nyu.oop.util.MutateUtil;

// General imports
import java.util.*;

class Phase2 {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(Phase2.class);
    private static boolean IS_DEBUG = false; // Set to false when correct

    ////////////////////////////// SBT methods //////////////////////////////
    static void printPhase2Data(GNode n) {
        logger.debug("\n" + generateStringASTData(n));
    } // End of the printPhase2Data method

    static void printPhase2AST(GNode n) {
        logger.debug("\n" + generateStringAST(n));
    } // End of the printPhase2AST method

    ////////////////////////////// String methods //////////////////////////////

    static String generateStringASTData(GNode n) {
        HeaderData data = generateData(n);
        return data.toString();
    } // End of the generateStringASTData method

    static String generateStringAST(GNode n) {
        StringBuilder sb = new StringBuilder();
        List<GNode> phase2ASTs = generateASTs(n);

        for (GNode phase2AST : phase2ASTs)
            sb.append(phase2AST.toString());
        return sb.toString();
    } // End of the generateStringAST method

    ////////////////////////////// Generate methods (public) //////////////////////////////

    // Called in phase 4
    static GNode getMainClassNode(GNode n) {
        CPPHeaderVisitor visitor = new CPPHeaderVisitor();
        HeaderData data = visitor.getData(MangleAST.generateMangledASTs(n));

        //TODO This is why printing output with sbt command shows double
        logger.debug(data.getMainClass().toString());
        return data.getMainClass();
    } // End of the getMainMethodNode method

    // Called through the orchestrator
    static GNode getMainClassNode(HeaderData data) {
        return data.getMainClass();
    } // End of the getMainMethodNode method

    // Called through the orchestrator
    static HeaderData generateData(List<GNode> mangledASTs) {
        CPPHeaderVisitor visitor = new CPPHeaderVisitor();
        return visitor.getData(mangledASTs);
    } // End of the generateData method

    // Required for string method
    static HeaderData generateData(GNode n) {
        List<GNode> mangledASTs = MangleAST.generateMangledASTs(n);
        CPPHeaderVisitor visitor = new CPPHeaderVisitor();
        return visitor.getData(mangledASTs);
    } // End of the generateData method

    // Called through the orchestrator
    static List<GNode> generateASTs(List<GNode> mangledASTs) {
        CPPHeaderVisitor visitor = new CPPHeaderVisitor();
        return visitor.getNodes(mangledASTs);
    } // End of the generateASTs method

    // Required for string method
    static List<GNode> generateASTs(GNode n) {
        List<GNode> mangledASTs = MangleAST.generateMangledASTs(n);
        CPPHeaderVisitor visitor = new CPPHeaderVisitor();
        return visitor.getNodes(mangledASTs);
    } // End of the generateASTs method

    ////////////////////////////// Generate methods (private) //////////////////////////////

    // Builds the C++ header AST (driver)
    static List<GNode> generateASTs(HeaderData data) {
        List<GNode> asts = new ArrayList<>();

        for (CPPClass cppClass : data.getClasses())
            asts.add(generateCPPAST(cppClass, data));

        // Sets the ast's parents to their correct nodes
        setClassParents(asts, data.getClasses());


        // Builds a GNode -> CPPClass mapping
        Map<GNode, CPPClass> getClassObject = new HashMap<>();
        for (GNode ast : asts) {
            String className = ast.getString(0);
            CPPClass current = null;

            for (CPPClass cppClass : data.getClasses()) {
                if (cppClass.getName().equals(className)) {
                    current = cppClass;
                    break;
                }
            }
            getClassObject.put(ast, current);
        }

        // Adds the super class non-static and non-private fields to the ast
        if (IS_DEBUG)
            logger.debug("\t--------------------");
        for (GNode ast : asts)
            addSuperFieldsToDataLayout(ast, getClassObject.get(ast));

        // Adds the super __init call to all children classes
        if (IS_DEBUG)
            logger.debug("\t--------------------");
        for (GNode ast : asts)
            addSuperInitCall(ast, getClassObject.get(ast));

        return asts;
    } // End of the generateASTs method

    private static void addSuperInitCall(GNode ast, CPPClass cppClass) {
        // Class AST children layout
        // Node 0: Class name (String!)
        // Node 1: Package node
        // Node 2: DataLayout node
        // Node 3: VTable node
        // Node 4: Parent node
        // Node 5: Implementation node

        GNode parentNode = ast.getGeneric(4);
        if (parentNode.getGeneric(0) == null) {
            if (IS_DEBUG)
                logger.debug("\tSkipping the addition of the super __init call in class: " + ast.getString(0));
        }
        else {
            if (IS_DEBUG)
                logger.debug("\tAdding super __init call in class: " + ast.getString(0));

            GNode implementation = ast.getGeneric(5);
            Map<Integer, GNode> initMethods = getInitMethodsFromImplementation(implementation);

            // Implementation::Method node children layout
            // Node 0: Method name (String!)
            // Node 1: Method Return Type node
            // Node 2: Method Modifiers node (no children here!)
            // Node 3: Method Parameters node
            // Node 4: Method Source node
            // Node 5: Method Block node

            // We want to add:
            // Node 0: ExpressionStatement
                // Node 0.0: CallExpression
                    // Node 0.0.0: SelectionExpression
                        // Node 0.0.0.0: PrimaryIdentifier
                            // Node 0.0.0.0.0: parent class name (String!)
                        // Node 0.0.0.1: null
                    // Node 0.0.1: null
                    // Node 0.0.2: "__init" (String!)
                    // Node 0.0.3: Arguments node
                        // Node 0.0.3.0: PrimaryIdentifier
                            // Node 0.0.3.0.0: "__this" (String!)

            for (Map.Entry<Integer, GNode> initEntry: initMethods.entrySet()) {
                int replaceIndex = initEntry.getKey();
                GNode initMethod = initEntry.getValue();

                GNode block = initMethod.getGeneric(5);
                if (!block.hasVariable())
                    block = GNode.ensureVariable(block);

                GNode expressionStatement = GNode.create("ExpressionStatement");
                GNode callExpression = GNode.create("CallExpression");

                // Adds the selectionExpression
                GNode selectionExpression = GNode.create("SelectionExpression");
                GNode primaryIdentifier = GNode.create("PrimaryIdentifier");
                primaryIdentifier.add("__" + parentNode.getGeneric(0).getString(0));
                selectionExpression.add(0, primaryIdentifier);
                selectionExpression.add(1, null);
                callExpression.add(0, selectionExpression);

                // Adds the null
                callExpression.add(1, null);

                // Adds the expression name (__init)
                callExpression.add(2, "__init");

                // Adds the arguments node (just adds the __this argument)
                GNode arguments = GNode.create("Arguments");

                GNode argumentsPrimaryIdentifier = GNode.create("PrimaryIdentifier");
                argumentsPrimaryIdentifier.add("__this");
                arguments.add(0, argumentsPrimaryIdentifier);

                callExpression.add(3, arguments);


                expressionStatement.add(0, callExpression);
                // We want to insert this __init call to the front
                block.add(0, expressionStatement);

                initMethod.set(5, block);
            }
        }
    } // End of the addSuperInitCall method

    private static Map<Integer, GNode> getInitMethodsFromImplementation(GNode implementation) {
        // Implementation::Method node children layout
        // Node 0: Method name (String!)
        // Node 1: Method Return Type node
        // Node 2: Method Modifiers node (no children here!)
        // Node 3: Method Parameters node
        // Node 4: Method Source node
        // Node 5: Method Block node (no children here!)
        Map<Integer, GNode> initMethods = new HashMap<>();

        for (int i = 0; i < implementation.size(); ++i) {
            if (implementation.getGeneric(i).getName().equals("MethodDeclaration") && implementation.getGeneric(i).getString(0).equals("__init"))
                initMethods.put(i, implementation.getGeneric(i));
        }
        return initMethods;
    } // End of the getInitMethods method

    private static void addSuperFieldsToDataLayout(GNode ast, CPPClass cppClass) {
        // Class AST children layout
        // Node 0: Class name (String!)
        // Node 1: Package node
        // Node 2: DataLayout node
        // Node 3: VTable node
        // Node 4: Parent node
        // Node 5: Implementation node

        // Datalayout node children layout
        // Node 0: class name (String!)
        // Node 1: vtable pointer field node
        // Node 2+: super class public & non-static fields <---------------- this is what we're inserting.
        // Node 2++: class fields
        // Node 2+++: class constructors
        // Node 2++++: class __init methods
        // Node 2+++++: class non-init methods

        if (IS_DEBUG)
            logger.debug("\tAdding super fields to data layout for class: " + ast.getString(0));

        // NOTE: We don't have to go through each super class, just the direct parent,
        // since that'll contain ancestor ones as well
        GNode parentClass = ast.getGeneric(4).getGeneric(0);
        GNode dataLayout = ast.getGeneric(2);

        // We now get the list of non-private class field declarations
        List<GNode> superFieldNodes = new ArrayList<>();

        if (parentClass != null) {
            GNode parentDatalayoutNode = parentClass.getGeneric(2);
            for (int i = 2; i < parentDatalayoutNode.size(); ++i) {
                GNode parentDatalayoutChild = parentDatalayoutNode.getGeneric(i);
                String datalayoutKind = parentDatalayoutChild.getName();

                if (isValidFieldForInheritance(parentDatalayoutChild))
                    superFieldNodes.add(parentDatalayoutChild);
            }
        }

        // Since we want to retain order, we insert backwards into index 2
        Collections.reverse(superFieldNodes);
        for (GNode field : superFieldNodes) {
            if (IS_DEBUG)
                logger.debug("\tAdding field: " + field.getString(0) + " from super class: " + parentClass.getString(0) + " to: " + ast.getString(0));
            dataLayout.add(2, field);
        }
    } // End of the addSuperFieldsToDataLayout method

    private static boolean isValidFieldForInheritance(GNode field) {
        // Field children layout
        // Node 0: field name (String!)
        // Node 1: field type node
        // Node 2: field modifiers
        GNode modifiers = field.getGeneric(2);
        return field.getName().equals("FieldDeclaration") && !field.getString(0).equals("__vtable")
                && !modifiers.toString().contains("private") && !modifiers.toString().contains("static");
    } // End of the isValidFieldForInheritance method

    private static void setClassParents(List<GNode> asts, List<CPPClass> cppClasses) {
        // Class AST children layout
        // Node 0: Class name (String!)
        // Node 1: Package node
        // Node 2: DataLayout node
        // Node 3: VTable node
        // Node 4: Parent node
        // Node 5: Implementation node

        // Builds a cpp class -> node mapping
        Map<GNode, CPPClass> getClassObject = new HashMap<>();
        Map<String, GNode> getClassNode = new HashMap<>();
        for (CPPClass cppClass : cppClasses) {
            String className = cppClass.getName();
            GNode classNode = null;

            for (GNode ast : asts) {
                if (ast.getString(0).equals(className)) {
                    classNode = ast;
                    break;
                }
            }
            getClassObject.put(classNode, cppClass);
            getClassNode.put(className, classNode);
        }

        // Adds each parent node as applicable (if parent is java.lang.Object, just sets it to null)
        for (GNode ast : asts) {
            CPPClass cppClass = getClassObject.get(ast);
            String parentName = cppClass.getParentName();

            if (parentName.equals("java.lang.Object"))
                continue;

            GNode parentNode = getClassNode.get(parentName);
            ast.getGeneric(4).set(0, parentNode);
        }
    } // End of the setClassParents method

    private static GNode generateCPPAST(CPPClass cppClass, HeaderData data) {
        if (IS_DEBUG)
            logger.debug("-------------------- Generating pre-mutated C++ AST for: " + cppClass.getName() + " --------------------");

        // Class AST children layout
        // Node 0: Class name (String!)
        // Node 1: Package node
        // Node 2: DataLayout node
        // Node 3: VTable node
        // Node 4: Parent node
        // Node 5: Implementation node

        // Creates the class root
        GNode classRootNode = GNode.create("Class");

        // Adds the name
        classRootNode.add(0, cppClass.getName());

        // Adds the package name
        GNode packageNameNode = GNode.create("Package");
        packageNameNode.add(0, cppClass.getPackageName());
        classRootNode.add(1, packageNameNode);

        // Adds the DataLayout
        GNode dataLayout = GNode.create("DataLayout");
        addToDataLayout(dataLayout, cppClass);
        classRootNode.add(2, dataLayout);

        // Adds the VTable
        GNode vtable = GNode.create("VTable");
        addToVTable(vtable, cppClass, data);
        classRootNode.add(3, vtable);

        // Adds the parent node (initialised to null)
        GNode parentNameNode = GNode.create("Parent");
        parentNameNode.add(0, cppClass.getParent());
        classRootNode.add(4, parentNameNode);

        // Adds the implementation
        GNode implementationNode = GNode.create("Implementation");
        addToImplementation(implementationNode, cppClass);
        classRootNode.add(5, implementationNode);

        if (IS_DEBUG)
            logger.debug("\tClass AST:\n\n" + classRootNode.toString() + "\n");

        return classRootNode;
    } // End of the generateCPPAST method

    private static void addToDataLayout(GNode dataLayout, CPPClass cppClass) {
        // Datalayout node children layout
        // Node 0: class name (String!)
        // Node 1: vtable pointer field node
        // Node 2+: class fields
        // Node 2++: class constructors
        // Node 2+++: class __init methods
        // Node 2++++: class non-init methods

        // Adds the datalayout name
        dataLayout.add(0, "__" + cppClass.getName());

        // Adds the vtable pointer (counts as a field)
        GNode vtablePointer = GNode.create("FieldDeclaration");

        // Adds the name
        vtablePointer.add(0, "__vptr");

        // Adds the type
        vtablePointer.add(1, MutateUtil.getTypeNodeFromType(new Type("__" + cppClass.getName() + "_VT*", null)));

        // Adds the modifiers
        vtablePointer.add(2, GNode.create("Modifiers"));
        dataLayout.add(1, vtablePointer);

        // Adds any class fields
        for (Field field : cppClass.getFields())
            dataLayout.add(MutateUtil.getFieldNodeFromFieldClass(field));

        // Adds in class constructor(s)
        for (Constructor constructor : cppClass.getConstructors())
            addConstructorToDatalayout(dataLayout, constructor, cppClass.getName());

        // Adds in class init methods
        for (Method initMethod : cppClass.getInitMethods())
            addMethodToDatalayout(dataLayout, initMethod, cppClass.getName());

        // Adds in class non-init method(s)
        for (Method method : cppClass.getNonInitMethods())
            addMethodToDatalayout(dataLayout, method, cppClass.getName());

        // Adds the class representation
        GNode classRepresentation = GNode.create("MethodDeclaration");
        // Adds the method name
        classRepresentation.add("__class");

        // Adds the return type (class)
        classRepresentation.add(MutateUtil.getReturnTypeNodeFromReturnType(new Type("Class", null)));

        // Adds the modifiers
        GNode classRepresentationModifiers = GNode.create("Modifiers");
        classRepresentationModifiers.add("static");
        classRepresentation.add(classRepresentationModifiers);

        // Adds the parameters
        classRepresentation.add(GNode.create("Parameters"));
        dataLayout.add(classRepresentation);

        // Adds the vtable for the class
        GNode vtable = GNode.create("FieldDeclaration");
        // Adds the vtable name
        vtable.add(0, "__vtable");

        // Adds the vtable type
        vtable.add(1, MutateUtil.getTypeNodeFromType(new Type("__" + cppClass.getName() + "_VT", null)));

        // Adds the modifiers
        GNode vtableModifiers = GNode.create("Modifiers");
        vtableModifiers.add("static");
        vtable.add(2, vtableModifiers);

        // Adds the parameters
        vtable.add(3, GNode.create("Parameters"));

        dataLayout.add(vtable);
    } // End of the addToDataLayout method

    private static void addMethodToDatalayout(GNode dataLayout, Method method, String className) {
        GNode methodNode = GNode.create("MethodDeclaration");

        // Sets the method name
        methodNode.add(method.getName());

        // Sets the method return type
        methodNode.add(MutateUtil.getReturnTypeNodeFromReturnType(method.getReturnType()));

        // Sets the method modifiers
        GNode modifiersNode = GNode.create("Modifiers");

        // Add public/private modifiers before static
        List<String> modifierList = method.getModifiers();

        for (int i = 0; i < modifierList.size(); i++)
            modifiersNode.add(modifierList.get(i));

        boolean staticFound = false;
        for (int i = 0; i < modifiersNode.size(); i++) {
            staticFound = modifiersNode.get(i).toString().equals("static");

            if (staticFound)
                break;
        }

        if (!staticFound)
            modifiersNode.add("static");

        methodNode.add(modifiersNode);

        // Sets the method parameters
        GNode parametersNode = GNode.create("Parameters");

        // Adds the self parameter
        GNode selfParameterNode = GNode.create("Parameter");

        // Adds the parameter name
        selfParameterNode.add(className.toLowerCase());

        // Adds the self parameter
        GNode selfParameterTypeNode = MutateUtil.getTypeNodeFromType(new Type(className, null));
        selfParameterNode.add(selfParameterTypeNode);
        parametersNode.add(selfParameterNode);

        // Adds all of the other parameters to the node
        for (Parameter parameter : method.getParameters())
            parametersNode.add(MutateUtil.getParameterNodeFromParameter(parameter));
        methodNode.add(parametersNode);

        // Sets the method source
        GNode sourceNode = GNode.create("Source");
        sourceNode.add(method.getSource());
        methodNode.add(sourceNode);

        // Adds all of this to the methods node
        dataLayout.add(methodNode);

    } // End of the addMethodToDatalayout method


    private static void addConstructorToDatalayout(GNode dataLayout, Constructor constructor, String className) {
        GNode constructorNode = GNode.create("ConstructorDeclaration");

        // Sets the C++ constructor name
        constructorNode.add("__" + constructor.getName());

        // Sets the constructor modifiers
        GNode modifiersNode = GNode.create("Modifiers");

        modifiersNode.add("public");

        constructorNode.add(modifiersNode);

        // Sets the constructor parameters

        // Sets the method parameters
        GNode parametersNode = GNode.create("Parameters");

        // Adds the self parameter
        GNode selfParameterNode = GNode.create("Parameter");
        selfParameterNode.add(className.toLowerCase()); // self parameter name

        // Adds the self parameter type
        selfParameterNode.add(MutateUtil.getTypeNodeFromType(new Type(className, null)));

        // Adds the self parameter node to the parameters node
        parametersNode.add(selfParameterNode);

        // Adds the rest of the parameters
        for (Parameter parameter : constructor.getParameters())
            parametersNode.add(MutateUtil.getParameterNodeFromParameter(parameter));

        // Adds the parameters node to the constructor
        constructorNode.add(parametersNode);

        // Sets the constructor source
        GNode sourceNode = GNode.create("Source");
        sourceNode.add(constructor.getSource());
        constructorNode.add(sourceNode);

        // Adds all of this to the methods node
        dataLayout.add(constructorNode);
    } // End of the addConstructor method

    ////////////////////////////// Helper methods (private) //////////////////////////////

    private static GNode getDynamicTypeToVTable() {
        // Field node children layout
        // Node 0: Field name (String!)
        // Node 1: Field type
        // Node 2: Field Modifiers

        GNode dynamicType = GNode.create("FieldDeclaration");

        // Dynamic type name
        dynamicType.add("__isa");

        // Dynamic type type node
        GNode dynamicTypeNode = GNode.create("Type");
        GNode dynamicTypeTypeNode = GNode.create("QualifiedIdentifier");
        dynamicTypeTypeNode.add("Class");
        dynamicTypeNode.add(dynamicTypeTypeNode); // Type
        dynamicTypeNode.add(""); // Dimension
        dynamicType.add(dynamicTypeNode);

        // Dynamic type modifiers
        GNode modifiers = GNode.create("Modifiers");
        dynamicType.add(modifiers);

        return dynamicType;
    } // End of the getDynamicTypeToVTable method

    private static GNode getMethodNodeFromMethodToVTable(Method methodObject, String className) {
        if (IS_DEBUG)
            logger.debug("\tAdding " + methodObject.getName() + " method");

        // Method node children layout
        // Node 0: Method name (String!)
        // Node 1: Method Return Type node
        // Node 2: Method Modifiers node (no children here!)
        // Node 3: Method Parameters node
        // Node 4: Method Source node
        // Node 5: Method Block node (no children here!)

        GNode newMethodNode = GNode.create("MethodDeclaration");

        // Adds the method name
        newMethodNode.add(0, methodObject.getName());

        // Adds the method return type
        newMethodNode.add(1, MutateUtil.getReturnTypeNodeFromReturnType(methodObject.getReturnType()));

        // Adds the method modifiers node (empty children)
        GNode newMethodModifiersNode = GNode.create("Modifiers");
        newMethodNode.add(2, newMethodModifiersNode);

        // Sets the method parameters
        GNode newMethodParametersNode = GNode.create("Parameters");

        // Adds the self parameter
        newMethodParametersNode.add(MutateUtil.getParameterNodeFromParameter(new Parameter("__this", new Type(className, null), new ArrayList<String>())));

        // Adds the other parameters
        for (Parameter methodParameter : methodObject.getParameters())
            newMethodParametersNode.add(MutateUtil.getParameterNodeFromParameter(methodParameter));

        newMethodNode.add(3, newMethodParametersNode);

        // Adds the method source
        GNode newMethodSourceNode = GNode.create("Source");
        newMethodSourceNode.add(methodObject.getSource());
        newMethodNode.add(4, newMethodSourceNode);

        // Adds the method block node (empty children)
        GNode newMethodBlock = GNode.create("Block");
        newMethodNode.add(5, newMethodBlock);

        // Returns the toString node
        return newMethodNode;
    } // End of the addMethodToVTable method


    ////////////////////////////// Base AST node generation methods (private) //////////////////////////////

    private static GNode getDeleteNodeToVTable(CPPClass cppClass, LinkedHashMap<String, String> getSource) {
        if (IS_DEBUG)
            logger.debug("\tAdding delete method");

        // Method node children layout
        // Node 0: Method name (String!)
        // Node 1: Method Return Type node
        // Node 2: Method Modifiers node (no children here!)
        // Node 3: Method Parameters node
        // Node 4: Method Source node
        // Node 5: Method Block node (no children here!)

        GNode hashcode = GNode.create("MethodDeclaration");

        // Adds the method name
        hashcode.add(0, "__delete");

        // Adds the method return type
        hashcode.add(1, MutateUtil.getReturnTypeNodeFromReturnType(new Type("void", null)));

        // Adds the method modifiers node (empty children)
        GNode hashcodeModifiersNode = GNode.create("Modifiers");
        hashcode.add(2, hashcodeModifiersNode);

        // Adds the method parameters (just the self parameter)
        GNode hashcodeParametersNode = GNode.create("Parameters");
        hashcodeParametersNode.add(MutateUtil.getParameterNodeFromParameter(new Parameter("__this", new Type("__" + cppClass.getName(), null), new ArrayList<String>())));
        hashcode.add(3, hashcodeParametersNode);

        // Adds the method source
        GNode hashcodeSource = GNode.create("Source");
        hashcodeSource.add(getSource.get("hashCode"));
        hashcode.add(4, hashcodeSource);

        // Adds the method block node (empty children)
        GNode hashcodeBlock = GNode.create("Block");
        hashcode.add(5, hashcodeBlock);

        // Returns the hashcode node
        return hashcode;
    } // End of the getHashCodeNodeToVTable method

    private static GNode getHashCodeNodeToVTable(CPPClass cppClass, LinkedHashMap<String, String> getSource) {
        if (IS_DEBUG)
            logger.debug("\tAdding hashcode method");

        // Method node children layout
        // Node 0: Method name (String!)
        // Node 1: Method Return Type node
        // Node 2: Method Modifiers node (no children here!)
        // Node 3: Method Parameters node
        // Node 4: Method Source node
        // Node 5: Method Block node (no children here!)

        GNode hashcode = GNode.create("MethodDeclaration");

        // Adds the method name
        hashcode.add(0, "hashCode");

        // Adds the method return type
        hashcode.add(1, MutateUtil.getReturnTypeNodeFromReturnType(new Type("int32_t", null)));

        // Adds the method modifiers node (empty children)
        GNode hashcodeModifiersNode = GNode.create("Modifiers");
        hashcode.add(2, hashcodeModifiersNode);

        // Adds the method parameters (just the self parameter)
        GNode hashcodeParametersNode = GNode.create("Parameters");
        hashcodeParametersNode.add(MutateUtil.getParameterNodeFromParameter(new Parameter("__this", new Type(cppClass.getName(), null), new ArrayList<String>())));
        hashcode.add(3, hashcodeParametersNode);

        // Adds the method source
        GNode hashcodeSource = GNode.create("Source");
        hashcodeSource.add(getSource.get("hashCode"));
        hashcode.add(4, hashcodeSource);

        // Adds the method block node (empty children)
        GNode hashcodeBlock = GNode.create("Block");
        hashcode.add(5, hashcodeBlock);

        // Returns the hashcode node
        return hashcode;
    } // End of the getHashCodeNodeToVTable method

    private static GNode getEqualsNodeToVTable(CPPClass cppClass, LinkedHashMap<String, String> getSource) {
        if (IS_DEBUG)
            logger.debug("\tAdding equals method");

        // Method node children layout
        // Node 0: Method name (String!)
        // Node 1: Method Return Type node
        // Node 2: Method Modifiers node (no children here!)
        // Node 3: Method Parameters node
        // Node 4: Method Source node
        // Node 5: Method Block node (no children here!)

        GNode equals = GNode.create("MethodDeclaration");

        // Adds the method name
        equals.add(0, "equals");

        // Adds the method return type
        equals.add(1, MutateUtil.getReturnTypeNodeFromReturnType(new Type("bool", null)));

        // Adds the method modifiers node (empty children)
        GNode equalsModifiersNode = GNode.create("Modifiers");
        equals.add(2, equalsModifiersNode);

        // Adds the method parameters
        GNode equalsParametersNode = GNode.create("Parameters");
        equalsParametersNode.add(MutateUtil.getParameterNodeFromParameter(new Parameter("__this", new Type(cppClass.getName(), null), new ArrayList<String>())));
        equalsParametersNode.add(MutateUtil.getParameterNodeFromParameter(new Parameter("o", new Type("Object", null), new ArrayList<String>())));
        equals.add(3, equalsParametersNode);

        // Adds the method source
        GNode equalsSource = GNode.create("Source");
        equalsSource.add(getSource.get("equals"));
        equals.add(4, equalsSource);

        // Adds the method block node (empty children)
        GNode equalsBlock = GNode.create("Block");
        equals.add(5, equalsBlock);

        // Returns the equals node
        return equals;
    } // End of the getEqualsNodeToVTable method

    private static GNode getGetClassNodeToVTable(CPPClass cppClass, LinkedHashMap<String, String> getSource) {
        if (IS_DEBUG)
            logger.debug("\tAdding getClass method");

        // Method node children layout
        // Node 0: Method name (String!)
        // Node 1: Method Return Type node
        // Node 2: Method Modifiers node (no children here!)
        // Node 3: Method Parameters node
        // Node 4: Method Source node
        // Node 5: Method Block node (no children here!)

        GNode getClass = GNode.create("MethodDeclaration");

        // Adds the method name
        getClass.add(0, "getClass");

        // Adds the method return type
        getClass.add(1, MutateUtil.getReturnTypeNodeFromReturnType(new Type("Class", null)));

        // Adds the method modifiers node (empty children)
        GNode getClassModifiersNode = GNode.create("Modifiers");
        getClass.add(2, getClassModifiersNode);

        // Adds the method parameters
        GNode getClassParametersNode = GNode.create("Parameters");
        getClassParametersNode.add(MutateUtil.getParameterNodeFromParameter(new Parameter("__this", new Type(cppClass.getName(), null), new ArrayList<String>())));
        getClass.add(3, getClassParametersNode);

        // Adds the method source
        GNode getClassSource = GNode.create("Source");
        getClassSource.add(getSource.get("getClass"));
        getClass.add(4, getClassSource);

        // Adds the method block node (empty children)
        GNode getClassBlock = GNode.create("Block");
        getClass.add(5, getClassBlock);

        // Returns the getClass node
        return getClass;
    } // End of the getGetClassNodeToVTable method

    private static GNode getToStringNodeToVTable(CPPClass cppClass, LinkedHashMap<String, String> getSource) {
        if (IS_DEBUG)
            logger.debug("\tAdding toString method");

        // Method node children layout
        // Node 0: Method name (String!)
        // Node 1: Method Return Type node
        // Node 2: Method Modifiers node (no children here!)
        // Node 3: Method Parameters node
        // Node 4: Method Source node
        // Node 5: Method Block node (no children here!)

        GNode toString = GNode.create("MethodDeclaration");

        // Adds the method name
        toString.add(0, "toString");

        // Adds the method return type
        toString.add(1, MutateUtil.getReturnTypeNodeFromReturnType(new Type("String", null)));

        // Adds the method modifiers node (empty children)
        GNode toStringModifiersNode = GNode.create("Modifiers");
        toString.add(2, toStringModifiersNode);

        // Adds the method parameters
        GNode toStringParametersNode = GNode.create("Parameters");
        toStringParametersNode.add(MutateUtil.getParameterNodeFromParameter(new Parameter("__this", new Type(cppClass.getName(), null), new ArrayList<String>())));
        toString.add(3, toStringParametersNode);

        // Adds the method source
        GNode toStringSource = GNode.create("Source");
        toStringSource.add(getSource.get("toString"));
        toString.add(4, toStringSource);

        // Adds the method block node (empty children)
        GNode toStringBlock = GNode.create("Block");
        toString.add(5, toStringBlock);

        // Returns the toString node
        return toString;
    } // End of the getToStringNodeToVTable method

    private static void addToVTable(GNode vtable, CPPClass cppClass, HeaderData headerData) {
        // VTable children layout
        // Node 0: vtable name (String!)
        // Node 1: dyanmic type node
        // Node 2 -> 5: object's methods
        // Node 6+: class methods (non-static)

        // Adds the vtable name
        vtable.add(0, "__" + cppClass.getName() + "_VT");

        // Adds the dynamic type
        vtable.add(1, getDynamicTypeToVTable());

        // Creates a class name -> class object mapping
        Map<String, CPPClass> getClassObject = new HashMap<>();
        for (CPPClass cppClassEntry : headerData.getClasses())
            getClassObject.put(cppClassEntry.getName(), cppClassEntry);

        // Creates a method name -> method object mapping
        Map<String, Method> getMethodObject = new HashMap<>();

        // Creates the list of of over-ridden methods (method name -> method source mapping)
        LinkedHashMap<String, String> getSource = new LinkedHashMap<>(); // We use LHM since it preserves insertion order (but not update order!)
        getSource.put("__delete", "Object");
        getSource.put("hashCode", "Object");
        getSource.put("equals", "Object");
        getSource.put("getClass", "Object");
        getSource.put("toString", "Object");

        if (IS_DEBUG)
            logger.debug("\tStarting dynamic dispatch");

        // We start dynamic dispatch from the most ancestor class, in an "onion-layering technique"
        // This means we get all instance methods from the ancestor, and continually replace
        // with descendant class instance methods, "overlaying" them. Since we use a LHM, it preserves
        // insertion order, even upon updating with an overlay
        List<CPPClass> inheritanceChain = new ArrayList<>();
        for (CPPClass current = cppClass; current != null; current = getClassObject.get(current.getParentName()))
            inheritanceChain.add(current);
        Collections.reverse(inheritanceChain);

        for (CPPClass current : inheritanceChain) {
            if (IS_DEBUG)
                logger.debug("\tDispatching for class: " + current.getName());

            for (Method method : current.getMethods()) {
                if (IS_DEBUG)
                    logger.debug("\tChecking method: " + method.getName());
                if (!method.getName().equals("__init")) {
                    if (IS_DEBUG && getSource.get(method.getName()) == null)
                        logger.debug("\tAdding method: " + method.getName() + " from class: " + method.getSource() + " to the mapping");
                    else if (IS_DEBUG && getSource.get(method.getName()) != null)
                        logger.debug("\tReplacing method: " + method.getName() + " with method from class: " + method.getSource() + " to the mapping");

                    getSource.put(method.getName(), method.getSource());
                    getMethodObject.put(method.getName(), method);
                }
            } // End of iterating through all class methods
        }

        // Adds the default object's method nodes to the vtable
        vtable.add(2, getDeleteNodeToVTable(cppClass, getSource));
        vtable.add(3, getHashCodeNodeToVTable(cppClass, getSource));
        vtable.add(4, getEqualsNodeToVTable(cppClass, getSource));
        vtable.add(5, getGetClassNodeToVTable(cppClass, getSource));
        vtable.add(6, getToStringNodeToVTable(cppClass, getSource));

        // We iterate backwards, in order to preserve method inheritance order (ancestors first)
        List<Map.Entry<String, String>> list = new ArrayList<>(getSource.entrySet());

        for(int i = list.size() - 1; i >= 0 ; --i) {
            Map.Entry<String, String> entry = list.get(i);
            String methodName = entry.getKey();

            // Sanity check to skip the object's methods
            if (methodName.equals("hashCode") || methodName.equals("equals") ||
                    methodName.equals("getClass") || methodName.equals("toString") || methodName.equals("__delete"))
                continue;

            // Adds the method to the vtable if it isn't a static method
            // NOTE: we don't need to specially handle __init methods since they're already static
            Method methodToAdd = getMethodObject.get(methodName);
            if (methodToAdd.getModifiers().toString().contains("static"))
                continue;

            // Adds the method to the vtable
            vtable.add(getMethodNodeFromMethodToVTable(methodToAdd, cppClass.getName()));
        } // End of adding all instance methods, including from ancestor classes
    } // End of the addToVTable method


    ////////////////////////////// Implementation methods //////////////////////////////

    private static void addToImplementation(GNode implementationNode, CPPClass cppClass) {
        // Adds constructor(s) to the implementation (should really only be one after name mangling in 1.5)
        for (Constructor constructor : cppClass.getConstructors())
            implementationNode.add(addConstructorToImplementation(constructor));

        // Adds all methods to the implementation
        for (Method method : cppClass.getMethods())
            implementationNode.add(addMethodToImplementation(method));

        // Adds all class fields to the implementation (to be mutated into __init in the mutator later)
        for (Field field : cppClass.getFields())
            implementationNode.add(addFieldToImplementation(field));
    } // End of the addToImplementation method

    // Adds class field nodes to the implementation
    private static GNode addFieldToImplementation(Field field) {
        // Field children layout
        // Node 0: Modifiers <-- field modifiers
        // Node 1: Type <-- field type
        // Node 2: Declarators <-- field information (e.g. name)

        return field.getNode();
    } // End of the addFieldToImplementation

    private static GNode addConstructorToImplementation(Constructor constructor) {
        // Constructor node children layout
        // Node 0: Constructor name (String!)
        // Node 1: Constructor Modifiers node (no children here!)
        // Node 2: Constructor Parameters node
        // Node 3: Constructor Source node
        // Node 4: Constructor Block node (no children here!)

        GNode constructorNode = GNode.create("ConstructorDeclaration");

        // Adds the constructor name
        constructorNode.add(0, constructor.getName());

        // Adds the constructor modifier(s)
        GNode constructorModifiersNode = GNode.create("Modifiers");
        for (String modifier : constructor.getModifiers())
            constructorModifiersNode.add(modifier);

        constructorNode.add(1, constructorModifiersNode);

        // Adds the constructor parameter(s)
        GNode constructorParametersNode = GNode.create("Parameters");
        for (Parameter parameter : constructor.getParameters())
            constructorParametersNode.add(MutateUtil.getParameterNodeFromParameter(parameter));

        constructorNode.add(2, constructorParametersNode);

        // Adds the constructor source
        GNode constructorSource = GNode.create("Source");
        constructorSource.add(constructor.getName());
        constructorNode.add(3, constructorSource);

        // Adds the implementation block
        constructorNode.add(4, constructor.getBlock());

        // Returns the new constructor node
        return constructorNode;
    } // End of the addConstructorToImplementation method

    private static GNode addMethodToImplementation(Method method) {
        // Method node children layout
        // Node 0: Method name (String!)
        // Node 1: Method Return Type node
        // Node 2: Method Modifiers node (no children here!)
        // Node 3: Method Parameters node
        // Node 4: Method Source node
        // Node 5: Method Block node (no children here!)

        GNode methodNode = GNode.create("MethodDeclaration");

        // Adds the method name
        methodNode.add(0, method.getName());

        // Adds the method return type
        methodNode.add(1, MutateUtil.getReturnTypeNodeFromReturnType(method.getReturnType()));

        // Adds the method modifiers
        GNode methodModifiersNode = GNode.create("Modifiers");
        for (String modifier : method.getModifiers())
            methodModifiersNode.add(modifier);
        methodNode.add(2, methodModifiersNode);

        // Adds the method parameters
        GNode methodParametersNode = GNode.create("Parameters");
        for (Parameter parameter : method.getParameters())
            methodParametersNode.add(MutateUtil.getParameterNodeFromParameter(parameter));
        methodNode.add(3, methodParametersNode);

        // Adds the method source
        methodNode.add(4, method.getSource());

        // Adds the implementation block
        methodNode.add(5, method.getBlock());

        return methodNode;
    } // End of the addMethodToImplementation method
} // End of the Phase2 class
