package edu.nyu.oop;

// xtc imports
import org.slf4j.Logger;
import xtc.tree.GNode;
import xtc.tree.Node;

// Utility imports
import edu.nyu.oop.util.JavaFiveImportParser;
import edu.nyu.oop.util.NodeUtil;

// General imports
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

class Phase1 {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(Phase1.class);

    public static void printPhase1AST(GNode n) {
        logger.debug("\n" + generateStringAST(n));
    } // End of the printFullAST method

    public static String generateStringAST(GNode n) {
        // Keeps a string representation of the nodes (stringBuilder for space efficiency)
        StringBuilder stringNodes = new StringBuilder();

        // Builds the source AST
        List<GNode> dependencies = generateSourceAST(n);

        // Builds the output string
        for(GNode node : dependencies)
            stringNodes.append(node.toString());

        return stringNodes.toString();
    } // End of the generateStringAST method

    // Generates the AST for a given node and all of its dependencies and returns as a string
    public static List<GNode> generateSourceAST(GNode n) {
        // Instantiates the seen set
        Set<GNode> seenClasses = new HashSet<>();
        List<GNode> dependencies = new LinkedList<>();

        return getDependencies(n, seenClasses, dependencies);
    } // End of the generateSourceAST method

    // Returns a list of dependencies from a given node
    private static List<GNode> getDependencies(GNode n, Set<GNode> seenClasses, List<GNode> dependencies) {
        // Keeps count of dependencies that we've already seen, and makes sure we don't cycle
        if (seenClasses.contains(n))
            return dependencies;

        // Adds the node to the dependencies if it hasn't been seen before
        dependencies.add(n);
        seenClasses.add(n);

        List<GNode> dependenciesNodes = JavaFiveImportParser.parse(n);

        for(GNode dependentNode : dependenciesNodes)
            getDependencies(dependentNode, seenClasses, dependencies);

        return dependencies;
    } // End of the getDependencies class

} // End of the Phase1 class
