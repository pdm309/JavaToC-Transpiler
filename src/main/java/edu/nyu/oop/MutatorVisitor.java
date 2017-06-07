package edu.nyu.oop;

// xtc imports
import org.slf4j.Logger;
import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;

// Utility imports
import edu.nyu.oop.util.NodeUtil;
import edu.nyu.oop.util.MutateUtil;
import edu.nyu.oop.util.StateCollectors.Type;

// General imports
import java.util.ArrayList;
import java.util.List;

class MutatorVisitor extends Visitor {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
    private boolean IS_DEBUG = false; // Set to false when correct

    public void visit(Node n) {
        for (Object o : n) {
            if (o instanceof Node) dispatch((Node) o);
        }
    } // End of the visit method

    /***************************** Helper Methods here ****************************/

    void mutate(List<GNode> phase2ASTs) {
        for (GNode phase2AST : phase2ASTs)
            super.dispatch(phase2AST);
    } // End of the getData method

    /***************************** Visit methods here ****************************/

    public void visitClass(GNode n) {
        if (IS_DEBUG)
            logger.debug("******************** Start of Java -> C++ conversion for class: " + n.get(0) + " ********************");

        GNode implementationNode = n.getGeneric(5);

        // Note: there is only one constructor to mutate, since all overloaded constructors are
        // converted to __init methods in the NameManglerVisitor
        GNode constructor =  (GNode) NodeUtil.dfs(implementationNode, "ConstructorDeclaration");
        mutateConstructor(constructor);

        if (IS_DEBUG)
            logger.debug(n.toString());

        // Mutates all methods
        List<Node> methods = NodeUtil.dfsAll(implementationNode, "MethodDeclaration");
        for (Node method : methods)
            mutateMethod((GNode) method);

        // Mutates all class fields (we use a loop since we only want direct descendants)
        for (int i = 0; i < implementationNode.size(); ++i) {
            if (implementationNode.get(i) instanceof Node) {
                if (implementationNode.getGeneric(i).getName().equals("FieldDeclaration")) {
                    // We only mutate the field declaration to an expression statement if it does assignment
                    GNode field = implementationNode.getGeneric(i);
                    GNode fieldDeclaratorsNode = field.getGeneric(2);
                    GNode fieldDeclaratorNode = fieldDeclaratorsNode.getGeneric(0);
                    if (fieldDeclaratorNode.getGeneric(2) == null)
                        continue;

                    // We now know the node does some kind of assignment, so we move that assignment to the constructor
                    GNode constructorBlockNode = constructor.getGeneric(4);
                    logger.debug(fieldDeclaratorNode.toString());
                    constructorBlockNode.add(mutateFieldToExpressionStatement(fieldDeclaratorNode.getString(0), fieldDeclaratorNode.getGeneric(2)));
                }
            }
        }

        // Deletes the field declarations from the implementation body since they're no longer needed
        for (int i = 0; i < implementationNode.size(); ++i) {
            if (implementationNode.get(i) instanceof Node) {
                if (implementationNode.getGeneric(i).getName().equals("FieldDeclaration")) {
                    implementationNode.remove(i);
                    --i;
                }
            }
        }

        if (IS_DEBUG)
            logger.debug("******************** End of Java -> C++ conversion for class: " + n.get(0) + " ********************");
    } // End of visiting for each class

    private GNode mutateFieldToExpressionStatement(String identifier, GNode equality) {
        // Field node children layout
        // Node 0: Modifiers node
        // Node 1: Type node
        // Node 2: Declarators node

        // Expression node children layout
        // Node 0: PrimaryIdentifier
        // Node 1: "="
        // Node 2: Whatever node is the equals

        GNode expressionStatement = GNode.create("ExpressionStatement");
        GNode expression = GNode.create("Expression");

        GNode primaryIdentifier = GNode.create("PrimaryIdentifier");
        primaryIdentifier.add(identifier);

        expression.add(0, primaryIdentifier);
        expression.add(1, "=");
        expression.add(2, equality);
        expressionStatement.add(expression);
        return expressionStatement;
    } // End of the mutateField method

    private void mutateConstructor(GNode constructor) {
        if (IS_DEBUG)
            logger.debug("\t******************** Mutating constructor: " + constructor.get(0) + " ********************");
        // Constructor children layout
        // Node 0: Name (String!)
        // Node 1: Modifiers
        // Node 2: Parameters
        // Node 3: Block (actual implementation)

        // Edits the name to be the C++ version
        String newName = "__" + constructor.get(0);
        constructor.set(0, newName);

        GNode block = constructor.getGeneric(3);

//        // TODO remove after
//        logger.debug("\t\tConstructor\n" + constructor.toString());

        if (IS_DEBUG)
            logger.debug("\t******************** End mutation constructor: " + constructor.get(0) + " ********************");
    } // End of the mutateConstructor method

    private void mutateMethod(GNode method) {
        if (IS_DEBUG)
            logger.debug("\t******************** Mutating method: " + method.get(0) + " ********************");
        // Method children layout
        // Node 0: Name (String!)
        // Node 1: Return Type
        // Node 2: Modifiers
        // Node 3: Parameters
        // Node 4: Method source (String!)
        // Node 5: Block (actual implementation)

        GNode block = method.getGeneric(5);

        // Sets the source name
        String oldSourceName = (String) method.get(4);
        String newSourceName = "__" + oldSourceName;
        method.set(4, newSourceName);

        // Adds a __this parameter
        GNode parametersNode = GNode.create("Parameters");
        GNode selfParameter = GNode.create("Parameter");
        selfParameter.add(0, "__this");

        GNode selfParameterType = GNode.create("Type");
        GNode selfParameterTypeTypeNode = GNode.create("QualifiedIdentifier");
        selfParameterTypeTypeNode.add(oldSourceName);
        selfParameterType.add(selfParameterTypeTypeNode); // Type node
        selfParameterType.add(null); // Dimension

        selfParameter.add(1, selfParameterType);
        selfParameter.add(2, GNode.create("Modifiers"));

        parametersNode.add(selfParameter);

        // Adds the other parameters
        for (int i = 0; i < method.getGeneric(3).size(); ++i)
            parametersNode.add(method.getGeneric(3).get(i));

        method.set(3, parametersNode);
        mutateBlock(block, method);

        // TODO remove after
//        logger.debug("\t\tMutatedMethod:\n" + method.toString());

        if (IS_DEBUG)
            logger.debug("\t******************** End mutation method: " + method.get(0) + " ********************");
    } // End of the mutateMethod method

    GNode convertMain(GNode mainClass) {
        if (IS_DEBUG)
            logger.debug("******************** Converting main class ********************");

        // Class AST children layout
        // Node 0: Class name (String!)
        // Node 1: Package node
        // Node 2: DataLayout node
        // Node 3: VTable node
        // Node 4: Parent node
        // Node 5: Implementation node

        GNode main = GNode.create("Class");
        String className = mainClass.getString(1);

        // Adds the name to the class
        main.add(0, className);

        // Adds the package node to the class
        main.add(1, NodeUtil.dfs(mainClass, "Package"));

        // Adds the DataLayout node (null)
        main.add(2, null);

        // Adds the VTable node (null)
        main.add(3, null);

        // Adds the Parent node (null)
        main.add(4, null);

        // Adds the Implementation node
        GNode implementation = GNode.create("Implementation");
        for (int i = 0; i < mainClass.getGeneric(5).size(); ++i)
            implementation.add(mainClass.getGeneric(5).getGeneric(i));

        // Mutates the methods inside the implementation node
        for (int i = 0; i < implementation.size(); ++i) {
            GNode classChild = implementation.getGeneric(i);

            if (!classChild.toString().contains("MethodDeclaration"))
                continue;

            if (classChild.getString(3).equals("main"))
                implementation.set(i, mutateMainMethod(classChild, className));
            else
                implementation.set(i, mutateMethodInMainClass(classChild, className));

        }
        main.add(5, implementation);

        if (IS_DEBUG)
            logger.debug("******************** Converting main class ********************");

        return main;
    } // End of the mutateMain method

    private GNode mutateMethodInMainClass(GNode oldMethod, String className) {
        // OLD Method children layout
        // Node 0: Modifiers
        // Node 2: Type Node
        // Node 3: name (String!)
        // Node 4: FormalParameters
        // Node 7: Block

        // NEW Method children layout
        // Node 0: method name (String!)
        // Node 1: Return Type
        // Node 2: Modifiers
        // Node 3: Parameters
        // Node 4: Source
        // Node 5: Block

        GNode method = GNode.create("MethodDeclaration");

        // Gets the name
        String name = oldMethod.getString(3);

        // Gets the return type
        GNode returnTypeNode = MutateUtil.getReturnTypeNodeFromReturnType(new Type(oldMethod.getGeneric(2).getGeneric(0).getString(0), oldMethod.getGeneric(2).getString(1)));

        // Gets the modifiers
        GNode modifiers = GNode.create("Modifiers");
        for (int i = 0; i < oldMethod.getGeneric(0).size(); ++i)
            modifiers.add(oldMethod.getGeneric(0).getGeneric(i).getString(0));

        // Gets the parameters
        GNode parameters = GNode.create("Parameters");
        for (int i = 0; i < oldMethod.getGeneric(4).size(); ++i) {
            GNode parameter = GNode.create("Parameter");

            // Gets the parameter name
            String parameterName = oldMethod.getGeneric(4).getGeneric(i).getString(3);
            parameter.add(0, parameterName);

            // Gets the parameter type
            GNode parameterType = MutateUtil.getTypeNodeFromType(new Type(oldMethod.getGeneric(4).getGeneric(i).getGeneric(1).getGeneric(0).getString(0), oldMethod.getGeneric(4).getGeneric(i).getGeneric(1).getString(1)));
            parameter.add(1, parameterType);

            // Gets the parameter modifiers
            GNode parameterModifiers = GNode.create("Modifiers");
            // TODO finish this off
            parameter.add(2, parameterModifiers);

            // Adds the new parameter
            parameters.add(parameter);
        }

        // Gets the block
        GNode block = oldMethod.getGeneric(7);
        if (!block.hasVariable())
            block = GNode.ensureVariable(block);

        mutateBlock(block, oldMethod);

        // Sets the gotten values
        method.add(0, name);
        method.add(1, returnTypeNode);
        method.add(2, modifiers);
        method.add(3, parameters);
        method.add(4, className);
        method.add(5, block);

        return method;
    } // End of the mutateMethodInMainClass

    private GNode mutateMainMethod(GNode oldMethod, String className) {
        // OLD Method children layout
        // Node 0: Modifiers
        // Node 2: Type Node
        // Node 3: name (String!)
        // Node 4: FormalParameters
        // Node 7: Block

        // NEW Method children layout
        // Node 0: method name (String!)
        // Node 1: Return Type
        // Node 2: Modifiers
        // Node 3: Parameters
        // Node 4: Source
        // Node 5: Block

        GNode method = GNode.create("MethodDeclaration");

        // Gets the name
        String name = oldMethod.getString(3);

        // Gets the return type
        GNode returnTypeNode = MutateUtil.getReturnTypeNodeFromReturnType(new Type("int", null));

        // Gets the modifiers
        GNode modifiers = GNode.create("Modifiers");
        for (int i = 0; i < oldMethod.getGeneric(0).size(); ++i)
            modifiers.add(oldMethod.getGeneric(0).getGeneric(i).getString(0));

        // Gets the parameters
        GNode parameters = GNode.create("Parameters");
        GNode voidParameter = GNode.create("Parameter");
        voidParameter.add(null); // No name for voidTypes
        voidParameter.add(GNode.create("VoidType")); // Type
        voidParameter.add(GNode.create("Modifiers")); // Type parameters
        parameters.add(voidParameter);

        // Gets the source
        String source = className;

        // Gets the block
        GNode block = oldMethod.getGeneric(7);
        if (!block.hasVariable())
            block = GNode.ensureVariable(block);

        mutateBlock(block, oldMethod);
        addReturnZero(block);

        // Sets the gotten values
        method.add(0, name);
        method.add(1, returnTypeNode);
        method.add(2, modifiers);
        method.add(3, parameters);
        method.add(4, source);
        method.add(5, block);

        return method;
    } // End of the mutateMainMethod

    // Since a main method must always return an int, we add a return 0 statement
    private void addReturnZero(GNode block) {
        GNode returnStatement = GNode.create("ReturnStatement");
        GNode zeroReturn = GNode.create("IntegerLiteral");
        zeroReturn.add("0");
        returnStatement.add(zeroReturn);
        block.add(returnStatement);
    } // End of the addReturnZero method

    private void mutateBlock(GNode block, GNode oldMethod) {
        // Old method node layout
        // Node 0: Modifiers
        // Node 2: Return type node
        //        B b = new __B();
//        __rt::Ptr<__B> pb(b);
//        __B::__init(b, 'z');

        if (IS_DEBUG)
            logger.debug("\nNew block:\n" + block.toString() + "\n");

        // Mutates class expressions and adds a method call to __init
        List<Node> newClassExpressions = NodeUtil.dfsAll(oldMethod, "NewClassExpression");
        for (Node newClassExpression : newClassExpressions) {
            // Figures out where we insert the new __init method call
            GNode declarator = NodeUtil.getParent((GNode) newClassExpression, block);
            GNode declarators;
            GNode fieldDeclaration;
            int fieldIndex;
            GNode newConstructorCall;

            // Sets a nicer method call (no args)
            if (declarator.getName().equals("Declarator")) {
                declarators = NodeUtil.getParent(declarator, block);
                fieldDeclaration = NodeUtil.getParent(declarators, block);
                fieldIndex = NodeUtil.getChildIndex(fieldDeclaration, block);
                newConstructorCall = MutateUtil.getNewConstructorCall((GNode) newClassExpression, declarator.getString(0));

                // Moves the args to an __init method after the field declaration
                block.add(fieldIndex + 1, newConstructorCall.getGeneric(1));

                // Replaces the newClassExpression with the new one
                NodeUtil.setNode((GNode) newClassExpression, block, newConstructorCall.getGeneric(0));
            }
            else if (declarator.getName().equals("ReturnStatement")) {
                // TODO
                // TODO
                // TODO, make a new method in MutateUtil that'll return 3, the declaration, _init, and the return
                fieldIndex = NodeUtil.getChildIndex(declarator, block);

                // Gets the new constructor call node
                newConstructorCall = MutateUtil.getNewConstructorCall((GNode) newClassExpression, oldMethod.getGeneric(2).getGeneric(0).getString(0));

                logger.debug("\nNewConstructorCall:\n" + newConstructorCall.toString() + "\n");
                // Adds a new field declaration *before* the return
                block.add(fieldIndex, makeNewFieldDeclaration());

                // Moves the args to an __init method *before* the return and *after* the field declaration
                block.add(fieldIndex + 1, newConstructorCall.getGeneric(1));

                // Replaces the newClassExpression with the new one
                NodeUtil.setNode((GNode) newClassExpression, block, newConstructorCall.getGeneric(0));
            }

        }

        // TODO mutate selection to be better
    } // End of the mutateBlock method

    private GNode makeNewFieldDeclaration() {
        return GNode.create(""); // TODO
    }








    //////////////////// old phase 4 below


//    public static List<GNode> adHocNodeMutation(GNode node) {
//        //MutatorVisitor visitor = new MutatorVisitor();
//
//        System.out.println("TESTING THE IMPLEMENTATION OF PHASE 4");
//        System.out.println("---------------------------------------");
//
//        List<GNode> root = Phase1.generateSourceAST(node);
//
//        Node wholeAST = NodeUtil.dfs(node, "CompilationUnit");
//        List<Node> methods = NodeUtil.dfsAll(node, "MethodDeclaration");
//
//        // change package to namespace
//        wholeAST = changePackageToNamespace(wholeAST);
//
//        // remove modifier public from class
//        wholeAST = removePublicModifierFromClass(wholeAST);
//
//        // append beginning of new class with "__"
//        GNode main = null;
//        for (Node n : methods) {
//            if (n.get(3).equals("main")) {
//                main = (GNode) n;
//            }
//        }
//
////        logger.debug("I am here");
//        // Find the any declarations inside the main method
//        if (main.contains("Declarator")) {
////            logger.debug("I am now here");
//            Node declarator = NodeUtil.dfs(NodeUtil.dfs(main, "Block"), "Declarator");
//            for (int i = 0; i < declarator.size(); ++i) {
//                try {
//                    GNode child = declarator.getGeneric(i);
//                    if (child.hasName("NewClassExpression")) {
//                        GNode id = (GNode) NodeUtil.dfs(child, "QualifiedIdentifier");
////                        logger.debug("I am now here again ");
//                        id.set(0, "__" + id.get(0));
//                    }
//                } catch (Exception e) {
//                    // its not a generic node, so we are not
//                    // interested in it for the purposes of this example.
//                }
//            }
//
//        }
//
//        List<Node> blockNodesList = NodeUtil.dfsAll(node, "Block");
//        logger.debug("BLOCKNODESLIST: " + blockNodesList);
//        GNode expressionStatement = null;
//        for (Node n : blockNodesList) {
//
//            for (int i = 0; i < n.size(); i++) {
//                GNode child = (GNode) n.get(i);
//                logger.debug("ITERATING THROUGH BLOCKNODES");
//
//                logger.debug(child.getName());
//
//                try {
//                    if (child.getName().equals("ExpressionStatement")) {
//                        expressionStatement = child;
//                        logger.debug("EXPRESSION STATEMENT IS " + expressionStatement);
//                        GNode callExpression = expressionStatement.getGeneric(0);
//                        if (callExpression.getName().equals("CallExpression")) {
//                            GNode argumentsNode = callExpression.getGeneric(3);
//                            GNode cout = GNode.create("cout");
//                            cout.add(argumentsNode);
//                            // change system out println to cout
//                            expressionStatement.set(0, cout);
//                        }
//                    }
//                } catch (Exception e) {
//                    logger.debug("ERROR FOUND: " + e.toString());
//                    // its not a generic node, so we are not
//                    // interested in it for the purposes of this example.
//                }
//            }
//        }
//
//        // removing modifier "static" from all methods
//        removeStaticModifierFromMethods(methods);
//
//        logger.debug("List Size : " + root.size());
//
//        return root;
//        //print String here
//    }
//
//    private static Node changePackageToNamespace(Node wholeAST) {
//        // Pull out a node we want to mutate
//        GNode packageDeclaration = (GNode) wholeAST.getNode(0);
//
//        // If it is a fixed size GNode, convert it to a variable size one and replace it in the ast
//        if (!packageDeclaration.hasVariable()) {
//            packageDeclaration = GNode.ensureVariable(packageDeclaration);
//            wholeAST.set(0, packageDeclaration);
//        }
//
//        // Create some new nodes...
//        GNode parent = GNode.create("Namespace");
//        parent.add(0, packageDeclaration.getNode(0));
//        parent.add(1, packageDeclaration.getNode(1));
//
//        // Add the new nodes to the parent..
//        packageDeclaration.add(parent);
//
//        // add the new Namespace node to the beginning
//        //remove the PackageDeclaration node (which has since been moved down from index 0 to 1)
//        wholeAST.add(0, parent);
//        wholeAST.remove(1);
//        return wholeAST;
//    }
//
//    private static Node removePublicModifierFromClass(Node wholeAST) {
//        GNode classDeclaration = (GNode) wholeAST.getNode(1);
//
//        // If it is a fixed size GNode, convert it to a variable size one and replace it in the ast
//        if (!classDeclaration.hasVariable()) {
//            classDeclaration = GNode.ensureVariable(classDeclaration);
//            wholeAST.set(1, classDeclaration);
//        }
//
//        classDeclaration.set(0, null);
//        return wholeAST;
//    }
//
//    private static void removeStaticModifierFromMethods(List<Node> methods) {
//        // iterating through methods
//        for (Node n : methods) {
//            // accessing Modifers node
//            GNode modifiers = (GNode) n.getNode(0);
//            try {
//                // the first modifier may be public or static. If it's static then there's no second modifier resulting in an exception which we ignore
//                GNode modifier1 = (GNode) modifiers.getNode(0);
//                if (modifier1.contains("static")) {
//                    // if there's a first modifier that's static, change to null
//                    modifiers.set(0, null);
//                }
//                // the second modifier (if there is one) should be static and will change to null
//                GNode modifier2 = (GNode) modifiers.getNode(1);
//                if (modifier2.contains("static")) {
//                    // if the second modifier is static, change it to null
//                    modifiers.set(1, null);
//                }
//            } catch (Exception e) {
//            }
//        }
//
//    }
//
//    public String generateStringMutatedAST(GNode gNodes) {
//        return Phase1.generateStringAST(gNodes);
//    }











    /////////////////////////////////// end of old phase 4





//    /**
//     * Scoping methods that lists when scope is entered/exited
//     * @param current The current node that whose entry/exit location is being logged
//     * @param next The next node to visit after the current node is logged
//     */
//    public void scope(GNode current, Node next) {
//        // Adds the scope to the summary
////        summary.addScope(current.getLocation()); //TODO
//        String names = "";
//
//        logger.debug("------------------------------ node current name: " + current.getName());
//        logger.debug("------------------------------ full node: " + current.toString());
//
//        // Enters scope
//        runtime.console().p("Enter scope at ").loc(current).pln().flush();
//
//        // Visits the other declarations
//        if (null != next) visit(next);
//
//    } // End of the scope method
//
//    /**
//     * Checks whether a node is an enhanced for-loop
//     * (not a part of the source specifications in our transpiler)
//     * @param n The current node that is being checked
//     * @return True if the current node is an enhanced for-loop, False otherwise
//     */
//    private boolean isEnhancedForControl(GNode n) {
//        return NodeUtil.dfs(n, "EnhancedForControl") != null;
//    } // End of the isEnhancedForControl
//

//
//
//    /***************************** Visit methods here ****************************/
//
//    public void visitPackageDeclaration(GNode n) {
//        scope(n, n.getNode(1));
//    }
//
//    // Deals with the class declarations (LinkedList & Node)
//    public void visitClassDeclaration(GNode n) {
//        remove(n, 0);
//        scope(n, n.getNode(5));
//
//    } // End of the visitClassDeclaration method
//
//    // Deals with the constructor declarations (Node(Object), LinkedList() & LinkedList(Node))
//    public void visitConstructorDeclaration(GNode n) {
//        scope(n, n.getNode(5));
//    } // End of the visitConstructorDeclaration method
//
//    // Deals with all other method declarations. This
//    // includes constructors, but when a constructor is visited
//    // the visitConstructorDeclaration gets called instead.
//    // Visits: add(Node) & remove(int)
//    public void visitMethodDeclaration(GNode n) {
//        // We only want to count methods that actually have code blocks(braces)
//        if (hasBlock(n)) {
//            remove(n, 0);
//            scope(n, n.getNode(7));
//        }
//    } // End of the visitMethodDeclaration method
//
//    // Deals with all code blocks (includes class-level anonymous scope)
//    public void visitBlock(GNode n) {
//        scope(n, n);
//    } // End of the visitBlock method
//
//    // Deals with all for-loop declarations (inside remove(Node))
//    public void visitForStatement(GNode n) {
//        if(!isEnhancedForControl(n))
//            scope(n, n.getNode(1));
//    } // End of the visitForStatement
//
//    public void remove(GNode n, int index) {
//        n.set(index, null);
//    }
//
//    private void replace(List<GNode> nodes, String newName, int index) {
//        for(Node n : nodes) {
//            n.set(index, newName);
//        }
//    }
} // End of the MutatorVisitor class



