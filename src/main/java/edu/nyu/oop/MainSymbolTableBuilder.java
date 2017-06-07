package edu.nyu.oop;

import edu.nyu.oop.util.RecursiveVisitor;
import org.slf4j.Logger;
import xtc.lang.JavaEntities;
import xtc.tree.Attribute;
import xtc.tree.GNode;
import xtc.type.AliasT;
import xtc.type.Type;
import xtc.type.VariableT;
import xtc.util.Runtime;
import xtc.util.SymbolTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MainSymbolTableBuilder extends RecursiveVisitor {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
    final private SymbolTable table;
    final private Runtime runtime;
    final private Map<String, Type.Tag> summary;

    MainSymbolTableBuilder() {
        this.runtime = new Runtime();
        this.table = new SymbolTable();
        this.summary = new HashMap<>();
    } // End of the constructor

    SymbolTable getTable(GNode mainMethod) {
        super.dispatch(mainMethod);
        return table;
    }
    Map<String, Type.Tag> getSummary(GNode n) {
        this.dispatch(n);
        return summary;
    }

    public void visitMethodDeclaration(GNode n) {
        String methodName = JavaEntities.methodSymbolFromAst(n);
        table.enter(methodName);
        table.mark(n);
        visit(n);
        table.exit();
    } // End of the visitMethodDeclaration method

    /////////////////////////////// Unverified stuff copied from symbol-table-in-class

    public void visitBlock(GNode n) {
        table.enter(table.freshName("block"));
        table.mark(n);
        visit(n);
        table.exit();
    }

    public void visitForStatement(GNode n) {
        table.enter(table.freshName("forStatement"));
        table.mark(n);
        visit(n);
        table.exit();
    }

    public List<Type> visitFieldDeclaration(final GNode n) {
        runtime.console().pln("visiting field declaration").flush();
        runtime.console().pln("\t" + n.toString()).flush();

        table.enter(table.freshName("fieldDeclaration"));
        table.mark(n);
        visit(n);
        table.exit();
        @SuppressWarnings("unchecked")
        Type type = (Type) dispatch(n.getNode(1));
        return processDeclarators(type, n.getGeneric(2));
    }

    public void visitExpressionStatement(final GNode n) {
        runtime.console().pln("visiting expression statement").flush();
        runtime.console().pln("\t" + n.toString()).flush();

        table.enter(table.freshName("expressionStatement"));
        table.mark(n);
        visit(n);
        table.exit();
    }

    public void visitPrimaryIdentifier(final GNode n) {
        runtime.console().pln("visiting primary identifier").flush();
        runtime.console().pln("\t" + n.toString()).flush();

        String name = n.getString(0);
        runtime.console().pln("\tName: " + name).flush();

        if (table.current().isDefined(name)) {
            Type type = (Type) table.current().lookup(name);

            // TODO:
            //   - Store all local variable names in 'summary' as a key with a value of their type
//            summary.put(name, type.tag());
//                if (JavaEntities.isLocalT(type))
//                    runtime.console().p("Found occurrence of local variable ").p(name).p(" with type ").pln(type.tag().name());

        }
        else {
            runtime.console().pln(n.toString()).flush();
            //entity.scope(table.current().getQualifiedName());
//            table.current().define(name, getTagFromPrimaryIdentifier(n));
        }
    }





































    /**
     * Visit a Modifiers = Modifier*.
     */
    public final List<Attribute> visitModifiers(final GNode n) {
        final List<Attribute> result = new ArrayList<Attribute>();
        for (int i = 0; i < n.size(); i++) {
            final String name = n.getGeneric(i).getString(0);
            final Attribute modifier = JavaEntities.nameToModifier(name);
            if (null == modifier)
                runtime.error("unexpected modifier " + name, n);
            else if (result.contains(modifier))
                runtime.error("duplicate modifier " + name, n);
            else
                result.add(modifier);
        }
        return result;
    }



    public final List<Type> processDeclarators(final Type type, final GNode declarators) {
        final List<Type> result = new ArrayList<Type>();
        boolean isLocal = JavaEntities.isScopeLocal(table.current().getQualifiedName());
        for (final Object i : declarators) {
            GNode declNode = (GNode) i;
            String name = declNode.getString(0);
            Type dimType = JavaEntities.typeWithDimensions(type,
                    countDimensions(declNode.getGeneric(1)));
            Type entity = isLocal ? VariableT.newLocal(dimType, name) :
                    VariableT.newField(dimType, name);
            if (null == table.current().lookupLocally(name)) {
                result.add(entity);
                table.current().define(name, entity);
                //entity.scope(table.current().getQualifiedName());
            }
        }
        return result;
    }

    public final Type visitFormalParameter(final GNode n) {
        assert null == n.get(4) : "must run JavaAstSimplifier first";
        String id = n.getString(3);
        Type dispatched = (Type) dispatch(n.getNode(1));
        Type result = VariableT.newParam(dispatched, id);
        if (n.getGeneric(0).size() != 0)
            result.addAttribute(JavaEntities.nameToModifier("final"));
        if (null == table.current().lookupLocally(id)) {
            table.current().define(id, result);
            result.scope(table.current().getQualifiedName());
        } else
            runtime.error("duplicate parameter declaration " + id, n);
        assert JavaEntities.isParameterT(result);
        return result;
    }

    public final Type visitPrimitiveType(final GNode n) {
        final Type result = JavaEntities.nameToBaseType(n.getString(0));
        return result;
    }

    public final Type visitType(final GNode n) {
        final boolean composite = n.getGeneric(0).hasName("QualifiedIdentifier");
        final Object dispatched0 = dispatch(n.getNode(0));
        assert dispatched0 != null;
        final Type componentT = composite ? new AliasT((String) dispatched0) : (Type) dispatched0;
        final int dimensions = countDimensions(n.getGeneric(1));
        final Type result = JavaEntities.typeWithDimensions(componentT, dimensions);
        return result;
    }

    public static int countDimensions(final GNode dimNode) {
        return null == dimNode ? 0 : dimNode.size();
    }

    public final Type visitVoidType(final GNode n) {
        return JavaEntities.nameToBaseType("void");
    }

} // End of the MainSymbolTableBuilder
