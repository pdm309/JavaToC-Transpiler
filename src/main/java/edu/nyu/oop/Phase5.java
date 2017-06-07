package edu.nyu.oop;

// xtc imports
import org.slf4j.Logger;
import xtc.tree.GNode;

// Utility imports

// General imports
import java.util.List;

public class Phase5 {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
    private static boolean IS_DEBUG = false; // Set to false when correct

    ////////////////////////////// SBT methods //////////////////////////////

    // Called directly through sbt, not the orchestrator
    public static void printImplementationFile(GNode n) {
        List<GNode> phase4ASTs = Phase4.mutateImplementations(n);
        GNode mainClassNode = Phase2.getMainClassNode(n);
        GNode mutatedMainClass = Phase4.convertMain(mainClassNode);
        outputImplementationFileFromPhase4(phase4ASTs, mutatedMainClass);
    } // End of the printImplementationFile method

    // Called directly through sbt, not the orchestrator
    public static void printMainFile(GNode n) {
        List<GNode> mangledASTs = MangleAST.generateMangledASTs(n);
        GNode mainClass = Phase2.getMainClassNode(Phase2.generateData(mangledASTs));
        GNode mutatedMainClass = Phase4.convertMain(mainClass);
        outputMainFileFromMutatedMain(mutatedMainClass);
    } // End of the printImplementationFile method

    ////////////////////////////// Generate methods (implementation file) //////////////////////////////

    // Called from orchestrator
    static void outputImplementationFileFromPhase4(List<GNode> phase4ASTs, GNode mainMethodNode, String filePath) {
        ImplementationPrinter printer = new ImplementationPrinter(filePath);
        printer.print(phase4ASTs, mainMethodNode);
    } // End of the outputImplementationFileFromPhase4 method

    // Called from orchestrator
    static void outputImplementationFileFromPhase4(List<GNode> phase4ASTs, GNode mainMethodNode) {
        ImplementationPrinter printer = new ImplementationPrinter("output.cpp");
        printer.print(phase4ASTs, mainMethodNode);
    } // End of the outputImplementationFileFromPhase4 method

    ////////////////////////////// Generate methods (main file) //////////////////////////////

    // Called from orchestrator
    static void outputMainFileFromMutatedMain(GNode mainMethodNode, String filePath) {
        MainPrinter printer = new MainPrinter(filePath);
        printer.print(mainMethodNode);
    } // End of the outputMainFileFromPhase4 method

    // Called from orchestrator
    static void outputMainFileFromMutatedMain(GNode mainMethodNode) {
        MainPrinter printer = new MainPrinter("main.cpp");
        printer.print(mainMethodNode);
    } // End of the outputMainFileFromPhase4 method

} // End of the Phase5 class