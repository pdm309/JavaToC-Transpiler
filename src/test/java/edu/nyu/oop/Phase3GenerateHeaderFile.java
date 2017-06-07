package edu.nyu.oop;

// JUnit imports
import org.junit.*;

// xtc imports
import org.slf4j.Logger;
import xtc.tree.GNode;

// Utility imports
import edu.nyu.oop.util.ASTTest;

// General imports
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

public class Phase3GenerateHeaderFile {
    private static int count = 0;
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(Phase3GenerateHeaderFile.class);
    private final static boolean IS_DEBUG = true; // Set to false when unit tests are well-formed and complete

    @BeforeClass
    public static void beforeClass() {
        // one-time initialization code before all test functions
        // The benefit of doing static init here is there is a failure you will get
        // informative error messages from JUnit and it will continue to run any other tests that it can.
        // As opposed to just blowing up and stopping test execution.
        logger.debug("-------------------- Start of Phase 3 Testing --------------------");
    } // End of the beforeClass method

    @AfterClass
    public static void afterClass() {
        // one-time cleanup code after all test functions run
        logger.debug("-------------------- End of Phase 3 Testing --------------------");
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
    public void HeaderFileTestDriver() {
        ASTTest.runAll(new Phase3HeaderFileTester());
    } // End of the HeaderASTTestDriver method

    private class Phase3HeaderFileTester implements ASTTest.Tester {
        @Override
        public boolean testFunction(String baseFileName) throws FileNotFoundException {
            // Generates a Java AST from a source input
            GNode node = (GNode) XtcTestUtils.loadTestFile(baseFileName + ".java");

            List<GNode> phase1Nodes =  Phase1.generateSourceAST(node);
            List<GNode> phase2ASTs = Phase2.generateASTs(phase1Nodes);
            GNode mainClass = Phase2.getMainClassNode(node);

            Phase3.outputHeaderFileFromPhase2(phase2ASTs, mainClass);

            // Loads in pre-generated .h file
            Scanner preGeneratedFileReader = new Scanner(new File(baseFileName + ".h"));
            StringBuilder preGeneratedHeaderFile = new StringBuilder();

            // Reads in the pre-generated (known to be correct) .h file
            while (preGeneratedFileReader.hasNextLine())
                preGeneratedHeaderFile.append(preGeneratedFileReader.nextLine()).append("\n");

            // Loads in the generated .h file
            Scanner generatedFileReader = new Scanner(new File("output/output.h"));
            StringBuilder generatedHeaderFile = new StringBuilder();
            while (generatedFileReader.hasNextLine())
                generatedHeaderFile.append(generatedFileReader.nextLine()).append("\n");

            // Closes potential memory leak
            preGeneratedFileReader.close();
            generatedFileReader.close();

            if (IS_DEBUG)
                logger.debug("Current test is returning: " + (preGeneratedHeaderFile.toString().equals(generatedHeaderFile.toString())));

            return preGeneratedHeaderFile.toString().equals(generatedHeaderFile.toString());
        } // End of the testFunction
    } // End of the Phase3HeaderFileTester class
} // End of the Phase3GenerateHeaderFile class
