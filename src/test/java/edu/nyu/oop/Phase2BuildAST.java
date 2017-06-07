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

public class Phase2BuildAST {
    private static int count = 0;
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(Phase2BuildAST.class);
    private final static boolean IS_DEBUG = false; // Set to false when unit tests are well-formed and complete

    @BeforeClass
    public static void beforeClass() {
        // one-time initialization code before all test functions
        // The benefit of doing static init here is there is a failure you will get
        // informative error messages from JUnit and it will continue to run any other tests that it can.
        // As opposed to just blowing up and stopping test execution.
        logger.debug("-------------------- Start of Phase 2 Testing --------------------");
    } // End of the beforeClass method

    @AfterClass
    public static void afterClass() {
        // one-time cleanup code after all test functions run
        logger.debug("-------------------- End of Phase 2 Testing --------------------");
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
    public void ASTDataTestDriver() {
        ASTTest.runAll(new Phase2DataTester());
    } // End of the ASTDataTestDriver method

    @Test
    public void ASTTestDriver() {
        ASTTest.runAll(new Phase2ASTTester());
    } // End of the ASTTestDriver method

    private class Phase2DataTester implements ASTTest.Tester {
        @Override
        public boolean testFunction(String baseFileName) throws FileNotFoundException {
            // Generates a Java AST from a source input
            GNode node = (GNode) XtcTestUtils.loadTestFile(baseFileName + ".java");

            // Creates the data AST (includes dependencies)
            String generatedASTsString = Phase2.generateStringASTData(node);

            // Loads in pre-generated AST data
            Scanner preGeneratedASTReader = new Scanner(new File(baseFileName + ".hdata"));
            StringBuilder preGeneratedASTData = new StringBuilder();

            // Reads in the pre-generated (known to be correct) .h file AST data
            while (preGeneratedASTReader.hasNextLine())
                preGeneratedASTData.append(preGeneratedASTReader.nextLine()).append("\n");

            // Closes potential memory leak
            preGeneratedASTReader.close();

            if (IS_DEBUG)
                logger.debug("Current test is returning: " + (preGeneratedASTData.toString().equals(generatedASTsString)));

            return preGeneratedASTData.toString().equals(generatedASTsString);
        } // End of the testFunction
    } // End of the Phase2DataTester class

    private class Phase2ASTTester implements ASTTest.Tester {
        @Override
        public boolean testFunction(String baseFileName) throws FileNotFoundException {
            // Generates a Java AST from a source input
            GNode node = (GNode) XtcTestUtils.loadTestFile(baseFileName + ".java");

            // Creates the AST (includes dependencies)
            String headerASTString = Phase2.generateStringAST(node);

            // Loads in pre-generated AST data
            Scanner preGeneratedASTReader = new Scanner(new File(baseFileName + ".hast"));
            StringBuilder preGeneratedASTData = new StringBuilder();

            // Reads in the pre-generated (known to be correct) .h file AST data
            while (preGeneratedASTReader.hasNextLine())
                preGeneratedASTData.append(preGeneratedASTReader.nextLine());

            // Closes potential memory leak
            preGeneratedASTReader.close();

            if (IS_DEBUG)
                logger.debug("Current test is returning: " + (preGeneratedASTData.toString().equals(headerASTString)));

            return preGeneratedASTData.toString().equals(headerASTString);
        } // End of the testFunction
    } // End of the Phase2ASTTester class
} // End of the Phase2BuildHeaderAST class