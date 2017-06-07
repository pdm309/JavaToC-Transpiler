package edu.nyu.oop;

// xtc imports
import org.slf4j.Logger;
import xtc.tree.Node;
import xtc.tree.GNode;
import xtc.tree.Printer;
import xtc.tree.Visitor;

// Utility imports
import edu.nyu.oop.util.NodeUtil;
import edu.nyu.oop.util.XtcProps;

// General imports
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

class CPPHeaderPrinter extends Visitor {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
    private Printer printer;
    private String outputLocation = XtcProps.get("output.location");
    private boolean IS_DEBUG = false; // Set to false when correct

    CPPHeaderPrinter(String outputfile) {
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
    } // End of the CPPHeaderPrinter method

    /**************************************** Helper methods ****************************************/
    private void cout(String line) {
        printer.incr().indent().pln("cout << \"" + line + "\" << endl;").decr();
    } // End of the cout method


    /**************************************** Sectional methods ****************************************/

    // Print all the node names in an Ast
    public void visit(Node n) {
        for (Object o : n) if (o instanceof Node) dispatch((Node) o);
    }

    public void print(List<GNode> phase2ASTs, GNode mainClass) {
        GNode packageNode = mainClass.getGeneric(mainClass.size() - 1);
        headOfFile(packageNode);

        setupForwardDeclarations(phase2ASTs);
        setupTypeDefinitions(phase2ASTs);

        for (GNode phase2AST : phase2ASTs) {
            printDatalayout(phase2AST);
            printer.pln();
            printVTable(phase2AST);
            printer.pln();
        }

        tailOfFile(packageNode);
        printer.flush(); // Important!
    } // End of the print method

    private void headOfFile(GNode packageNode) {
        printer.pln("#pragma once");
        printer.pln("#include <stdint.h>");
        printer.pln("#include <string>");
        printer.pln("#include \"java_lang.h\"");
        printer.pln();

        printer.pln("using namespace java::lang;");
        printer.pln("using namespace std;");
        printer.pln();

        // Prints the package namespaces
        String deliminatedPackageName = packageNode.get(0).toString();
        String[] packageName = deliminatedPackageName.split("\\.");

        for (String aPackageName : packageName)
            printer.pln("namespace " + aPackageName + " {");
    } // End of the headOfFile method

    private void setupTypeDefinitions(List<GNode> phase2ASTs) {
        for (GNode phase2AST : phase2ASTs)
            printer.indent().pln("typedef __rt::Ptr< __" + phase2AST.get(0) + "> " + phase2AST.get(0) + ";");
        printer.pln();
    } // End of the setupTypeDefinitions method

    private void setupForwardDeclarations(List<GNode> phase2ASTs) {
        for (GNode phase2AST : phase2ASTs) {
            printer.indent().pln("struct __" + phase2AST.get(0) + ";");
            printer.indent().pln("struct __" + phase2AST.get(0) + "_VT;");
            printer.pln();
        }
    } // End of the setupForwardDeclarations method

    private void tailOfFile(GNode packageNode) {
        String deliminatedPackageName = packageNode.get(0).toString();
        String[] packageName = deliminatedPackageName.split("\\.");

        for (String aPackageName : packageName)
            printer.pln("} // End of the " + aPackageName + " namespace");
    } // End of the tailOfFile method

    /**************************************** Print methods ****************************************/

    private void printDatalayout(GNode phase2AST) {
        // Start of printing out the data layout
        GNode datalayout = (GNode) NodeUtil.dfs(phase2AST, "DataLayout");

        String className = (String) datalayout.get(0);
        // Prints the head stuff
        printer.indent().pln("// " + className + "'s data layout");
        printer.indent().pln("struct " + className + " {");
        printer.incr().incr().pln();

        boolean isFirstField = true;
        boolean isFirstConstructor = true;
        boolean isFirstInit = true;
        boolean isFirstMethod = true;

        // Iterates through the datalayout's children and prints what it finds
        for (int i = 1; i < datalayout.size(); ++i) { // We start at 0 since it's the data layout name
            GNode datalayoutChild = (GNode) datalayout.get(i);
            switch (datalayoutChild.getName()) {
                case "FieldDeclaration":
                    String fieldName = datalayoutChild.getString(0);

                    if (isFirstField) {
                        isFirstField = false;
                        printer.indent().pln("// " + className + "'s fields");
                    }
                    else if (fieldName.startsWith("__vtable"))
                        printer.pln().indent().pln("// The vtable for " + className);

                    printFieldDatalayout(datalayoutChild);
                    break;
                case "ConstructorDeclaration":
                    if (isFirstConstructor) {
                        isFirstConstructor = false;
                        printer.pln().indent().pln("// " + className + "'s constructor");
                    }
                    printConstructorDatalayout(datalayoutChild);
                    break;
                case "MethodDeclaration":
                    String methodName = (String) datalayoutChild.get(0);

                    if (methodName.startsWith("__init") && isFirstInit) {
                        // Deals with class init methods
                        isFirstInit = false;
                        printer.pln().indent().pln("// " + className + "'s __init methods");
                    }
                    else if (isFirstMethod) {
                        // Deals with the rest of the methods
                        isFirstMethod = false;
                        printer.pln().indent().pln("// " + className + "'s methods");
                    }
                    else if (methodName.startsWith("__class"))
                        printer.pln().indent().pln("// Function returning class object representing " + className);

                    printMethodDatalayout(datalayoutChild);
                    break;
            }
        } // End of iterating through the datalayout children

        printer.decr().decr().pln();
        // Prints the tail stuff
        printer.indent().pln("};"); // End of the class data layout
    } // End of the printDatalayout method

    private void printFieldDatalayout(GNode fieldNode) {
        // Node 0: name
        // Node 1: Type node
        // Node 2: Modifiers node
        printer.indent();

        GNode modifiers = fieldNode.getGeneric(2);
        boolean isFirstTime = true;
        //if there is at least one modifier, print it with the proper formatting before the field
        if (modifiers.size() > 0){
            for (int i = 0; i < modifiers.size(); ++i) {
                if (isFirstTime)
                    isFirstTime = false;
                else
                    printer.p(" ");

                printer.p(modifiers.getString(i));
                if (!modifiers.getString(i).equals("static"))
                    printer.p(":");
                printer.p(" ");
//                printer.p("\n");
//                printer.indent();
//                printer.indent();
            }
        }


        // Prints the type
        GNode fieldType = fieldNode.getGeneric(1);
        printer.p(fieldType.getGeneric(0).getString(0)); // Type
        if (fieldType.getString(1) != null)
            printer.p("[]"); //Dimension

        // Prints the field name
        printer.p(" ").p(fieldNode.getString(0));

        printer.pln(";").flush();
    } // End of the printFieldDataLayout method

    private void printConstructorDatalayout(GNode constructorNode) {
        // Node 0: name
        // Node 1: Modifiers node
        // Node 2: Parameters node
        // Node 3: source

        GNode constructorModifiers = (GNode) constructorNode.get(1);
        printer.indent();
        //if there is at least one modifier, print it with the proper formatting before the constructor
        if (constructorModifiers.size() > 0){
            for (int i = 0; i < constructorModifiers.size(); ++i) {
                printer.p(constructorModifiers.getString(i));
                printer.p(":");
                printer.p("\n");
                printer.indent();
                printer.indent();
            }

        }


        // Adds the method name
        printer.p((String) constructorNode.get(0));

        // Adds the parameter types
        printer.p("(");
        GNode constructorParameters = (GNode) constructorNode.get(2);

        boolean isFirstTime = true;
        boolean isFirstPrinted = false;
        for (int i = 0; i < constructorParameters.size(); ++i) {
            GNode parameter = (GNode) constructorParameters.get(i);
            GNode typeNode = (GNode) parameter.get(1);

            if (isFirstTime) { // Makes sure we don't print the self object for constructor
                isFirstTime = false;
                continue;
            }

            // Prints deliminator between parameters
            if (isFirstPrinted)
                printer.p(", ");
            else
                isFirstPrinted = true;

            // Adds the parameter type
            printer.p((String) typeNode.get(0));

        } // End of iterating through all constructor parameters
        printer.pln(");");
    } // End of the printConstructorDatalayout method

    private void printMethodDatalayout(GNode method) {
        // Node 0: name
        // Node 1: Return Type node
        // Node 2: Modifiers node
        // Node 3: Parameters node
        // Node 4: source

        // Adds the modifiers
        GNode modifiers = (GNode) method.get(2);
        if (modifiers.get(0).toString().equals("public") || modifiers.get(0).toString().equals("private")) {
            printer.indent().p(modifiers.get(0).toString() + ":");

            for (int i = 1; i < modifiers.size(); i++)
                printer.p(modifiers.get(i).toString());
        }

        else {
            for (int i = 0; i < modifiers.size(); ++i)
                printer.indent().p(modifiers.get(i).toString());
        }


        // Adds the return type
        GNode returnType = (GNode) method.get(1);
        if (returnType.get(0) != null)
            printer.p(" " + returnType.get(0));

        // Adds the method name
        printer.p(" " + method.get(0));

        // Adds the parameter types
        printer.p("(");
        GNode parameters = (GNode) method.get(3);

        boolean isFirstTime = true;
        for (int i = 0; i < parameters.size(); ++i) {
            GNode parameter = (GNode) parameters.get(i);
            GNode typeNode = (GNode) parameter.get(1);
            if (isFirstTime)
                isFirstTime = false;
            else
                printer.p(", ");

            // Adds the parameter type
            printer.p(typeNode.getGeneric(0).getString(0));

        } // End of iterating through all parameters
        printer.pln(");");
    } // End of the printMethodDatalayout method

    private void printVTable(GNode phase2AST) {
        // Class AST children layout
        // Node 0: Class name (String!)
        // Node 1: Package node
        // Node 2: DataLayout node
        // Node 3: VTable node
        // Node 4: Parent node
        // Node 5: Implementation node

        // VTable children layout
        // Node 0: vtable name (String!)
        // Node 1: dyanmic type node
        // Node 2 -> 5: object's methods
        // Node 6+: class methods (non-static)

        // Start of printing out the vtable
        GNode vTable = phase2AST.getGeneric(3);

        // Prints the head stuff
        String className = (String) phase2AST.get(0);
        printer.indent().pln("// " + className + "'s vtable");
        printer.indent().pln("struct " + vTable.get(0) + " {");
        printer.incr().incr();

        // Prints out the fields (basically just the dynamic type)
        List<Node> fields = NodeUtil.dfsAll(vTable, "FieldDeclaration");
        for (Node field : fields) {
            // Field node children layout
            // Node 0: Field name (String!)
            // Node 1: Field Type node
            // Node 2: Field Modifiers node

            // Prints the field type
            GNode type = field.getGeneric(1);
            printer.indent().p(type.getGeneric(0).getString(0));
            printer.p(" ").p((String) type.get(1));

            // Prints the field name
            printer.p(field.getString(0));

            printer.pln(";");
        }
        printer.pln();

        // Prints out the method declarations (public and non-static only)
        List<Node> methods = NodeUtil.dfsAll(vTable, "MethodDeclaration");
        for (Node method : methods) {
            // Method node children layout
            // Node 0: Method name (String!)
            // Node 1: Method Return Type node
            // Node 2: Method Modifiers node (no children here!)
            // Node 3: Method Parameters node
            // Node 4: Method Source node
            // Node 5: Method Block node (no children here!)

            String methodName = method.getString(0);
            GNode methodParameters = method.getGeneric(3);

            // Prints the method return type
            GNode returnTypeParentNode = method.getGeneric(1);
            printer.indent().p(returnTypeParentNode.getString(0));
            printer.p(" (*").p(methodName).p(")");
            printer.p("(");
            printMethodParameterTypes(methodParameters);

            if (methodName.equals("__delete")) {
                printer.pln("*);");
            }else {
                printer.pln(");");
            }

        } // End of printing method declarations
        printer.pln();

        // Prints out the initialisation list
        printer.indent().p((String) vTable.get(0)).pln("()");
        printer.incr().incr();

        // Adds the isa class
        printer.indent().p(": ");
        printer.p((String) fields.get(0).get(0));
        printer.p("(__").p((String) phase2AST.get(0)).p("::__class()").p("),");
        printer.incr();
        printer.pln();

        // Adds the initialisation list methods
        for (int i = 0; i < methods.size(); ++i) {
            // Method node children layout
            // Node 0: Method name (String!)
            // Node 1: Method Return Type node
            // Node 2: Method Modifiers node (no children here!)
            // Node 3: Method Parameters node
            // Node 4: Method Source node
            // Node 5: Method Block node (no children here!)

            Node method = methods.get(i);
            GNode sourceNode = method.getGeneric(4);
            GNode returnTypeNode = method.getGeneric(1);
            GNode methodParameters = method.getGeneric(3);

            String methodName =  method.getString(0);

            printer.indent().p(method.getString(0));
            printer.p("(");

            if (methodName.equals("__delete")) {
                printer.p("&__rt::");
                printer.p(method.getString(0));
                printer.p("<");
                printMethodParameterTypes(methodParameters);
                printer.p(">");

            }else {
                if (sourceNode.get(0).equals(phase2AST.get(0))) {
                    // Method is implemented in this class
                    printer.p("&__");
                    printer.p((String) sourceNode.get(0));
                    printer.p("::");
                    printer.p((String) method.get(0));
                } else {
                    // Method is inherited from a super class


                    printer.p("(");

                    // Prints the return class
                    printer.p((String) returnTypeNode.get(0));
                    printer.p("(*)");


                    // Prints the method parameters
                    printer.p("(");
                    printMethodParameterTypes(methodParameters);
                    printer.p(")");

                    printer.p(")");

                    printer.flush();
                    // Prints the class the method is from
                    printer.p(" &__");

                    printer.p((String) sourceNode.get(0));
                    printer.p("::");
                    printer.p((String) method.get(0));
                }
            }
            printer.p(")");
            if (i != methods.size() -1)
                printer.pln(",");
            else
                printer.pln(" {");
        }
        printer.decr().decr().decr().indent().pln("}");

        printer.decr().decr().pln();
        // Prints the tail stuff
        printer.indent().pln("};"); // End of the class vtable
    } // End of the printVTable method

    private void printMethodParameterTypes(GNode parameters) {
        // Parameter node children layout
        // Node 0: Parameter name (String!)
        // Node 1: Parameter Type node
        // Node 2: Parameter Modifiers

        boolean isFirstTime = true;
        for (int i = 0; i < parameters.size(); ++i) {
            GNode parameter = parameters.getGeneric(i);
            if (isFirstTime)
                isFirstTime = false;
            else
                printer.p(", ");

            GNode parameterTypeParent = parameter.getGeneric(1);
            printer.p(parameterTypeParent.getGeneric(0).getString(0));
        }
    } // End of the printMethodParameterTypes method
} // End of the CPPHeaderPrinter class



