package edu.nyu.oop;

// xtc imports
import org.slf4j.Logger;
import xtc.Constants;
import xtc.lang.JavaEntities;
import xtc.tree.GNode;
import xtc.tree.Attribute;
import xtc.type.AliasT;
import xtc.type.Type;
import xtc.type.VariableT;
import xtc.util.SymbolTable;

// Utility imports
import edu.nyu.oop.util.RecursiveVisitor;

// General imports
import java.util.ArrayList;
import java.util.List;

class ManglerSymbolTableBuilder extends RecursiveVisitor {
    final private SymbolTable table;
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
    private boolean IS_DEBUG = false;

    ManglerSymbolTableBuilder() {this.table = new SymbolTable();}

    SymbolTable getTable(GNode someClass) {
        build(someClass);
        return table;
    }

    private void build(GNode someClass) {
        // someClass actually includes the Compilation unit

        if (IS_DEBUG)
            logger.debug("******************** Building symbol table ********************");

        this.dispatch(someClass);

        if (IS_DEBUG)
            logger.debug("\t******************** Symbol table built ********************");
    } // End of the setup method

    //////////////////// Visit methods ////////////////////

    public void visitCompilationUnit(GNode n) {
        if (null == n.get(0))
            visitPackageDeclaration(null);
        else
            dispatch(n.getNode(0));

        table.enter(JavaEntities.fileNameToScopeName(n.getLocation().file));
        table.mark(n);

        for (int i = 1; i < n.size(); i++) {
            GNode child = n.getGeneric(i);
            dispatch(child);
        }

        table.setScope(table.root());
    }

    public void visitPackageDeclaration(final GNode n) {
        String canonicalName = null == n ? "" : (String) dispatch(n.getNode(1));
        table.enter(JavaEntities.packageNameToScopeName(canonicalName));
        table.mark(n);
    }

    public void visitClassDeclaration(GNode n) {
        // ClassDeclaration children node
        // Node 0: Modifiers node
        // Node 1: class name (String!)
        // Node 5: ClassBody node
        String className = n.getString(1);
        if (IS_DEBUG)
            logger.debug("\t\t******************** Entering class: " + className + " ********************");

        table.enter(className);
        table.mark(n);
        visit(n);

        table.exit();

        if (IS_DEBUG)
            logger.debug("\t\t******************** Exiting class: " + className + " ********************");
    } // End of the visitClassDeclaration method


    public void visitClassBody(GNode classNode) {
        // The children of a class body are field, method, and/or constructor declarations
        if (IS_DEBUG)
            logger.debug("\t\t******************** Entering class body ********************");

        table.enter("ClassBody");
        table.mark(classNode);
        visit(classNode);

        table.exit();
        if (IS_DEBUG)
            logger.debug("\t\t******************** Exiting class body ********************");
    } // End of the visitClass method

    public void visitMethodDeclaration(GNode method) {
        // Method children layout
        // Node 0: Modifiers node
        // Node 2: Type node
        // Node 3: Method name (String!)
        // Node 4: FormalParameters node
        // Node 7: Block node
        String methodName = method.getString(3);

        if (IS_DEBUG)
            logger.debug("\t\t******************** Entering method: " + methodName + "'s scope ********************");

//        // Adds the method to the symbol table lookup
//        Type dispatched = (Type) dispatch(method.getGeneric(2));
//        Type result = VariableT.newParam(dispatched, methodName);
//
//        // TODO why does this add the final modifier if modifiers exit?
//        if (method.getGeneric(0).size() != 0)
//            result.addAttribute(JavaEntities.nameToModifier("final"));
//
//        // Defines the method if it cannot be found
//        if (table.current().lookupLocally(methodName) == null) {
//            table.current().define(methodName, result);
//            result.scope(table.current().getQualifiedName());
//        }
//        else
//            logger.error("Duplicate method declaration " + methodName, method);
//
//        assert JavaEntities.isParameterT(result);

        table.enter(methodName);
        table.mark(method);
        visit(method);
        table.exit();

        if (IS_DEBUG)
            logger.debug("\t\t******************** Exiting method: " + methodName + "'s scope ********************");
    } // End of the visitMethodDeclaration method

    public void visitConstructorDeclaration(GNode constructor) {
        // Constructor children layout
        // Node 0: Modifiers node
        // Node 2: constructor name (String!)
        // Node 3: FormalParameters node
        // Node 5: Block node
        String constructorName = constructor.getString(2);

        if (IS_DEBUG)
            logger.debug("\t\t******************** Entering constructor: " + constructorName + "'s scope ********************");

        table.enter(constructorName);
        table.mark(constructor);
        visit(constructor);

        table.exit();

        if (IS_DEBUG)
            logger.debug("\t\t******************** Exiting constructor: " + constructorName + "'s scope ********************");
    } // End of the visitConstructorDeclaration method

    public final String visitQualifiedIdentifier(final GNode n) {
        if (IS_DEBUG)
            logger.debug("\t\t\tEntering QualifiedIdentifier");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n.size(); i++) {
            if (sb.length() > 0)
                sb.append(Constants.QUALIFIER);
            sb.append(n.getString(i));
        }

        if (IS_DEBUG)
            logger.debug("\t\t\tExiting QualifiedIdentifier");

        return sb.toString();
    } // End of the visitQualifiedIdentifier method

    public final Type visitVoidType(final GNode n) {
        if (IS_DEBUG)
            logger.debug("\t\t\tEntering VoidType's scope");

        if (IS_DEBUG)
            logger.debug("\t\t\tExiting VoidType's scope");

        return JavaEntities.nameToBaseType("void");
    } // End of the visitVoidType method

    public final Type visitFormalParameter(final GNode n) {
        if (IS_DEBUG)
            logger.debug("\t\t\tEntering FormalParameter");

        // OLD FormalParameter children layout
        // Node 0: Modifiers
        // Node 1: Type
        // Node 3: parameter name (String!)

        // NEW Parameter node children layout
        // Node 0: Parameter name (String!)
        // Node 1: Parameter Type node
        // Node 2: Parameter Modifiers

        String id = n.getString(3);
        Type dispatched = (Type) dispatch(n.getGeneric(1));
        Type result = VariableT.newParam(dispatched, id);

        // Checks the modifiers list
        // TODO why does this add the final modifier if modifiers exit?
        if (n.getGeneric(0).size() != 0)
            result.addAttribute(JavaEntities.nameToModifier("final"));

        // Defines if it cannot be found
        if (table.current().lookupLocally(id) == null) {
            table.current().define(id, result);
            result.scope(table.current().getQualifiedName());
        }
        else
            logger.error("Duplicate parameter declaration " + id, n);

        // Checks that it is now defined in the symbol table
        assert JavaEntities.isParameterT(result);

        if (IS_DEBUG)
            logger.debug("\t\t\tExiting FormalParameter");

        return result;
    } // End of the visitParameter method

    public void visitBlock(GNode n) {
        if (IS_DEBUG)
            logger.debug("\t\t\tEntering Block");

        table.enter("block");
        table.mark(n);
        visit(n);
        table.exit();

        if (IS_DEBUG)
            logger.debug("\t\t\tExiting Block");
    } // End of the visitBlock method

    public void visitForStatement(GNode n) {
        if (IS_DEBUG)
            logger.debug("\t\t\tEntering For-statement: " + n.toString());

        table.enter(table.freshName("forStatement"));
        table.mark(n);
        visit(n);
        table.exit();

        if (IS_DEBUG)
            logger.debug("\t\t\tExiting For-statement: " + n.toString());
    } // End of the visitForStatement method

    /**
     * Visit a Modifiers = Modifier*.
     */
    public final List<Attribute> visitModifiers(final GNode modifiers) {
        if (IS_DEBUG)
            logger.debug("\t\t\tEntering Modifiers");
        final List<Attribute> result = new ArrayList<Attribute>();

        for (int i = 0; i < modifiers.size(); i++) {
            // Gets the modifier name
            String modifierName = modifiers.getGeneric(i).getString(0);

            final Attribute modifier = JavaEntities.nameToModifier(modifierName);
            if (null == modifier)
                logger.error("unexpected modifier " + modifierName, modifiers);
            else if (result.contains(modifier))
                logger.error("duplicate modifier " + modifierName, modifiers);
            else
                result.add(modifier);
        }
        if (IS_DEBUG)
            logger.debug("\t\t\tExiting Modifiers");
        return result;
    }

    public final List<Type> visitFieldDeclaration(final GNode n) {
        // FieldDeclaration node children layout
        // Node 0: Modifiers
        // Node 1: Type node
        // Node 2: Declarators

        // Adds the field declaration to the symbol table
        GNode declarator = n.getGeneric(2).getGeneric(0);
        String fieldName = declarator.getString(0);

        if (IS_DEBUG)
            logger.debug("\t\t\tEntering FieldDeclaration: " + fieldName);

        @SuppressWarnings("unchecked")
        final List<Attribute> modifiers = (List<Attribute>) dispatch(n.getNode(0));
        Type type = (Type) dispatch(n.getNode(1));



        // Defines the field under the method scope
        table.current().getParent().define(fieldName, type);

        if (IS_DEBUG)
            logger.debug("\t\t\tExiting FieldDeclaration: " + fieldName);

        return processDeclarators(modifiers, type, n.getGeneric(2));
    } // End of the visitFieldDeclaration method

    public final List<Type> processDeclarators(final List<Attribute> modifiers,
                                               final Type type, final GNode declarators) {
        final List<Type> result = new ArrayList<Type>();
        boolean isLocal = JavaEntities.isScopeLocal(table.current().getQualifiedName());
        for (final Object i : declarators) {
            GNode declNode = (GNode) i;
            String name = declNode.getString(0);
            Type dimType = JavaEntities.typeWithDimensions(type,
                    countDimensions(declNode.getGeneric(1)));
            Type entity = isLocal ? VariableT.newLocal(dimType, name) :
                    VariableT.newField(dimType, name);
            for (Attribute mod : modifiers)
                entity.addAttribute(mod);
            if (null == table.current().lookupLocally(name)) {
                result.add(entity);
                table.current().define(name, entity);
                //entity.scope(table.current().getQualifiedName());
            }
        }
        return result;
    } // End of the processDeclarators method

    public static int countDimensions(final GNode dimNode) {
        return null == dimNode ? 0 : dimNode.size();
    } // End of the countDimensions method

    public final Type visitPrimitiveType(final GNode n) {
        if (IS_DEBUG)
            logger.debug("\t\t\tEntering PrimitiveType: " + n.toString());
        final Type result = JavaEntities.nameToBaseType(n.getString(0));

        if (IS_DEBUG)
            logger.debug("\t\t\tExiting PrimitiveType: " + n.toString());

        return result;
    } // End of the visitPrimitiveType method

    public final Type visitType(final GNode n) {
        if (IS_DEBUG)
            logger.debug("\t\t\tEntering Type: " + n.toString());

        final boolean composite = n.getGeneric(0).hasName("QualifiedIdentifier");
        final Object dispatched0 = dispatch(n.getNode(0));
        assert dispatched0 != null;
        final Type componentT = composite ? new AliasT((String) dispatched0) : (Type) dispatched0;
        final int dimensions = countDimensions(n.getGeneric(1));
        final Type result = JavaEntities.typeWithDimensions(componentT, dimensions);

        if (IS_DEBUG)
            logger.debug("\t\t\tExiting Type: " + n.toString());

        return result;
    } // End of the visitType method













    // TODO not used methods
    // TODO not used methods
    // TODO not used methods
    // TODO not used methods
    // TODO not used methods
    // TODO not used methods
    // TODO not used methods
    // TODO not used methods
    // TODO not used methods



//    public void visitExpressionStatement(GNode expressionStatement) {
//        if (IS_DEBUG)
//            logger.debug("\t******************** Entering expression statement: " + expressionStatement.toString() + "'s scope ********************");
//
//        table.enter(expressionStatement.getName());
//        table.mark(expressionStatement);
//        visit(expressionStatement);
//
//        if (IS_DEBUG)
//            logger.debug("\t******************** Exiting expression statement: " + expressionStatement.toString() + "'s scope ********************");
//        table.exit();
//    } // End of the visitExpressionStatement

//    public void visitExpression(GNode expression) {
//        if (IS_DEBUG)
//            logger.debug("\t******************** Entering expression: " + expression.toString() + "'s scope ********************");
//
//        table.enter(expression.getName());
//        table.mark(expression);
//        visit(expression);
//
//        if (IS_DEBUG)
//            logger.debug("\t******************** Exiting expression: " + expression.toString() + "'s scope ********************");
//        table.exit();
//    } // End of the visitExpressionStatement





} // End of the SymbolTableBuilder class
