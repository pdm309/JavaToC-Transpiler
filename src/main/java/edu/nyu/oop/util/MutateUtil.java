package edu.nyu.oop.util;

// xtc imports
import xtc.tree.GNode;

// Utility imports
import edu.nyu.oop.util.StateCollectors.Type;
import edu.nyu.oop.util.StateCollectors.Field;
import edu.nyu.oop.util.StateCollectors.Parameter;

public class MutateUtil {
    public static GNode getNewConstructorCall(GNode old, String identifier) {




//        Declarator:
//        ReturnStatement(NewClassExpression(null, null, QualifiedIdentifier("D"), Arguments(), null))


        // OLD NewClassExpression
        // Node 2: QualifiedIdentifier node <-- contains the type
        // Node 3: Arguments node

        // NEW ConstructorCall
        // Node 0: NewClassExpression
            // Node 0.0: QualifiedIdentifier node <-- contains the type (converted though, so "B" -> "__B")
        // Node 1: ExpressionStatement (init method call)
            // Node 1.0: CallExpression node
                // Node 1.0.0: SelectionExpression node
                    // Node 1.0.0.0: PrimaryIdentifier <-- class name
                    // Node 1.0.0.1: null // TODO refactor after
                // Node 1.0.1: null // TODO refactor after
                // Node 1.0.2: constructor name (String!, should just be __init)
                // Node 1.0.3: Arguments node <-- Need to add

        GNode constructorCall = GNode.create("ConstructorCall");

        GNode newClassExpression = GNode.create("NewClassExpression");
        String underscoredName = "__" + old.getGeneric(2).getString(0);

        // Adds the qualified identifier
        GNode qualifiedIdentifier = GNode.create("QualifiedIdentifier");
        qualifiedIdentifier.add(underscoredName);
        newClassExpression.add(0, qualifiedIdentifier);

        // Adds the expression statement (init method call)
        GNode expressionStatement = GNode.create("ExpressionStatement");

            // Adds the callExpression
            GNode callExpression = GNode.create("CallExpression");
            GNode selectionExpression = GNode.create("SelectionExpression");

                // Adds the PrimaryIdentifier (class name with underscores)
                GNode primaryIdentifier = GNode.create("PrimaryIdentifier");
                primaryIdentifier.add(underscoredName);
                selectionExpression.add(primaryIdentifier);
                selectionExpression.add(null);

            // Adds the self argument
            GNode argumentsNode = old.getGeneric(3);

            if (!argumentsNode.hasVariable())
                argumentsNode = GNode.ensureVariable(argumentsNode);
            GNode primaryArgumentIdentifier = GNode.create("PrimaryIdentifier");
            primaryArgumentIdentifier.add(identifier);

            argumentsNode.add(0, primaryArgumentIdentifier);

            callExpression.add(0, selectionExpression); // Selector
            callExpression.add(1, null);
            callExpression.add(2, "__init"); // Method name
            callExpression.add(3, argumentsNode); // Arguments node




            expressionStatement.add(0, callExpression);

        constructorCall.add(0, newClassExpression);
        constructorCall.add(1, expressionStatement);

        return constructorCall;
    } // End of the getNewConstructorCall method

    public static GNode getTypeNodeFromType(Type type) {
        // Type node children layout
        // Node 0: PrimitiveType OR QualifiedIdentifier node
        // Node 1: Dimension (String OR null!)
        GNode typeNode = GNode.create("Type");
        GNode typeParent;
        switch (type.getType()) {
            case "byte":
            case "short":
            case "int":
            case "long":
            case "float":
            case "double":
            case "boolean":
            case "char":
                typeParent =  GNode.create("PrimitiveType");
            default:
                typeParent = GNode.create("QualifiedIdentifier");
        }
        typeParent.add(type.getType());

        typeNode.add(0, typeParent); // Adds the type
        typeNode.add(1, type.getDimension()); // Adds the dimension

        return typeNode;
    } // End of the getTypeNodeFromType

    public static GNode getFieldNodeFromFieldClass(Field field) {
        // Field node children layout
        // Node 0: Field name (String!)
        // Node 1: Field Type node
        // Node 2: Field Modifiers node

        GNode fieldNode = GNode.create("FieldDeclaration");

        // Adds the name
        fieldNode.add(0, field.getName());

        // Adds the type
        fieldNode.add(1, getTypeNodeFromType(field.getType()));

        // Adds the field modifiers
        GNode modifiersNode = GNode.create("Modifiers");
        for (String modifier : field.getModifiers())
            modifiersNode.add(modifier);
        fieldNode.add(2, modifiersNode);

        // Adds this field to the data layout
        return fieldNode;
    } // End of the createFieldFromFieldClass method

    public static GNode getReturnTypeNodeFromReturnType(Type type) {
        // Return node children layout
        // Node 0: Type (String!)
        // Node 1: Dimension (String!)

        GNode returnTypeNode = GNode.create("ReturnType");
        returnTypeNode.add(type.getType());
        returnTypeNode.add(type.getDimension());
        return returnTypeNode;
    } // End of the getReturnTypeNodeFromReturnType method

    public static GNode getParameterNodeFromParameter(Parameter parameter) {
        // Parameter node children layout
        // Node 0: Parameter name (String!)
        // Node 1: Parameter Type node
        // Node 2: Parameter Modifiers
        GNode parameterNode = GNode.create("Parameter");

        // Adds the parameter name
        parameterNode.add(0, parameter.getName());

        // Adds the parameter type
        parameterNode.add(1, getTypeNodeFromType(parameter.getType()));

        // Adds the parameter modifiers
        GNode parameterModifiersNode = GNode.create("Modifiers");
        for (String modifier : parameter.getModifiers())
            parameterModifiersNode.add(modifier);
        parameterNode.add(2, parameterModifiersNode);

        return parameterNode;
    } // End of the getParameterNodeFromParameter method

} // End of the MutateUtil class
