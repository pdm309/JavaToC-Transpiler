package edu.nyu.oop;

import edu.nyu.oop.util.NodeUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import xtc.tree.GNode;
import xtc.tree.Node;


import java.util.List;

public class Phase5ImplementationFileUnitTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(Phase5ImplementationFileUnitTest.class);

    @BeforeClass
    public static void beforeClass() {
        logger.debug("Executing NodeTransformationExample");
    }

    // For the purposes of v1 of the translator we just want to get it working
    // for as many inputs as possible, so start with the simplest and do it ad-hoc.
    // i.e. just getting working for the simplest input and then as you move through
    // the inputs start generalizing and abstracting your mutation methods.
    // Here is a trivial example:
    //   Change the right hand side of new expressions to match the C++ type.
    //   Ex.
    //     B b = new B();
    //   Becomes..
    //     B b = new __B();
    // Note that a lot of the code here is just boilerplate stuff that I need to get at the thing I want to change.
    // Must of this would not be necessary if I was using visitors.
//    @Test
//    public void testingPhase4() {
//        logger.debug("========Begin Phase 5========");
//        GNode root = (GNode) XtcTestUtils.loadTestFile("src/test/java/inputs/test002/Test002.java");
//
//        List<GNode> test = Phase4.adHocNodeMutation(root);
//
//        XtcTestUtils.prettyPrintAst(test.get(0));
//
//        Phase5 printFile = new Phase5("Test002");
//        printFile.print(test.get(0));
//
//        logger.debug("====COMPLETE====");
//    }

}