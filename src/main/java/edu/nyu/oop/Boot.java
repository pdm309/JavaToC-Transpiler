package edu.nyu.oop;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import edu.nyu.oop.util.JavaFiveImportParser;
import edu.nyu.oop.util.NodeUtil;
import edu.nyu.oop.util.XtcProps;
import org.slf4j.Logger;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.util.Tool;
import xtc.lang.JavaPrinter;
import xtc.parser.ParseException;

/**
 * This is the entry point to your program. It configures the user interface, defining
 * the set of valid commands for your tool, provides feedback to the user about their inputs
 * and delegates to other classes based on the commands input by the user to classes that know
 * how to handle them. So, for example, do not put translation code in Boot. Remember the
 * Single Responsiblity Principle https://en.wikipedia.org/wiki/Single_responsibility_principle
 */
public class Boot extends Tool {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    @Override
    public String getName() {
        return XtcProps.get("app.name");
    }

    @Override
    public String getCopy() {
        return XtcProps.get("group.name");
    }

    @Override
    public void init() {
        super.init();
        // Declare command line arguments.
        runtime.bool("printJavaAst", "printJavaAst", false, "Prints a single Java AST (no dependencies).").
        bool("translate", "translate", false, "Translates a single Java source file into C++").
        bool("printPhase1AST", "printPhase1AST", false, "Prints the full Java AST, including dependencies.").
                bool("mangleAST", "mangleAST", false, "Mangles the Java AST method names if overloaded").
        bool("printPhase2Data", "printPhase2Data", false, "Prints the C++ AST data.").
        bool("printPhase2AST", "printPhase2AST", false, "Prints the full pre-mutated C++ AST.").
        bool("printPhase3HeaderFile", "printPhase3HeaderFile", false, "Outputs the C++ header file to disk").
                bool("printPhase4MutatedAST", "printPhase4MutatedAST", false, "Prints the fully mutated C++ AST").
                bool("printPhase5ImplementationFile", "printPhase5ImplementationFile", false, "Outputs the C++ implementation file to disk").
                bool("printPhase5MainFile", "printPhase5MainFile", false, "Outputs the C++ main file to disk").
        bool("printJavaCode", "printJavaCode", false, "Print Java code.").
        bool("printJavaImportCode", "printJavaImportCode", false, "Print Java code for imports and package source.");
    }

    @Override
    public void prepare() {
        super.prepare();
        // Perform consistency checks on command line arguments.
        // (i.e. are there some commands that cannot be run together?)
        logger.debug("This is a debugging statement."); // Example logging statement, you may delete
    }

    @Override
    public File locate(String name) throws IOException {
        File file = super.locate(name);
        if (Integer.MAX_VALUE < file.length()) {
            throw new IllegalArgumentException("File too large " + file.getName());
        }
        if (!file.getAbsolutePath().startsWith(System.getProperty("user.dir"))) {
            throw new IllegalArgumentException("File must be under project root.");
        }
        return file;
    }

    @Override
    public Node parse(Reader in, File file) throws IOException, ParseException {
        return NodeUtil.parseJavaFile(file);
    }

    @Override
    public void process(Node n) {
        if (runtime.test("printJavaAst")) {
            runtime.console().format(n).pln().flush();
        }

        /******************** Orchestrator methods ********************/
        if (runtime.test("translate"))
            Orchestrator.translate((GNode) n);

        /******************** End Orchestrator methods ********************/


        /******************** Phase 1 methods ********************/
        if (runtime.test("printPhase1AST"))
            Phase1.printPhase1AST((GNode) n);

        /******************** End Phase 1 methods ********************/


        /******************** Phase 1.5 methods ********************/
        if (runtime.test("mangleAST"))
            MangleAST.printMangledAST((GNode) n);
        /******************** End Phase 1.5 methods ********************/


        /******************** Phase 2 methods ********************/
        if (runtime.test("printPhase2AST"))
            Phase2.printPhase2AST((GNode) n);

        if (runtime.test("printPhase2Data"))
            Phase2.printPhase2Data((GNode) n);

        /******************** End Phase 2 methods ********************/


        /******************** Phase 3 methods ********************/
        if (runtime.test("printPhase3HeaderFile"))
            Phase3.printPhase3HeaderFile((GNode) n);

        /******************** End Phase 3 methods ********************/


        /***************** Phase 4 Methods *****************/
        if (runtime.test("printPhase4MutatedAST"))
            Phase4.printMutatedAST((GNode) n);

        /***************** End Phase 4 Methods *****************/


        /***************** Phase 5 Methods *****************/
        if (runtime.test("printPhase5ImplementationFile"))
            Phase5.printImplementationFile((GNode) n);

        if (runtime.test("printPhase5MainFile"))
            Phase5.printMainFile((GNode) n);

        /***************** End Phase 5 Methods *****************/


        if (runtime.test("printJavaCode")) {
            new JavaPrinter(runtime.console()).dispatch(n);
            runtime.console().flush();
        }

        if (runtime.test("printJavaImportCode")) {
            List<GNode> nodes = JavaFiveImportParser.parse((GNode) n);
            for (Node node : nodes) {
                runtime.console().pln();
                new JavaPrinter(runtime.console()).dispatch(node);
            }
            runtime.console().flush();
        }
    } // End of processing sbt commands


    /**
     * Run Boot with the specified command line arguments.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        new Boot().run(args);
    }
}