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
import xtc.util.Runtime;
import xtc.util.SymbolTable;

// Utility imports
import edu.nyu.oop.util.RecursiveVisitor;

// General imports
import java.util.ArrayList;
import java.util.List;

class SymbolTableBuilder extends RecursiveVisitor {
    final private SymbolTable table;
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
    private boolean IS_DEBUG = false;

    SymbolTableBuilder() {this.table = new SymbolTable();}

    SymbolTable getTable(GNode someClass) {
        build(someClass);
        return table;
    }

    private void build(GNode someClass) {
        if (IS_DEBUG)
            logger.debug("******************** Building symbol table ********************");

        this.dispatch(someClass);

        if (IS_DEBUG)
            logger.debug("******************** Built symbol table ********************");
    } // End of the setup method

    //////////////////// Visit methods ////////////////////

    public void visitExpressionStatement(GNode expressionStatement) {
        if (IS_DEBUG)
            logger.debug("\t\tEntering expression statement");

        table.enter(expressionStatement.getName());
        table.mark(expressionStatement);
        visit(expressionStatement);

        if (IS_DEBUG)
            logger.debug("\t\tExiting expression statement");
        table.exit();
    } // End of the visitExpressionStatement

    public void visitExpression(GNode expression) {
        if (IS_DEBUG)
            logger.debug("\t\tEntering expression: " + expression.toString());

        table.enter(expression.getName());
        table.mark(expression);
        visit(expression);

        if (IS_DEBUG)
            logger.debug("\t\tExiting expression");
        table.exit();
    } // End of the visitExpressionStatement

    public void visitClass(GNode classNode) {
        String className = classNode.getString(0);
        if (IS_DEBUG)
            logger.debug("\t\t******************** Entering class: " + className + "'s scope ********************");

        table.enter(className);
        table.mark(classNode);
        visit(classNode.getGeneric(5));

        table.exit();
        if (IS_DEBUG)
            logger.debug("\t\t******************** Exiting class: " + className + "'s scope ********************");
    } // End of the visitClass method

    public void visitMethodDeclaration(GNode method) {
        String methodName = method.getString(0);
        if (IS_DEBUG)
            logger.debug("\t\t******************** Entering method: " + methodName + "'s scope ********************");

        table.enter(methodName);
        table.mark(method);
        visit(method);
        table.exit();

        if (IS_DEBUG)
            logger.debug("\t\t******************** Exiting method: " + methodName + "'s scope ********************");
    } // End of the visitMethodDeclaration method

    public void visitConstructorDeclaration(GNode constructor) {
        String constructorName = constructor.getString(0);

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
            logger.debug("\t\t\tEntering QualifiedIdentifier: " + n.toString() + "'s scope");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n.size(); i++) {
            if (sb.length() > 0)
                sb.append(Constants.QUALIFIER);
            sb.append(n.getString(i));
        }

        if (IS_DEBUG)
            logger.debug("\t\t\tExiting QualifiedIdentifier: " + n.toString() + "'s scope");

        return sb.toString();
    } // End of the visitQualifiedIdentifier method

    public final Type visitVoidType(final GNode n) {
        if (IS_DEBUG)
            logger.debug("\t\t\tEntering VoidType's scope");

        if (IS_DEBUG)
            logger.debug("\t\t\tExiting VoidType's scope");

        return JavaEntities.nameToBaseType("void");
    } // End of the visitVoidType method

    public final Type visitParameter(final GNode n) {
        if (IS_DEBUG)
            logger.debug("\t\t\tEntering Parameter");

        // OLD FormalParameter children layout
        // Node 0: Modifiers
        // Node 1: Type
        // Node 3: parameter name (String!)

        // NEW Parameter node children layout
        // Node 0: Parameter name (String!)
        // Node 1: Parameter Type node
        // Node 2: Parameter Modifiers

        String id = n.getString(0);
        Type dispatched = (Type) dispatch(n.getGeneric(1));
        Type result = VariableT.newParam(dispatched, id);

        // Checks the modifiers list
        // TODO why does this add the final modifier if modifiers exit?
        if (n.getGeneric(2).size() != 0)
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
            logger.debug("\t\t\tExiting Parameter");

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
            logger.debug("\t\t\tExiting For-statement");
    } // End of the visitForStatement method

    /**
     * Visit a Modifiers = Modifier*.
     */
    public final List<Attribute> visitModifiers(final GNode modifiers) {
        if (IS_DEBUG)
            logger.debug("\t\t\tEntering Modifiers");
        List<Attribute> result = new ArrayList<Attribute>();

        for (int i = 0; i < modifiers.size(); ++i) {
            Object modifierObject = modifiers.get(i);

            // TODO fix this upstream so that the modifiers node is properly mutated
            String modifierName;
            if (modifierObject instanceof GNode) {
                // We know that this is a level deeper
                modifierName = modifiers.getGeneric(i).getString(0);
            }
            else {
                modifierName = modifiers.getString(i);
            }

            // Gets the modifier name
            Attribute modifierAttribute = JavaEntities.nameToModifier(modifierName);

            // Adds the modifier in if it can't be found
            if (modifierAttribute == null)
                logger.error("unexpected modifier " + modifierName, modifiers);
            else if (result.contains(modifierAttribute))
                logger.error("duplicate modifier " + modifierName, modifiers);
            else
                result.add(modifierAttribute);
        }

        if (IS_DEBUG)
            logger.debug("\t\t\tExiting Modifiers");

        return result;
    } // End of the visitModifiers method

    public final List<Type> visitFieldDeclaration(final GNode n) {
        // FieldDeclaration node children layout
        // Node 0: Modifiers
        // Node 1: Type node
        // Node 2: Declarators

        if (IS_DEBUG)
            logger.debug("\t\t\tEntering FieldDeclaration: " + n.toString());

        @SuppressWarnings("unchecked")
        final List<Attribute> modifiers = (List<Attribute>) dispatch(n.getNode(0));
        Type type = (Type) dispatch(n.getNode(1));

        // Adds the field declaration to the symbol table
        GNode declarator = n.getGeneric(2).getGeneric(0);
        String fieldName = declarator.getString(0);

        // Defines the field under the method scope
        table.current().getParent().define(fieldName, type);

        if (IS_DEBUG)
            logger.debug("\t\t\tExiting FieldDeclaration");

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
} // End of the SymbolTableBuilder class
