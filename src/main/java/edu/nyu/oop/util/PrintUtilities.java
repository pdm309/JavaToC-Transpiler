package edu.nyu.oop.util;

import org.slf4j.Logger;
import xtc.tree.GNode;
import xtc.tree.Printer;
import xtc.type.AliasT;
import xtc.type.Type;
import xtc.util.SymbolTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import xtc.type.VariableT;

public class PrintUtilities {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(PrintUtilities.class);
    private static boolean IS_DEBUG = false;

    /**************************************** Literal methods ****************************************/

    public static String visitIntegerLiteral(GNode integer) {
        return integer.getString(0);
    } // End of the visitIntegerLiteral method

    public static String visitStringLiteral(GNode string) {
        return "__rt::literal(" + string.getString(0) + ")";
    } // End of the visitStringLiteral method

    public static String visitFloatingPointLiteral(GNode floatingPoint) {
        return floatingPoint.getString(0);
    } // End of the visitFloatingPointLiteral method

    public static String visitBooleanLiteral(GNode bool) {
        return bool.getString(0);
    } // End of the visitBooleanLiteral method

    public static String visitCharacterLiteral(GNode character) {
        return character.getString(0);
    } // End of the visitCharacterLiteral method

    public static String visitPrimaryIdentifier(GNode identifier) {
        return identifier.getString(0);
    } // End of the visitPrimaryIdentifier method

    public static String visitQualifiedIdentifier(GNode qualifiedIdentifier) {
        return qualifiedIdentifier.getString(0);
    } // End of the visitQualifiedIdentifier method

    /**************************************** Manual dispatching methods (return string) ****************************************/

    public static String getVisitFunctionString(GNode argumentNode) {
        switch (argumentNode.getName()) {
            case "IntegerLiteral":
                return visitIntegerLiteral(argumentNode);
            case "StringLiteral":
                return visitStringLiteral(argumentNode);
            case "FloatingPointLiteral":
                return visitFloatingPointLiteral(argumentNode);
            case "BooleanLiteral":
                return visitBooleanLiteral(argumentNode);
            case "CharacterLiteral":
                return visitCharacterLiteral(argumentNode);
            case "PrimaryIdentifier":
                return visitPrimaryIdentifier(argumentNode);
            case "QualifiedIdentifier":
                return visitQualifiedIdentifier(argumentNode);
//            case "SelectionExpression":
//                visitSelectionExpression(n);
//            case "CallExpression":
//                return "call expression";
//                visitCallExpression(n); // TODO
            default:
                // TODO finish this off
                logger.debug("Error: Implement" + argumentNode.getName() + " in PrintUtil");
        }
        return null;
    } // End of the getStringFromArgument




    /**************************************** Helper methods (public) ****************************************/

    public static void cout(String line, Printer printer) {
        printer.incr().indent().pln("cout << \"" + line + "\" << endl;").decr();
    } // End of the cout method

    public static void printPackageName(GNode packageNode, String deliminator, Printer printer) {
        List<String> packageNames = getPackageList(packageNode);
        boolean isFirst = true;
        for (String packageName : packageNames) {
            if (isFirst)
                isFirst = false;
            else
                printer.p(deliminator);
            printer.p(packageName);
        }
    } // End of the printPackageName method

    public static List<String> getPackageList(GNode packageNode) {
        String deliminatedPackageName = packageNode.get(0).toString();
        String[] packageName = deliminatedPackageName.split("\\.");
        return new ArrayList<>(Arrays.asList(packageName));
    } // End of getPackageName method

    public static String getPackageName(GNode packageNode) { // TODO remove
        // Prints the package namespaces
        String deliminatedPackageName = packageNode.get(0).toString();
        String[] packageName = deliminatedPackageName.split("\\.");

        String packageString = "";
        boolean isFirstString = true;
        for (String aPackageName : packageName) {
            if (isFirstString)
                isFirstString = false;
            else
                packageString += "::";
            packageString += aPackageName;
        }
        return packageString;
    } // End of the getPackageName method

    public static String extractClass(Type type) {
        return type.toAlias().getName();
    } // End of the extractClass method

    public static String extractClass(AliasT node) {return node.getName();}

    public static String extractClass(VariableT node) {
        Type type = node.getType();

        if (type != null) {
            if (type.hasAlias())
                return extractClass(type.toAlias());

            else
                return type.getName();
        }
        else {
            return node.getName();
        }
    }
    /**************************************** Java system calls ****************************************/

    public static String jPrintln(GNode argumentsNode) {
        return "cout << " + getOutput(argumentsNode) + " << endl";

    } // End of the jPrintln method

    public static String jPrint(GNode argumentsNode) {
        return "cout << " + getOutput(argumentsNode);
    } // End of the jPrint method

    // Used by jPrint and jPrintln
    private static String getOutput(GNode argumentsNode) { // TODO replace this method with visit method
        StringBuilder output = new StringBuilder();

        // Iterates through all the arguments in the print statement
        for (int i = 0; i < argumentsNode.size(); ++i) {
            if (i > 0)
                output.append(" + ");

            String nodeName = argumentsNode.getGeneric(i).getName();

            switch (nodeName) {
                case "IntegerLiteral":
                case "FloatingPointLiteral":
                case "BooleanLiteral":
                case "CharacterLiteral":
                    output.append((String) argumentsNode.getGeneric(i).get(0));
                    break;
                case "StringLiteral":
                    output.append(visitStringLiteral(argumentsNode.getGeneric(i)));
                    break;
                case "PrimaryIdentifier":
                    output.append("__this->" + argumentsNode.getGeneric(i).getString(0));
                    break;
                case "SelectionExpression":
                    // Prints out a variable
                    output.append((String) argumentsNode.getGeneric(i).getGeneric(0).get(0));
                    if (argumentsNode.getGeneric(i).get(1) != null)
                        output.append("->").append((String) argumentsNode.getGeneric(i).get(1));
                    break;
                case "CallExpression":
                    // Puts in a method call
                    GNode methodNode = argumentsNode.getGeneric(i);
                    String methodName = "";

//                    logger.debug(methodNode.toString());

                    if (methodNode.getGeneric(0) == null) {
                        // We know that we're calling a static method
                        output.append(methodNode.getString(2));
                        output.append("(");
                    }
                    else {
                        // Gets the variable to call
                        String callingVariable = null;
                        try {
                            callingVariable = (String) methodNode.getGeneric(0).get(0);
                        }
                        catch(Exception e){
                            //In some cases such as input 9 this doesn't find an object it can cast to a string at methodNode.getGeneric(0).get(0)
                            // but one level deeper is a value "a", so we take from that child value
                            callingVariable = (String) methodNode.getGeneric(0).getGeneric(0).get(0);
                        }
                        output.append(callingVariable);
                        output.append("->__vptr->");

                        methodName = (String) argumentsNode.getGeneric(i).get(2);
                        // Gets the name of the method to call
                        output.append(methodName);
                        output.append("(").append(callingVariable); // Adds the self
                    }

                    // Adds the arguments of the method call
                    GNode methodCallArgumentsNode = argumentsNode.getGeneric(i).getGeneric(3);
                    for (int j = 0; j < methodCallArgumentsNode.size(); ++j) {
                        if (j > 0)
                            output.append(", ");

                        String argument = getVisitFunctionString(methodCallArgumentsNode.getGeneric(j));
                        if (argument != null)
                            output.append(argument);
                    }
                    output.append(")");
                    if (methodName.equals("toString"))
                        output.append("->data");

                    break;
                case "SubscriptExpression":
                    GNode subscriptNode = argumentsNode.getGeneric(0);
                    GNode primaryIdentifier = subscriptNode.getGeneric(0);
                    GNode primaryIdentifier2 = subscriptNode.getGeneric(1);
                    if (primaryIdentifier.get(0).toString().equals("args"))
                        output.append("argv");
                    else
                        output.append(primaryIdentifier.get(0).toString());
                    output.append("[");
                    output.append(primaryIdentifier2.get(0).toString());
                    output.append("]");
                default:
                    logger.warn("!!!!!!!!!! ERROR: make sure to deal with this node name: " + nodeName);  // TODO
            }
        } // End of generating the output
        return output.toString();
    } // End of the getOutput method


















    // TODO
    // TODO
    // TODO
    // TODO
    // TODO Delete this shit after
    // TODO
    // TODO
    // TODO
    // TODO
    // TODO










//    private static void printReturnStatement(GNode line, Printer printer, SymbolTable table) {
//        GNode returnTypeNode = line.getGeneric(0);
//
//        String output = "return ";
//        switch (returnTypeNode.getName()) {
//            case "IntegerLiteral":
//            case "StringLiteral":
//            case "PrimaryIdentifier":
//                output += (String) returnTypeNode.get(0);
//                break;
//            default:
//                output += "did not catch: " + returnTypeNode.getName();
//        }
//
//        output += ";";
//        printer.indent().pln(output);
//    } // End of the printReturnStatement method


    // private static void printFieldDeclaration(GNode line, Printer printer, SymbolTable table) {
//        // Field declaration children layout
//        // Node 0: Modifiers node
//        // Node 1: Type node
//        // Node 2: Declarators node
//        GNode typeNode = line.getGeneric(1).getGeneric(0);
//        GNode dimensionNode = line.getGeneric(1).getGeneric(1);
//        StringBuilder sb = new StringBuilder();
//
//        // Adds the field type
//        sb.append((String) typeNode.get(0));
//
//        // Adds the array dimension if applicable
//        if (dimensionNode != null)
//            sb.append("[]");
//        // TODO we don't handle dimensions fully just yet
//
//        sb.append(" ");
//
//        // Adds the field name
//        GNode declaratorNode = line.getGeneric(2).getGeneric(0);
//        GNode declaration = declaratorNode.getGeneric(2);
//        sb.append((String) declaratorNode.get(0));
//
//        // Adds the right hand declaration if there
//        if (declaration != null) {
//            sb.append(" = ");
//
//            if (declaration.getName().equals("PrimaryIdentifier")) {
//                // Some kind of assignment operation is here
//                // We first do a check to see if the type matches, and casts if it does not
//
//                if (table.current().isDefined((String) declaration.get(0))) {
//                    printer.pln("(some cast) ");
//                }
//                else {
//
//                }
////                Type type = (Type) table.current().lookup();
////                if ()
//
//                sb.append(declaration.get(0));
//            }
//            else if (declaration.getName().equals("NewClassExpression")) {
//                // TODO handle new form of constructors
//                // We instantiate a new class
//                // NewClassExpression children layout
//                // Node 2: QualifiedIdentifier node <-- which type it is
//                // Node 3: Arguments node
//                sb.append("new __");
//                sb.append((String) declaration.getGeneric(2).get(0));
//                sb.append("(");
//
//                // Adds any arguments if there are any
//                GNode argumentsNode = declaration.getGeneric(3);
//                boolean isFirstTime = true;
//                for (int i = 0; i < argumentsNode.size(); ++i) {
//                    if (isFirstTime)
//                        isFirstTime = false;
//                    else
//                        sb.append(", ");
//                    sb.append((String) argumentsNode.getGeneric(i).get(0));
//                }
//                sb.append(")");
//            }
//        }
//
//        // Finishes the line
//        sb.append(";");
//
//        printer.indent().pln(sb.toString());
//    } // End of the printFieldDeclaration





//
//    private static void printExpressionStatement(GNode line, Printer printer, SymbolTable table) {
//        // ExpressionStatement children layout
//        // Node 0: CallExpression
//
//        // CallExpression children layout
//        GNode callExpression = line.getGeneric(0);
//
//        if (PrintUtilities.isSystemExpression(callExpression))
//            printSystemExpression(callExpression);
//        else {
//            // It is a custom expression call, probably to a static method
//            // TODO
//            printer.indent().pln("is not system");
//        }
//
//        // TODO remove after
////        printer.indent().pln(line.toString());
//    } // End of the printExpressionStatement method

//    private static void printSystemExpression(GNode callExpression) {
//        GNode argumentsNode = callExpression.getGeneric(3);
//
//        if (PrintUtilities.isPrintln(callExpression))
//            PrintUtilities.jPrintln(argumentsNode);
//        else if (PrintUtilities.isPrint(callExpression))
//            PrintUtilities.jPrint(argumentsNode);
//    } // End of the printSystemExpression method




















} // End of the PrintUtilities class
