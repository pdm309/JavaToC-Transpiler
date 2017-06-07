package edu.nyu.oop;

// xtc imports
import org.slf4j.Logger;
import xtc.tree.GNode;

// Utility imports
import edu.nyu.oop.util.NodeUtil;

// General imports
import java.util.List;

public class Phase4 {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(Phase4.class);
    private static boolean IS_DEBUG = false; // Set to false when correct

    ////////////////////////////// SBT methods //////////////////////////////

    static void printMutatedAST(GNode n) {
        logger.debug(generateStringAST(n));
    } // End of the printPhase4MutatedAST method

    ////////////////////////////// String methods //////////////////////////////

    static String generateStringAST(GNode n) {
        List<GNode> phase2ASTs = Phase2.generateASTs(n);
        return generateStringAST(phase2ASTs);
    } // End of the generateStringAST method

    static String generateStringAST(List<GNode> phase2ASTs) {
        List<GNode> phase4ASTs = mutateImplementations(phase2ASTs);
        StringBuilder sb = new StringBuilder();

        for (GNode phase4AST : phase4ASTs)
            sb.append(NodeUtil.mkString(phase4AST, "/"));

        return sb.toString();
    } // End of the generateStringAST method

    ////////////////////////////// Generate methods (public) //////////////////////////////

    // Called through directly by sbt in stage 5
    static List<GNode> mutateImplementations(GNode n) {
        List<GNode> phase2ASTs = Phase2.generateASTs(n);
        MutatorVisitor visitor = new MutatorVisitor();
        visitor.mutate(phase2ASTs);
        return phase2ASTs; // Returns the now mutated ASTs
    } // End of the mutateImplementation method

    // Called through the orchestrator
    static List<GNode> mutateImplementations(List<GNode> phase2ASTs) {
        MutatorVisitor visitor = new MutatorVisitor();
        visitor.mutate(phase2ASTs);
        return phase2ASTs; // Returns the now mutated ASTs
    } // End of the mutateImplementation method

    static GNode convertMain(GNode mainClass) {
        MutatorVisitor visitor = new MutatorVisitor();
        return visitor.convertMain(mainClass);
    } // End of the mutateMain method
} // End of the Phase4 class
