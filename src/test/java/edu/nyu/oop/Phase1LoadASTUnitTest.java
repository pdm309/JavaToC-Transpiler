package edu.nyu.oop;

// JUnit imports
import edu.nyu.oop.util.ASTTest;
import org.junit.*;

// xtc imports
import org.slf4j.Logger;
import xtc.tree.GNode;

// General imports
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.File;

public class Phase1LoadASTUnitTest {
    private static int count = 0;
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(JunitTestExample.class);
    private final static boolean IS_DEBUG = false; // Set to false when unit tests are well-formed and complete

    @BeforeClass
    public static void beforeClass() {
        // one-time initialization code before all test functions
        // The benefit of doing static init here is there is a failure you will get
        // informative error messages from JUnit and it will continue to run any other tests that it can.
        // As opposed to just blowing up and stopping test execution.
        logger.debug("-------------------- Start of Phase 1 Testing --------------------");
    } // End of the beforeClass method

    @AfterClass
    public static void afterClass() {
        // one-time cleanup code after all test functions run
        logger.debug("-------------------- End of Phase 1 Testing --------------------");
    } // End of the afterClass method

    @Before
    public void setUp() {
        // run before each test function
        // the benefit if utilizing this function is to reduce code duplication
        // and you can also reset the state of your class before each test is executed.
        logger.debug("Setting up test #" + count);
    } // End of the setUp method

    @After
    public void tearDown() {
        // run after each test function
        logger.debug("Tearing down after test #" + count);
        count++;
    } // End of the tearDown method

    @Test
    public void ASTTestDriver() {
        ASTTest.runAll(new Phase1Tester());
    } // End of the ASTTestDriver class

    private class Phase1Tester implements ASTTest.Tester {
        @Override
        public boolean testFunction(String baseFileName) throws FileNotFoundException {
            // Generates an AST from a source input
            GNode node = (GNode) XtcTestUtils.loadTestFile(baseFileName + ".java");

            // Unformatted string representation of AST
            String generatedASTDataString = Phase1.generateStringAST(node);

            // Loads in pre-generated AST from file
            Scanner preGeneratedASTReader = new Scanner(new File(baseFileName + ".ast"));
            StringBuilder preGeneratedASTData = new StringBuilder();

            // Reads in the pre-generated (known to be correct) java source AST
            while (preGeneratedASTReader.hasNextLine())
                preGeneratedASTData.append(preGeneratedASTReader.nextLine());

            // Closes potential memory leaks
            preGeneratedASTReader.close();

            if (IS_DEBUG)
                logger.debug("Current test is returning: " + (preGeneratedASTData.toString().equals(generatedASTDataString)));

            return preGeneratedASTData.toString().equals(generatedASTDataString);
        } // End of the testFunction
    } // End of the Phase1Test class

} // End of the Phase1LoadASTUnitTest class
