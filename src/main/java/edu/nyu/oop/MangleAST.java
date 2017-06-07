package edu.nyu.oop;

import edu.nyu.oop.util.NodeUtil;
import org.slf4j.Logger;
import xtc.tree.GNode;

import java.util.List;

public class MangleAST {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(MangleAST.class);

    public static void printMangledAST(GNode n) {logger.debug(generateStringMangledASTs(n));} // End of the printMangledAST method

    public static String generateStringMangledASTs(GNode n) {
        List<GNode> mangledASTs = generateMangledASTs(n);

        // Appends the mangled ASTs together
        StringBuilder sb = new StringBuilder();
        for (GNode mangledAST : mangledASTs)
            sb.append(NodeUtil.mkString(mangledAST, "/"));

        return sb.toString();
    } // End of the generateStringMangledASTs

    public static List<GNode> generateMangledASTs(List<GNode> phase1Nodes) {
        for (GNode phase1AST : phase1Nodes) {
            NameManglerVisitor visitor = new NameManglerVisitor();
            visitor.mangleAST(phase1AST);
        }

        return phase1Nodes;
    } // End of the generateMangledASTs method

    public static List<GNode> generateMangledASTs(GNode n) {
        List<GNode> phase1Nodes = Phase1.generateSourceAST(n);

        for (GNode phase1AST : phase1Nodes) {
            NameManglerVisitor visitor = new NameManglerVisitor();
            visitor.mangleAST(phase1AST);
        }

        return phase1Nodes;
    } // End of the generateMangledASTs method

} // End of the MangleAST class
