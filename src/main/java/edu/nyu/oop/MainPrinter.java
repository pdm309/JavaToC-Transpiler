package edu.nyu.oop;

// xtc imports
import edu.nyu.oop.util.*;
import org.slf4j.Logger;
import xtc.lang.JavaEntities;
import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Printer;
import xtc.tree.Visitor;
import xtc.type.Type;
import xtc.util.SymbolTable;
import xtc.util.SymbolTable.Scope;

// Utility imports

// General imports
import java.io.*;
import java.util.ArrayList;
import java.util.List;

class MainPrinter extends Visitor {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
    private Printer printer;
    private boolean IS_DEBUG = false;
    private String outputLocation = XtcProps.get("output.location");
    private SymbolTable table;

    MainPrinter(String outputfile) {
        Writer w;
        try {
            FileOutputStream fos = new FileOutputStream(outputLocation + "/" + outputfile);
            OutputStreamWriter ows = new OutputStreamWriter(fos, "utf-8");
            w = new BufferedWriter(ows);
            this.printer = new Printer(w);
        } catch (Exception e) {
            throw new RuntimeException("Output location not found. Create the /output directory.");
        }

        // Register the visitor as being associated with this printer.
        // We do this so we get some nice convenience methods on the printer,
        // such as "dispatch", You should read the code for Printer to learn more.
        printer.register(this);
    } // End of the MainPrinter method

    /**************************************** Literal methods (print) ****************************************/

    public void visitIntegerLiteral(GNode integer) {
        printer.p(PrintUtilities.visitIntegerLiteral(integer));
    } // End of the visitIntegerLiteral method

    public void visitStringLiteral(GNode string) {
        printer.p(PrintUtilities.visitStringLiteral(string));
    } // End of the visitStringLiteral method

    public void visitFloatingPointLiteral(GNode floatingPoint) {
        printer.p(PrintUtilities.visitFloatingPointLiteral(floatingPoint));
    } // End of the visitFloatingPointLiteral method

    public void visitBooleanLiteral(GNode bool) {
        printer.p(PrintUtilities.visitBooleanLiteral(bool));
    } // End of the visitBooleanLiteral method

    public void visitCharacterLiteral(GNode character) {
        printer.p(PrintUtilities.visitCharacterLiteral(character));
    } // End of the visitCharacterLiteral method

    public String visitPrimaryIdentifier(GNode n) {
        return n.getString(0);
//        printer.p(n.getString(0)); // TODO refactor this
    } // End of the visitPrimaryIdentifier method

    private void printPrimaryIdentifier(GNode n) {
        printer.p(n.getString(0));
    }

//    public void visitQualifiedIdentifier(GNode qualifiedIdentifier) {
//       printer.p(PrintUtilities.visitQualifiedIdentifier(qualifiedIdentifier));
//    } // TODO refactor to allow this

    /**************************************** Manual dispatching methods (immediate printing) ****************************************/

    private void callVisitFunction(GNode n, GNode declaredType) {
        switch (n.getName()) {
            case "IntegerLiteral":
                visitIntegerLiteral(n);
                break;
            case "StringLiteral":
                visitStringLiteral(n);
                break;
            case "FloatingPointLiteral":
                visitFloatingPointLiteral(n);
                break;
            case "BooleanLiteral":
                visitBooleanLiteral(n);
                break;
            case "CharacterLiteral":
                visitCharacterLiteral(n);
                break;
            case "SelectionExpression":
                visitSelectionExpression(n);
                break;
            case "CallExpression":
                visitCallExpression(n);
                break;
            case "Arguments":
                visitArguments(n, declaredType);
                break;
            case "PrimaryIdentifier":
                printPrimaryIdentifier(n);
                break;
            case "SubscriptExpression":
                visitSubscriptExpression(n);
            default:
                // TODO finish this off
                logger.debug("TODO need to finish off for most other method calls " + n.getName());
        }
    } // End of the callVisitFunction method

    /**************************************** Helper methods ****************************************/

    // TODO stick this inside PrintUtil
    private void printFunctionCall(List<String> function, GNode argumentsNode, GNode declaredType) {
        // Generates the function name
        boolean isFirstPart = true;
        StringBuilder sb = new StringBuilder();
        for (String functionPart : function) {
            if (isFirstPart)
                isFirstPart = false;
            else
                sb.append("::");
            sb.append(functionPart);
        }

        switch (sb.toString()) {
            case "System::out::println":
                printer.p(PrintUtilities.jPrintln(argumentsNode));
                break;
            case "System::out::print":
                printer.p(PrintUtilities.jPrint(argumentsNode));
                break;
            default:
                if (IS_DEBUG)
                    logger.debug("Non-default function call: " + sb.toString());

                // Prints the function name
                printer.p(sb.toString());

                // Prints the function arguments
                printer.p("(");
                callVisitFunction(argumentsNode, declaredType);
                printer.p(")");
        }
    } // End of the getFunctionCall method


    // TODO stick this inside PrintUtil
    private void printInstanceMethodCall(GNode methodIdentifier, GNode argumentsNode, String methodName) {
        printer.p(methodIdentifier.getString(0));
        printer.p("->__vptr->");
        printer.p(methodName);
        printer.p("(");
        //If PrimaryIdentifier is the argument of the method, pass in the methodIdentifier
        if (argumentsNode.size() == 1 && argumentsNode.getGeneric(0).getName().equals("PrimaryIdentifier")){
            printer.p(methodIdentifier.getString(0));
            printer.p(", ");
            printer.p(argumentsNode.getGeneric(0).getString(0));
            printer.p(")");
        }
        //Otherwise, print the arguments normally
        else {
            printer.p(methodIdentifier.getString(0));
            printer.p(", ");
            visit(argumentsNode);
            printer.p(")");
        }

    } // End of the getFunctionCall method

    private String getParametersString(GNode parameters) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < parameters.size(); ++i) {
            GNode parameter = parameters.getGeneric(i);

            if (i < 0)
                sb.append(", ");

            // If it's a void type just prints void
            if (parameter.get(1).toString().equals("VoidType()")) {
                sb.append("void");
                continue;
            }

            // Otherwise adds the type and name
            sb.append(parameter.getGeneric(1).getGeneric(0).getString(0)); // Type
            if (parameter.getGeneric(1).getString(1) != null)
                sb.append("[]"); // Dimension

            sb.append(" ").append(parameter.getString(0)); // Name
        }

        return sb.toString();
    } // End of the printParameters method

    /**************************************** Sectional methods ****************************************/

    public void print(GNode mainClass) {
        headOfFile(PrintUtilities.getPackageName(mainClass.getGeneric(1)));
        buildSymbolTable(mainClass);
        this.dispatch(mainClass);
        printer.flush(); // Important!
    } // End of the print method

    private void headOfFile(String packageName) {
        printer.pln("#include <iostream>");
        printer.pln();
        printer.pln("#include \"ptr.h\"");
        printer.pln("#include \"java_lang.h\"");
        printer.pln("#include \"output.h\"");
        printer.pln();

        printer.pln("using namespace " + packageName + ";");
        printer.pln("using namespace java::lang;");
        printer.pln("using namespace std;");
    } // End of the headOfFile method

    private void buildSymbolTable(GNode mainClass) {
        SymbolTableBuilder tableBuilder = new SymbolTableBuilder();
        this.table = tableBuilder.getTable(mainClass);
    } // End of the buildSymbolTable method

    /**************************************** Visit methods ****************************************/
    public void visit(Node n) {
        for (Object o : n) if (o instanceof Node) {
//            PrintUtilities.cout(((Node) o).getName(), printer);
            dispatch((Node) o);}
    }

    public List<String> visitSelectionExpression(GNode n) {
        List<String> selectionExpression = new ArrayList<>();

        String dispatched = (String) dispatch(n.getGeneric(0));

        if (dispatched != null)
            selectionExpression.add(dispatched);

        if (n.getString(1) != null)
            selectionExpression.add(n.getString(1));

        if (selectionExpression.size() > 1 && !selectionExpression.get(0).equals("System")) {

            if (selectionExpression.get(0).equals("args"))
                printer.p("argc");
            else {
                printer.p(selectionExpression.get(0));
                printer.p("->");
                printer.p(selectionExpression.get(1));
            }
        }


        return selectionExpression;
    } // End of the visitSelectionExpression method

    public void visitReturnStatement(GNode statement) {
        if (IS_DEBUG)
            logger.debug("\tPrinting out ReturnStatement");

        printer.indent().p("return ");

        for (int i = 0; i < statement.size(); ++i) {
            if (PrintUtilities.getVisitFunctionString(statement.getGeneric(i)) != null)
                printer.p(PrintUtilities.getVisitFunctionString(statement.getGeneric(i)));
        }

        printer.pln(";");
        if (IS_DEBUG)
            logger.debug("\tEnd Printing out ReturnStatement");
    } // End of the visitReturnStatement method

    public void visitArguments(GNode n) {
        for (int i = 0; i < n.size(); ++i) {
            if (i > 0)
               printer.p(", ");
//            visit(n.getGeneric(i)); // TODO why doesn't this work?
            callVisitFunction(n.getGeneric(i), null);
        }
    } // End of the visitArguments method

    public void visitArguments(GNode n, GNode declaredType) {
        for (int i = 0; i < n.size(); ++i) {
            if (i > 0)
                printer.p(", ");

            // Checks if we need to cast
            switch (n.getGeneric(i).getName()) {
                case "IntegerLiteral":
                case "StringLiteral":
                case "FloatingPointLiteral":
                case "BooleanLiteral":
                case "CharacterLiteral":
                    break;
                case "PrimaryIdentifier":
                    Type type = (Type) table.current().lookup(n.getGeneric(i).getString(0));
                    String extractedClass = PrintUtilities.extractClass(type);
                    if (declaredType != null && !declaredType.getString(0).substring(2).equals(extractedClass))
                        printer.p("(").p(declaredType.getString(0).substring(2)).p(") ");
                    break;
                default:
                    logger.debug("TODO: Add to switch statement in MainPrinter::visitArgument: " + n.getGeneric(i).getName());
            }

            callVisitFunction(n.getGeneric(i), declaredType);
        }
    } // End of the visitArguments method

    public void visitCallExpression(GNode n) {
        if (IS_DEBUG)
            logger.debug("\tEntering call expression");

        if (n.getGeneric(0) != null)
        {
            // NOTE: We have two cases here: when it has a selectionExpression, and when it doesn't
            switch (n.getGeneric(0).getName()){
                case "SelectionExpression":
                    List<String> functionList;
                    functionList = (List<String>) dispatch(n.getGeneric(0));
                    // Adds the name of the function
                    functionList.add(n.getString(2));
                    printFunctionCall(functionList, n.getGeneric(3), n.getGeneric(0).getGeneric(0));
                    break;
                case "PrimaryIdentifier":
                    printInstanceMethodCall(n.getGeneric(0), n.getGeneric(3), n.getString(2));
                    break;
                default:
                    logger.debug("TODO: Add to switch statement in MainPrinter::visitCallExpression: " + n.getGeneric(0).getName());
            }
        }
        if (IS_DEBUG)
            logger.debug("\tExiting call expression");

    } // End of the visitCallExpression method

    public void visitConstructorCall(GNode n) {
        // Calls the blank constructor
        printer.p("new ");
        dispatch(n.getGeneric(0));
        printer.pln(";");

        // Calls the relevant __init method
        dispatch(n.getGeneric(1));
        printer.indent();
    } // End of the visit

    public void visitNewClassExpression(GNode n) {

        // OLD NewClassExpression
        // Node 2: QualifiedIdentifier node <-- contains the type
        // Node 3: Arguments node

        // NEW NewClassExpression
        // Node 0: QualifiedIdentifier node <-- contains the type (converted though, so "B" -> "__B")
        // Node 1: ExpressionStatement (init method call)

        // NEW ExpressionStatement (init method call)
        // Node 0: CallExpression node

        // NEW CallExpression node
        // Node 0: SelectionExpression node
        // Node 1: constructor name (String!, should just be __init)
        // Node 2: Arguments node

        // NEW SelectionExpression nodee
        // Node 0: PrimaryIdentifier <-- Class name
        // Node 1: just set to null

        printer.p("new ");
        printer.p(PrintUtilities.getVisitFunctionString(n.getGeneric(0)));

        // Since there isn't any arguments to the constructor, no need to visit for args
        printer.p("(");
        printer.p(")");
    } // End of the visitNewClassExpression method

    public void visitClass(GNode n) {
        if (IS_DEBUG)
            logger.debug("\t*************** Printing out class: " + n.getString(0) + " ********************");

        SymbolTableUtil.enterScope(table, n);

        if (IS_DEBUG)
            logger.debug("\t\tEntered scope " + table.current().getName());

        visit(n);

        SymbolTableUtil.exitScope(table, n);

        if (IS_DEBUG)
            logger.debug("\t\tExited scope " + table.current().getName());
    } // End of the visitClassDeclaration method

    public void visitMethodDeclaration(GNode method) {
        String methodName = method.getString(0);
        if (IS_DEBUG)
            logger.debug("\t\t*************** Printing out method: " + methodName + " ***************");

        // Method children layout
        // Node 0: method name (String!)
        // Node 1: Return Type
        // Node 2: Modifiers
        // Node 3: Parameters
        // Node 4: Source
        // Node 5: Block

        SymbolTableUtil.enterScope(table, method);

        if (IS_DEBUG)
            logger.debug("\t\tEntered scope " + table.current().getName());

        // Prints the method signature
        printer.indent().pln();

        // Prints the return type
        printer.p(method.getGeneric(1).getString(0));
        if (method.getGeneric(1).get(1) != null)
            printer.p("[]"); // type dimension

        // Prints the method name
        printer.p(" ");
        printer.p(methodName);

        if (methodName.equals("main")){
            printer.pln("(int argc, char* argv[]) {");
        }

        else {
            // Prints any method parameters in
            printer.p("(");
            printer.p(getParametersString(method.getGeneric(3)));
            printer.pln(") {");
        }

        if (IS_DEBUG)
            logger.debug("Method AST:\n" + method.toString() + "\n");

        printer.incr().incr();
        visit(method);
        printer.decr().decr();

        printer.indent().pln("} // End of the " + methodName + " method");

        SymbolTableUtil.exitScope(table, method);

        if (IS_DEBUG)
            logger.debug("\t\tExited scope " + table.current().getName());
    } // End of the visitMethodDeclaration method

    public void visitBlock(GNode n) {
        if (IS_DEBUG)
            logger.debug("\tPrinting out Block");

        visit(n);

        if (IS_DEBUG)
            logger.debug("\tEnd printing out Block");
    } // End of the visitBlock method

    public void visitFieldDeclaration(GNode field) {
        // Field node children layout
        // Node 0: Modifiers
        // Node 1: Type node
        // Node 2: Declarators

        if (IS_DEBUG)
            logger.debug("\tPrinting out FieldDeclaration");

        printer.indent();

        // Prints the field type
        String declaredType = field.getGeneric(1).getGeneric(0).getString(0);
        printer.p(declaredType);
        if (field.getGeneric(1).getString(1) != null)
            printer.p("[]");

        GNode declarators = field.getGeneric(2);
        for (int i = 0; i < declarators.size(); ++i) {
            GNode declarator = declarators.getGeneric(i);


            // Prints the declarator name
            printer.p(" ").p(declarator.getString(0));

            if (declarator.getGeneric(2) != null) {
                printer.p(" = ");

            }


            if (declarator.getGeneric(2) != null) {
                switch (declarator.getGeneric(2).getName()) {
                    case "NewClassExpression":
                        // Casts it if the type is different
                        String instantiatedType = declarator.getGeneric(2).getGeneric(0).getString(0);
                        String typeToCompare = "__" + declaredType;
                        if (!typeToCompare.equals(instantiatedType))
                            printer.p("(").p(declaredType).p(") ");
                        visit(declarator);
                        break;
                    case "PrimaryIdentifier":
                        // Casts it if the type is different
                        Type type = (Type) table.current().lookup(declarator.getGeneric(2).getString(0));
                        String extractedClass = PrintUtilities.extractClass(type);
                        if (declaredType != null && !field.getGeneric(1).getGeneric(0).getString(0).equals(extractedClass))
                            printer.p("(").p(field.getGeneric(1).getGeneric(0).getString(0)).p(") ");
                        callVisitFunction(declarator.getGeneric(2), null);
                        break;
                    case "SelectionExpression":
                        if (!declarator.get(2).toString().contains("PrimaryIdentifier"))
                            visitSelectionExpression(declarator.getGeneric(2));
                    default:
                        visit(declarator);
                        logger.debug("TODO: Add to switch statement in MainPrinter::visitFieldDeclaration: " + declarator.getGeneric(2).getName());
                }
            }
        }

        printer.pln(";");

        if (IS_DEBUG)
            logger.debug("\tEnd printing out FieldDeclaration");
    } // End of the visitFieldDeclaration method

//    public void visitDeclarators(GNode n) {
//        for (int i = 0; i < n.size(); ++i) {
//            GNode declarator = n.getGeneric(i);
//            // Prints the declarator name
//            printer.p(" ").p(declarator.getString(0));
//
//            if (declarator.getGeneric(2) != null)
//                printer.p(" = ");
//
//            visit(declarator);
//        }
//    } // End of the visitDeclarators method

    public void visitExpressionStatement(GNode n) {
        if (IS_DEBUG)
            logger.debug("\tEntering expression statement");

        printer.indent();
        visit(n);

        printer.pln(";");

        if (IS_DEBUG)
            logger.debug("\tExiting expression statement");
    } // End of the visitExpressionStatement

    public void visitExpression(GNode n) {
        if (IS_DEBUG)
            logger.debug("\tEntering expression");

        if (n.get(0).toString().contains("SelectionExpression")) {

            if (n.toString().contains("CastExpression")) {
                String castToClass = n.getGeneric(2).getGeneric(0).getGeneric(0).getString(0);
                String castFromIdentifier = n.getGeneric(2).getGeneric(1).getString(0);
                printer.pln("__rt::java_cast<" + castToClass + ">(" + castFromIdentifier + ");");
                printer.indent();
            }


            visitSelectionExpression(n.getGeneric(0));
            printer.p(" " + n.get(1).toString() + " ");
            if (n.getGeneric(2).getName().toString().equals("PrimaryIdentifier"))
                printPrimaryIdentifier(n.getGeneric(2));
            else
                visitCastExpression(n.getGeneric(2));
        }
        else if (n.get(0).toString().contains("PrimaryIdentifier") && n.get(1).toString().contains("=")){
            switch (n.getGeneric(2).getName()){
                case "IntegerLiteral":
                     printPrimaryIdentifier(n.getGeneric(0));
                    printer.p(" = " + n.getGeneric(2).getString(0));
                    break;
                default:
                    //TODO: probably wanna handle more assignments here
                    break;
            }

        }

        if (IS_DEBUG)
            logger.debug("\tExiting expression");
    }

    public void visitCastExpression(GNode n) {
        GNode qualifiedIdentifier = n.getGeneric(0).getGeneric(0);

        printer.p("(");
        printer.p(qualifiedIdentifier.get(0).toString());
        printer.p(") ");
        printPrimaryIdentifier(n.getGeneric(1));
    }

    public void visitForStatement(GNode n) {
        GNode forControl = n.getGeneric(0);

        GNode type = forControl.getGeneric(1).getGeneric(0);

        GNode declarators = forControl.getGeneric(2);
        GNode loopVariable = declarators.getGeneric(0);
        printer.indent();
        printer.p("for(");
        printer.p(type.get(0).toString() + " ");
        printer.p(loopVariable.get(0).toString() + " = ");
        visitIntegerLiteral(loopVariable.getGeneric(2));
        printer.p("; ");
        visitRelationalExpression(forControl.getGeneric(3));
        printer.p("; ");
        visitExpressionList(forControl.getGeneric(4));
        printer.p(") {");
        printer.pln().indent();
        visit(n.getGeneric(1));

        printer.indent();
        printer.pln("}");
    }

    public void visitRelationalExpression(GNode n) {
        printPrimaryIdentifier(n.getGeneric(0));
        printer.p(" " + n.get(1).toString() + " ");
        visitSelectionExpression(n.getGeneric(2));
    }

    public void visitExpressionList(GNode n) {
        GNode expression = n.getGeneric(0);
        printPrimaryIdentifier(expression.getGeneric(0));
        printer.p(expression.get(1).toString());
    }

    // Doesnt do anything but breaks everything if deleted idk
    public void visitSubscriptExpression(GNode n) {

    }

} // End of the MainPrinter class
