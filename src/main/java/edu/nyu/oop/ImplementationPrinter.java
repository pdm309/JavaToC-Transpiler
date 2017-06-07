package edu.nyu.oop;

// xtc imports
import edu.nyu.oop.util.PrintUtilities;
import edu.nyu.oop.util.SymbolTableUtil;
import org.slf4j.Logger;
import xtc.tree.*;

// Utility imports
import edu.nyu.oop.util.XtcProps;
import xtc.type.Type;
import xtc.util.SymbolTable;

// General imports
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import xtc.util.SymbolTable.Scope;
import java.util.List;

public class ImplementationPrinter extends Visitor {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
    private Printer printer;
    private String outputLocation = XtcProps.get("output.location");
    private boolean IS_DEBUG = false;
    private SymbolTable table;

    public ImplementationPrinter(String outputfile) {
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
    } // End of the ImplementationPrinter method

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
            default:
                // TODO finish this off
                logger.debug("TODO need to finish off for most other method calls " + n.getName());
        }
    } // End of the callVisitFunction method

    /**************************************** Helper methods ****************************************/

    private void buildSymbolTable(GNode someClass) {
       SymbolTableBuilder tableBuilder = new SymbolTableBuilder();
        this.table = tableBuilder.getTable(someClass);
    } // End of the buildSymbolTable method

    /**************************************** Sectional methods ****************************************/
    public void print(List<GNode> phase4ASTs, GNode mainClass) {
        GNode packageNode = mainClass.getGeneric(1);

        // Prints the start of the file
        headOfFile(packageNode);

        // Prints out all class methods and constructors
        printer.incr().incr();
        for (GNode phase4AST : phase4ASTs) {
            if (IS_DEBUG)
                logger.debug("-------------------- Printing class: " + phase4AST.getString(0) + " --------------------\n" + phase4AST.toString());

            buildSymbolTable(phase4AST);
            dispatch(phase4AST);
        }
        printer.decr().decr();

        // Prints the end of the implementations
        tailOfFile(packageNode);
        printer.pln();

        // Prints the array specialisation
        printArraySpecialisation(phase4ASTs);

        printer.flush(); // Important!
    } // End of the print method

    private void headOfFile(GNode packageNode) {
        printer.pln("#include \"output.h\"");
        printer.pln("#include \"java_lang.h\"");
        printer.pln("#include <iostream>");
        printer.pln();
        printer.pln("using namespace java::lang;");
        printer.pln("using namespace std;");
        printer.pln();

        // Prints the package namespaces
        List<String> packageNames = PrintUtilities.getPackageList(packageNode);
        for (String packageName : packageNames)
            printer.pln("namespace " + packageName + " {");
    } // End of the headOfFile method

    private void tailOfFile(GNode packageNode) {
        List<String> packageNames = PrintUtilities.getPackageList(packageNode);
        for (String packageName : packageNames)
            printer.pln("} // End of the " + packageName + " namespace");
    } // End of the tailOfFile method


    /**************************************** Visit methods ****************************************/
    public void visit(Node n) {
        for (Object o : n) if (o instanceof Node) {
//            cout(((Node) o).getName());
            dispatch((Node) o);}
    }


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
                if(argumentsNode.getGeneric(0).getName().equals("CallExpression"))
                {
                    String identifier = argumentsNode.getGeneric(0).getGeneric(0).getString(0);
                    printer.pln("__rt::checkNotNull("+identifier+");");
                    printer.indent();
                }
                printer.p(PrintUtilities.jPrintln(argumentsNode));
                break;
            case "System::out::print":
                if(argumentsNode.getGeneric(0).getName().equals("CallExpression"))
                {
                    String identifier = argumentsNode.getGeneric(0).getGeneric(0).getString(0);
                    printer.pln("__rt::checkNotNull("+identifier+");");
                    printer.indent();
                }
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

    public void visitExpression(GNode n) {
        if (IS_DEBUG)
            logger.debug("\tEntering Expression");

        SymbolTableUtil.enterScope(table, n);

        Scope methodScope = table.current().getParent().getParent().getParent();

        if (n.get(0) instanceof Node) {
            switch (n.getGeneric(0).getName()) {
                case "PrimaryIdentifier":
                    if (IS_DEBUG)
                        logger.debug("Expression is a primary identifier");

                    GNode primaryIdentifier = n.getGeneric(0);
                    GNode primaryIdentifier2 = n.getGeneric(2);

                    //get symbol to find details on PrimaryIdentifier such as a private class variable
                    //getting the PrimaryIdentifier child node containing the returned value/variable

                    if (methodScope.lookup(primaryIdentifier.getString(0)) == null) {
                        // Cannot be found in the current scope, so use __this
                        printer.p("__this->");
                    }
                    else {
                        // identifer was found in the current scope, no need to use __this
                    }
                    printPrimaryIdentifier(primaryIdentifier);
                    printer.p(" " + n.getString(1) + " "); // Prints the '=' or other operator

                    if (primaryIdentifier2.getName().equals("PrimaryIdentifier")) {
                        if (methodScope.lookup(primaryIdentifier2.getString(0)) == null) {
                            // Cannot be found in the current scope, so use __this
                            printer.p("__this->");
                        }
                        else {
                            // identifer was found in the current scope, no need to use __this
                            printPrimaryIdentifier(primaryIdentifier2);
                        }
                    }
                    else if (primaryIdentifier2.getName().equals("ThisExpression")) {
                        printer.p("__this");
                    }
                    else {
                        // Not an identifier, logs and should be filled in
                        logger.debug("TODO: Add to ImplementationPrinter::visitExpression: " + primaryIdentifier2.getName());
                        dispatch(n.getGeneric(2));
                    }

                    break;
                case "ThisExpression":
                    logger.debug("THIS________" + n.get(0).toString());
                    visit(n);
                    printer.p(" " + n.get(1).toString() + " ");
                    printPrimaryIdentifier(n.getGeneric(2));
                    break;
                case "SelectionExpression":


                    dispatch(n.getGeneric(0));
                    printer.p(" " + n.getString(1) + " "); // Prints the '=' or other operator

                    if (methodScope.lookup(n.getGeneric(2).getString(0)) == null) {
                        // Cannot be found in the current scope, so use __this
                        printer.p("__this->");
                    }
                    else {
                        // identifer was found in the current scope, no need to use __this
                    }

                    if (n.getGeneric(2).getName().equals("PrimaryIdentifier"))
                        printPrimaryIdentifier(n.getGeneric(2));
                    else
                        dispatch(n.getGeneric(2));

                    // Prints the operator

                    break;
                default:
                    logger.debug("TODO: This needs to be added to ImplementationPrinter::visitExpression: " + n.getGeneric(0).getName() + "\n" + n.getGeneric(0).toString());
            } // End of the switch statement
        } // End of checking if it's a node

        SymbolTableUtil.exitScope(table, n);
        if (IS_DEBUG)
            logger.debug("\tExiting Expression");
    } // End of the visitExpression method

    public void visitExpressionInConstructor(GNode n) {
        if (IS_DEBUG)
            logger.debug("\tEntering Expression");

        SymbolTableUtil.enterScope(table, n);

        if (n.get(0) instanceof Node) {
            switch (n.getGeneric(0).getName()) {
                case "PrimaryIdentifier":
                    if (IS_DEBUG)
                        logger.debug("Expression is a primary identifier");

                    GNode primaryIdentifier = n.getGeneric(0);
                    GNode primaryIdentifier2 = n.getGeneric(2);

                    //get symbol to find details on PrimaryIdentifier such as a private class variable
                    //getting the PrimaryIdentifier child node containing the returned value/variable

                    Scope methodScope = table.current().getParent().getParent().getParent();

                    if (methodScope.lookup(primaryIdentifier.getString(0)) == null) {
                        // Cannot be found in the current scope, so use __this
                        printer.p("this->");
                    }
                    else {
                        // identifer was found in the current scope, no need to use __this
                    }
                    printPrimaryIdentifier(primaryIdentifier);
                    printer.p(" " + n.getString(1) + " "); // Prints the '=' or other operator

                    if (primaryIdentifier2.getName().equals("PrimaryIdentifier")) {
                        if (methodScope.lookup(primaryIdentifier2.getString(0)) == null) {
                            // Cannot be found in the current scope, so use __this
                            printer.p("this->");
                        }
                        else {
                            // identifer was found in the current scope, no need to use __this
                            printPrimaryIdentifier(primaryIdentifier2);
                        }
                    }
                    else {
                        // Not an identifier, logs and should be filled in
                        logger.debug("TODO: Add to ImplementationPrinter::visitExpressionInConstructor: " + primaryIdentifier2.getName());
                        dispatch(n.getGeneric(2));
                    }

                    break;
                case "ThisExpression":
                    logger.debug("THIS________" + n.get(0).toString());
                    visit(n);
                    printer.p(" " + n.get(1).toString() + " ");
                    printPrimaryIdentifier(n.getGeneric(2));
                    break;
                default:
                    logger.debug("TODO: This needs to be added to ImplementationPrinter::visitExpression: " + n.getGeneric(0).getName());
            } // End of the switch statement
        } // End of checking if it's a node

        SymbolTableUtil.exitScope(table, n);
        if (IS_DEBUG)
            logger.debug("\tExiting Expression");
    } // End of the visitExpressionInConstructor method


    public void visitCallExpression(GNode n) {
        if (IS_DEBUG)
            logger.debug("\tEntering call expression");

        // Generates what the function name is

        // NOTE: We have two cases here: when it has a selectionExpression, and when it doesn't
        List<String> functionList;
        if (n.getGeneric(0) != null && n.getGeneric(0).getName().equals("SelectionExpression")) {
            functionList = (List<String>) dispatch(n.getGeneric(0));
            // Adds the name of the function
            functionList.add(n.getString(2));

            if (IS_DEBUG)
                logger.debug("\tExiting call expression");

            // Prints the function call
            printFunctionCall(functionList, n.getGeneric(3), n.getGeneric(0).getGeneric(0));

        }

        else {
            functionList = new ArrayList<>();

            // Adds the name of the function
            functionList.add(n.getString(2));

            if (IS_DEBUG)
                logger.debug("\tExiting call expression");

            // Prints the function call
            printFunctionCall(functionList, n.getGeneric(3), null);
        }

    } // End of the visitCallExpression method




    public void visitReturnStatement(GNode statement) {
        if (IS_DEBUG)
            logger.debug("\tPrinting out ReturnStatement");


        printer.indent().p("return ");
        //get symbol to find details on PrimaryIdentifier such as a private class variable
        //getting the PrimaryIdentifier childnode containing the returned value/variable
        String statementType = statement.getGeneric(0).getName();
        boolean local;
        switch (statementType){
            //if the return statement is calling another method
            case "CallExpression":
                //if that method is local or of a parent class
                local = table.current().isDefinedLocally(statement.getGeneric(0).getGeneric(0).get(0).toString());
                if (local){
                    //if the method is local
                    printer.p(statement.getGeneric(0).getString(2) + "(" + statement.getGeneric(0).getGeneric(3).toString() + ")");
                }
                else {
                    //if the method is of a parent class
                    printer.p("__this->" + statement.getGeneric(0).getGeneric(0).get(0).toString() + "->__vptr->");
                    printer.p(statement.getGeneric(0).getString(2) + "(__this->" + statement.getGeneric(0).getGeneric(0).get(0).toString() + ")");
                }
                break;
            //other possible return statements
            //TODO: Might need more switch cases for other types of return values
            default:
                local = table.current().isDefinedLocally(statement.getGeneric(0).getString(0));
                if (local || statement.getGeneric(0).getString(0).contains("\"")) {
                    //not local or not even a variable (could be a string literal)
                } else {
                    //variable that isn't locally defined: class variable needing of __this->
                    printer.p("__this->");
                }
                break;
        }
//        while (symbols.hasNext()){
//            logger.debug("\nTHIS IS OUR SYMBOL?:" + symbols.next());
//        }
        //String symbol = symbols.next();

//        if (symbol.contains(("__this"))){
//            logger.debug("HOLY SHIT");
//            printer.p(symbol + "->");
//        }
        //logger.debug(scope.getName());
        for (int i = 0; i < statement.size(); ++i) {
            if (PrintUtilities.getVisitFunctionString(statement.getGeneric(i)) != null)
                printer.p(PrintUtilities.getVisitFunctionString(statement.getGeneric(i)));
        }

        printer.pln(";");
        if (IS_DEBUG)
            logger.debug("\tEnd Printing out ReturnStatement");
    } // End of the visitReturnStatement method



    public List<String> visitSelectionExpression(GNode n) {
        List<String> selectionExpression = new ArrayList<>();

        if (IS_DEBUG)
            logger.debug("Entering Selection Expression: " + n.toString());

        String dispatched = (String) dispatch(n.getGeneric(0));

        if (dispatched != null)
            selectionExpression.add(dispatched);

        if (n.getString(1) != null)
            selectionExpression.add(n.getString(1));

        if (n.get(0).toString().contains("ThisExpression"))
            printer.p(n.get(1).toString());

        else
            visit(n);


        if (IS_DEBUG)
            logger.debug("Exiting Selection Expression: " + n.toString());

        return selectionExpression;
    } // End of the visitSelectionExpression method


    public void visitArguments(GNode n) {
        for (int i = 0; i < n.size(); ++i) {
            if (i > 0)
                printer.p(", ");
            dispatch(n.getGeneric(i));
        }
    } // End of the visitArguments method

    public void visitExpressionStatementInConstructor(GNode n) {
        if (IS_DEBUG)
            logger.debug("\tEntering expression statement");

        printer.indent();

        for (int i = 0; i < n.size(); ++i)
            visitExpressionInConstructor(n.getGeneric(i));

        printer.pln(";");

        if (IS_DEBUG)
            logger.debug("\tExiting expression statement");
    } // End of the visitExpressionStatement

    public void visitExpressionStatement(GNode n) {
        if (IS_DEBUG)
            logger.debug("\tEntering expression statement");

        SymbolTableUtil.enterScope(table, n);
        printer.indent();
        visit(n);

        printer.pln(";");

        SymbolTableUtil.exitScope(table, n);
        if (IS_DEBUG)
            logger.debug("\tExiting expression statement");
    } // End of the visitExpressionStatement

    public void visitThisExpression(GNode n) {
        printer.p("__this->");
    }

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





    public void visitClass(GNode phase4AST) {
        String className = phase4AST.getString(0);
        String packageName = phase4AST.getGeneric(1).get(0).toString();
        GNode parentNode = phase4AST.getGeneric(4);
        GNode implementationNode = phase4AST.getGeneric(5);

        printer.pln().indent().pln("/******************** Implementation of " + className + " ********************/");
        SymbolTableUtil.enterScope(table, phase4AST);
        visit(implementationNode);
        SymbolTableUtil.exitScope(table, phase4AST);

        // Prints the __class method TODO convert this into an actual method in phase 2
        printer.pln();
        printer.indent().pln("// " + className + "'s __class method");
        printer.indent().pln("Class __" + className + "::__class() {");
        printer.incr().incr();

        printer.indent().pln("static Class k =");
        printer.incr().incr();

        printer.indent().p("new __Class(__rt::literal(\"");

        printer.p(packageName + "." + className + "\"), __");

        if (parentNode.getGeneric(0) == null)
            printer.p("Object");
        else
            printer.p(parentNode.getGeneric(0).getString(0));

        printer.pln("::__class());");
        printer.incr().incr();

        printer.decr().decr();
        printer.decr().decr();
        printer.indent().pln("return k;");

        printer.decr().decr();
        printer.indent().pln("} // End of " + className + "'s __class method");

        // Prints the vtable
        printer.pln();
        printer.indent().pln("__" + className + "_VT __" + className + "::__vtable;");

    } // End of the printClass method

    public void visitMethodDeclaration(GNode method) {
        // Method children layout
        // Node 0: Name (String!)
        // Node 1: Return Type
        // Node 2: Modifiers
        // Node 3: Parameters
        // Node 4: Method source (String!)
        // Node 5: Block (actual implementation)

        SymbolTableUtil.enterScope(table, method);

        // Prints the method signature
        printer.indent();

        // Prints the return type
        GNode returnTypeNode = method.getGeneric(1);
        printer.p(returnTypeNode.getString(0));
        if (returnTypeNode.get(1) != null)
            printer.p("[]");
        printer.p(" ");

        // Prints which class it is from
        printer.p(method.getString(4));
        printer.p("::");

        // Prints the method name
        String methodName = method.getString(0);
        printer.p(methodName);

        // Prints out any method parameters
        printer.p("(");
        GNode parametersNode = method.getGeneric(3);
        for (int i = 0; i < parametersNode.size(); ++i) {
            if (i > 0)
                printer.p(", ");

            GNode parameter = parametersNode.getGeneric(i);

            // Prints the parameter type
            printer.p(parameter.getGeneric(1).getGeneric(0).getString(0));
            if (parameter.getGeneric(1).get(1) != null)
                printer.p("[]"); // Adds in the dimension if applicable

            // Prints the parameter name
            printer.p(" " + parameter.get(0));
        }
        printer.p(")");

        GNode block = method.getGeneric(5);
        if (block.size() == 0)
            printer.p(" {}");
        else {
            printer.pln(" {");
            printer.incr().incr();
            visit(block);
            printer.decr().decr();
            printer.indent().pln("} // End of the " + methodName + " implementation");
        }
        printer.pln();

        SymbolTableUtil.exitScope(table, method);
    } // End of the printMethod method

    public void visitConstructorDeclaration(GNode constructor) {
        // Constructor children layout
        // Node 0: Name (String!)
        // Node 1: Modifiers
        // Node 2: Parameters
        // Node 3: Source
        // Node 4: Block (actual implementation)

        SymbolTableUtil.enterScope(table, constructor);

        // Prints which class it is from
        printer.indent().p(constructor.getString(0));
        printer.p("::");

        // Prints the constructor name
        printer.p(constructor.getString(0));
        printer.p("() : ");

        // Prints the initialiser list for the vtable
        printer.p("__vptr(&__vtable) ");

        // Prints out the constructor block to initialise class fields if there are any
        GNode block = constructor.getGeneric(4);
        if (block.size() != 0) {
            printer.pln("{");
            printer.incr().incr();

            // We call any expression statements here explicitly to get "this" instead of "__this"
            logger.debug(block.toString());
            for (int i = 0; i < block.size(); ++i)
                visitExpressionStatementInConstructor(block.getGeneric(i));

            printer.decr().decr();
            printer.indent().pln("}");
        }
        else {
            // Nothing required to print the block
            printer.pln("{}");
        }
        printer.pln();

        SymbolTableUtil.exitScope(table, constructor);
    } // End of the printConstructor method

    public void visitBlock(GNode n) {
        if (IS_DEBUG)
            logger.debug("\tPrinting out Block");

        SymbolTableUtil.enterScope(table, n);
        visit(n);
        SymbolTableUtil.exitScope(table, n);

        if (IS_DEBUG)
            logger.debug("\tEnd printing out Block");
    } // End of the visitBlock method


    /**************************************** Array specialisation methods ****************************************/

    private void printArraySpecialisation(List<GNode> phase4ASTs) {
        printer.pln("namespace __rt").pln("{");
        printer.incr().incr();

        for (GNode phase4AST : phase4ASTs) {
            String className = phase4AST.getString(0);
            if (IS_DEBUG)
                logger.debug("-------------------- Printing array specialisation for class: " + className + " --------------------");

            printer.pln();
            printer.indent().pln("// " + phase4AST.getString(0) + "'s array specialization");
            printer.indent().pln("template<>");
            printer.indent().p("java::lang::Class Array<");

            PrintUtilities.printPackageName(phase4AST.getGeneric(1), "::", printer);
            printer.pln("::" + className + ">::__class() {");
            printer.incr().incr();

            printer.indent().pln("static java::lang::Class k =");
            printer.incr().incr();

            printer.indent().p("new java::lang::__Class(literal(\"[L");

            PrintUtilities.printPackageName(phase4AST.getGeneric(1), ".", printer);
            printer.pln("." + className + ";\"),");

            printer.incr().incr();

            // Prints the superclass name
            printArraySpecialisationClassName(phase4AST.getGeneric(4).getGeneric(0));
            printer.pln(",");

            // Prints the current class name
            printArraySpecialisationClassName(phase4AST);
            printer.pln(");");

            printer.decr().decr();
            printer.decr().decr();
            printer.indent().pln("return k;");

            printer.decr().decr();
            printer.indent().pln("}");
        } // End of looping through all classes

        printer.decr().decr();
        printer.pln("} // End of the runtime namespace");

    } // End of the printArraySpecialisation method

    private void printArraySpecialisationClassName(GNode classNode) {
        // Class AST children layout
        // Node 0: Class name (String!)
        // Node 1: Package node
        // Node 2: DataLayout node
        // Node 3: VTable node
        // Node 4: Parent node
        // Node 5: Implementation node

        printer.indent();
        if (classNode == null)
            printer.p("java::lang::__Object::__class()");
        else {
            PrintUtilities.printPackageName(classNode.getGeneric(1), "::", printer);
            printer.p("::").p("__").p(classNode.getString(0));
            printer.p("::__class()");
        }
    } // End of the printArraySpecialisationClassName method
























    ///////////////TODO
    ///////////////TODO
    ///////////////TODO
    ///////////////TODO
    ///////////////TODO
    ///////////////TODO Everything below this line is shit that needs to be cleaned up
    ///////////////TODO
    ///////////////TODO



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
                    String argumentName = n.getGeneric(i).getString(0);

//                    // TODO we need to figure out why the symbol table fails here
//                    Type type = (Type) table.current().lookup(argumentName);
////
////                    // TODO remove This shows that we're inside of the right method scope
//                    logger.debug("\nCurrent method scope:\n" + table.current().getName());
////
////                    // TODO remove this shows that the right method scope doesn't contain the name
//                    logger.debug("\nCurrent:\n" + type.toString());
////
//
//                    for (Iterator<String> iter = table.current().symbols(); iter.hasNext();) {
//                        String current = iter.next();
//                        logger.debug("\nCurrent: " + current);
//                    }
////
////                    logger.debug("\nSymbol table dump:\n");
////                    table.current().dump(printer);
////                    logger.debug("\nEnd symbol table dump:\n");
//
//
//                    if (type == null) logger.debug("\nType is null"); else logger.debug("\nType is not null");
//
//                    if (type != null)
//                        logger.debug("\nType:\n" + type.toString() + "\n");
//
//                    String extractedClass = PrintUtilities.extractClass(type);
//                    if (declaredType != null && !declaredType.getString(0).substring(2).equals(extractedClass))
//                        printer.p("(").p(declaredType.getString(0).substring(2)).p(") ");

//                    printer.p(argumentName);
                    break;
                default:
                    logger.debug("TODO: Add to switch statement in MainPrinter::visitArgument: " + n.getGeneric(i).getName());
            }

            callVisitFunction(n.getGeneric(i), declaredType);
        }
    } // End of the visitArguments method



} // End of the MainPrinter class
