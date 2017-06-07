package edu.nyu.oop.util;

import org.slf4j.Logger;
import java.io.FileNotFoundException;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ASTTest {

    private final static int INPUT_SIZE = 57;
    private final static String DIRECTORY = "src/test/java/inputs/test";
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ASTTest.class);
    private final static boolean IS_DEBUG = false; // Set to false when unit tests are well-formed and complete

    public interface Tester {
        boolean testFunction(String baseFileName) throws FileNotFoundException;
    } // End of the Tester interface

    public static void runAll(Tester tester) {
        try {
            for (int i = 0; i <= INPUT_SIZE; ++i) {
                logger.debug("-------------------- Running test input " + i + " --------------------");

                // Pads with 0s in filename as needed
                String fileNumber;
                if (i < 10)
                    fileNumber = "00" + Integer.toString(i);
                else
                    fileNumber = "0" + Integer.toString(i);

                String baseFileName = DIRECTORY + fileNumber + "/Test" + fileNumber;
                if (IS_DEBUG)
                    logger.debug("The current AST being loaded is: " + baseFileName + ".ast");

                // Runs a comparison for each AST
                assertTrue(tester.testFunction(baseFileName));
            } // End of looping through each AST comparison
        } catch (FileNotFoundException e) {
            logger.debug("ERROR: AST file could not be found");
            logger.debug(e.getMessage());
            fail();
        }
    } // End of the runAll method

} // End of the RunAllTestsUtil class
