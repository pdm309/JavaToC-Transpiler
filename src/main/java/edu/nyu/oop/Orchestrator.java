package edu.nyu.oop;

// xtc imports
import org.slf4j.Logger;
import xtc.tree.GNode;

// General imports
import java.util.List;

public class Orchestrator {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(Orchestrator.class);
    private static final boolean IS_DEBUG = true;

    //////////////////////////////////////// SBT methods ////////////////////////////////////////

    static void translate(GNode n) {
        if (IS_DEBUG)
            logger.debug("-------------------- Starting Translation --------------------");

        // Phase 1: Generates the Java AST
        if (IS_DEBUG)
            logger.debug("-------------------- Phase 1: Generating Java AST --------------------");

        List<GNode> phase1Nodes = Phase1.generateSourceAST(n);

        if (IS_DEBUG)
            logger.debug("-------------------- Phase 1: End Java AST generated --------------------");

        // Phase 1.5: Mangles method names and converts constructors
        if (IS_DEBUG)
            logger.debug("-------------------- Phase 1.5: Mangling method names and converting constructors --------------------");

        List<GNode> mangledASTs = MangleAST.generateMangledASTs(phase1Nodes);

        if (IS_DEBUG)
            logger.debug("-------------------- Phase 1.5: End mangled method names and converted constructors --------------------");

        // Phase 2: Generates all C++ ASTs
        if (IS_DEBUG)
            logger.debug("-------------------- Phase 2: Starting C++ AST generation --------------------");

        List<GNode> phase2ASTs = Phase2.generateASTs(mangledASTs);
        GNode mainClass = Phase2.getMainClassNode(Phase2.generateData(mangledASTs));

        if (IS_DEBUG)
            logger.debug("-------------------- Phase 2: End C++ AST generated --------------------");

        // Phase 3: Condense all C++ ASTs into a single .h file
        if (IS_DEBUG)
            logger.debug("-------------------- Phase 3: Generating .h file now --------------------");

        Phase3.outputHeaderFileFromPhase2(phase2ASTs, mainClass);

        if (IS_DEBUG)
            logger.debug("-------------------- Phase 3: End .h file generated --------------------");

        // Phase 4: Mutates implementation portion of C++ ASTs
        if (IS_DEBUG)
            logger.debug("-------------------- Phase 4: Mutating Java implementation AST to C++ --------------------");

        List<GNode> phase4ASTs = Phase4.mutateImplementations(phase2ASTs);
        GNode mutatedMainClass = Phase4.convertMain(mainClass);

        if (IS_DEBUG)
            logger.debug("-------------------- Phase 4: End implementation AST conversion --------------------");

        // Phase 5.1: Create C++ implementation file
        if (IS_DEBUG)
            logger.debug("-------------------- Phase 5.1: Creating C++ implementation file--------------------");

        Phase5.outputImplementationFileFromPhase4(phase4ASTs, mutatedMainClass);

        if (IS_DEBUG)
            logger.debug("-------------------- Phase 5.1: Created C++ implementation file --------------------");

        // Phase 5.2: Create C++ main file
        if (IS_DEBUG)
            logger.debug("-------------------- Phase 5.2: Creating C++ main file --------------------");

        Phase5.outputMainFileFromMutatedMain(mutatedMainClass);

        if (IS_DEBUG)
            logger.debug("-------------------- Phase 5.2: Created C++ main file --------------------");
    } // End of the translate method

} // End of the Orchestrator class
