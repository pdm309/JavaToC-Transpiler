package edu.nyu.oop;

// xtc imports
import org.slf4j.Logger;
import xtc.tree.GNode;

// General imports
import java.util.List;

// General imports

public class Phase3 {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(Phase3.class);
    private static boolean IS_DEBUG = false; // Set to false when correct

    ////////////////////////////// SBT methods //////////////////////////////

    // Called directly through sbt, not the orchestrator
    public static void printPhase3HeaderFile(GNode n) {
        List<GNode> phase2ASTs = Phase2.generateASTs(n);
        GNode mainClassNode = Phase2.getMainClassNode(n);
        outputHeaderFileFromPhase2(phase2ASTs, mainClassNode);
    } // End of the printPhase3HeaderFile

    ////////////////////////////// Generate methods //////////////////////////////

    static void outputHeaderFileFromPhase2(List<GNode> phase2ASTs, GNode mainClassNode) {
        CPPHeaderPrinter printer = new CPPHeaderPrinter("output.h");
        printer.print(phase2ASTs, mainClassNode);
    } // End of the outputDefaultHeaderFileFromPhase2
} // End of the Phase3 class