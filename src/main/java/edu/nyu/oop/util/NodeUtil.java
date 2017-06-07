package edu.nyu.oop.util;

import xtc.lang.JavaFiveParser;
import xtc.parser.Result;
import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Token;
import xtc.tree.Visitor;

import java.io.*;
import java.util.*;


public class NodeUtil {

    public static void setNode(GNode nodeToReplace, GNode containingNode, GNode replacingNode) {
        GNode parent = NodeUtil.getParent(nodeToReplace, containingNode);
        int childIndex = NodeUtil.getChildIndex(nodeToReplace, parent);
        parent.set(childIndex, replacingNode);
    } // End of the setNode method

    // Gets the index of a child's node
    public static int getChildIndex(GNode child, GNode parent) {
        for (int i = 0; i < parent.size(); ++i) {
            if (parent.get(i) != null && parent.get(i) instanceof Node && parent.getGeneric(i).equals(child))
                return i;
        }
        return -1;
    } // End of the getChildIndex method


    // Finds a node's parent via a dfs
    public static GNode getParent(GNode child, GNode nodeContainingParent) {
        Stack<GNode> s = new Stack<>();
        s.push(nodeContainingParent);
        Set<GNode> seen = new HashSet<>();

        GNode latestNodeToContainTheChild = nodeContainingParent;

        while (!s.isEmpty()) {
            GNode v = s.pop();

            if (v.contains(child))
                latestNodeToContainTheChild = v;

            // Gets the lower level
            if (!seen.contains(v)) {
                seen.add(v);
                for (int i = 0; i < v.size(); ++i) {
                    if (v.get(i) instanceof Node)
                        s.push(v.getGeneric(i));
                }
            }
        }

        return latestNodeToContainTheChild;
    } // End of the getParent method

    // Also see xtc.lang.JavaEntities

    // Takes a node and concatenates all its children into a string with the specified delimiter
    public static String mkString(Node node, String delim) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < node.size() ; i++) {
            if(node.get(i) == null) continue;

            String s = "";
            try {
                s = Token.cast(node.get(i));
            } catch (Exception e) {
                s = node.get(i).toString();
            }

            buf.append(s);

            if (i < node.size() - 1) buf.append(delim);
        }
        return buf.toString();
    }

    // Searches Ast for a node with specified name. Returns first that it finds.
    public static Node dfs(Node node, String nodeName) {

        // TODO commented out to deal with input 6
        if (node == null) {
            return null;
        } else if (node.hasName(nodeName)) {
            return node;
        } else {
            for (Object o : node) {
                if (o instanceof Node) {
                    Node casted = (Node) o;
                    Node target = dfs(casted, nodeName);
                    if (target != null) return target;
                }
            }
        }
        return null;
    }

    // Searches Ast for a node with specified name. Returns all that it finds.
    public static List<Node> dfsAll(Node root, final String nodeName) {
        final List<Node> nodes = new LinkedList<Node>();
        new Visitor() {
            public void visit(Node n) {
                if(nodeName.equals(n.getName())) {
                    nodes.add(n);
                }
                for (Object o : n) {
                    if (o instanceof Node) dispatch((Node) o);
                }
            }
        } .dispatch(root);

        return nodes;
    }

    // Parses a Java source file into an Xtc Ast
    public static Node parseJavaFile(File file) {
        try {
            InputStream instream = new FileInputStream(file);
            Reader in = new BufferedReader(new InputStreamReader(instream));
            JavaFiveParser parser = new JavaFiveParser(in, file.toString(), (int)file.length());
            Result result = parser.pCompilationUnit(0);
            return (Node) parser.value(result);
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse Java file " + file.getName(), e);
        }
    }

    /**
     * Creates a deep copy of the root GNode
     *
     * @param root a GNode to copy
     * @return duplicated GNode
     */
    public static GNode deepCopyNode(GNode root) {
        GNode top = GNode.create(root);
        top = GNode.ensureVariable(top);
        for (int i = 0; i < top.size(); i++) {
            Object child = top.get(i);
            if (child instanceof GNode) {
                GNode childNode = GNode.cast(child);
                child = deepCopyNode(childNode);
            }
            top.set(i, child);
        }
        return top;
    }
} // End of the NodeUtil class
