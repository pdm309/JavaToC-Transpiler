package edu.nyu.oop;

// xtc imports
import org.slf4j.Logger;
import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;

// Utility imports
import edu.nyu.oop.util.NodeUtil;
import edu.nyu.oop.util.StateCollectors.*;

// General imports
import java.util.List;
import java.util.ArrayList;

/* WARNING: The following code *will* make you cry. A safety pig has been included for your benefit.
*
*
*	 _._ _..._ .-',     _.._(`))
*	'-. `     '  /-._.-'    ',/
*	   )         \            '.
*	  / _    _    |             \
*	 |  a    a    /              |
*	 \   .-.                     ;
*	  '-('' ).-'       ,'       ;
*	     '-;           |      .'
*	        \           \    /
*	        | 7  .__  _.-\   \
*	        | |  |  ``/  /`  /
*	       /,_|  |   /,_/   /
*	          /,_/      '`-'
*
*/

class CPPHeaderVisitor extends Visitor {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
    private List<String> packageName;
    private HeaderData headerData; // The "state collector", add to this as you traverse.
    private boolean IS_DEBUG = false;

    public CPPHeaderVisitor() {
        this.headerData = new HeaderData();
        this.packageName = new ArrayList<>();
    } // End of the CPPHeaderVisitor constructor

    public void visit(Node n) {
        for (Object o : n) {
            if (o instanceof Node) dispatch((Node) o);
        }
    } // End of the visit method

    /***************************** Helper Methods here ****************************/

    // Used when checking if a class body child is a method or field.
    // Probably has other uses with detecting anonymous class-level scope because magic.
    private boolean hasBlock(GNode n) {
        return NodeUtil.dfs(n, "Block") != null;
    } // End of the hasBlock method

    private String typeConvert(String s) {
        switch (s) {
            case "int":
                return "int32_t";
            case "boolean":
                return "bool";
            default:
                return s;
        }
    } // End of the typeConvert method

    HeaderData getData(List<GNode> mangledASTs) {
        if (IS_DEBUG)
            logger.debug("******************** Start of Phase 2 Visitor: Data Gathering ********************");

        for (GNode mangledAST : mangledASTs)
            this.dispatch(mangledAST);

        if (IS_DEBUG)
            logger.debug("******************** End of Phase 2 Visitor: Data Gathering ********************");

        return this.headerData;
    } // End of the getData method

    List<GNode> getNodes(List<GNode> mangledASTs) {
        if (IS_DEBUG)
            logger.debug("******************** Start of Phase 2 Visitor: AST Generation ********************");

        for (GNode mangledAST : mangledASTs)
            this.dispatch(mangledAST);

        if (IS_DEBUG)
            logger.debug("******************** End of Phase 2 Visitor: AST Generation ********************");

        return Phase2.generateASTs(this.headerData);
    } // End of the getNodes method

    private String getPackageNameString() {
        // Gets back the package name
        StringBuilder packageNameSB = new StringBuilder();
        for (String packageNameSubstring : packageName)
            packageNameSB.append(packageNameSubstring).append(".");
        return packageNameSB.toString().substring(0, packageNameSB.length() - 1);
    } // End of the getPackageNameString method

    // Finds the package name and adds it to the header data
    public void visitPackageDeclaration(GNode n) {
        if (IS_DEBUG)
            logger.debug("\t******************** Retrieving package data ********************");

        GNode qualifiedIdentifier = (GNode) n.get(1); // This node's children are the package names

        for (int i = 0; i < qualifiedIdentifier.size(); ++i) {
            if (IS_DEBUG)
                logger.debug("\t\tAdding package name: " + qualifiedIdentifier.get(i).toString());
            this.packageName.add(qualifiedIdentifier.get(i).toString());
        }
        headerData.setPackageName(getPackageNameString());
    } // End of the visitPackageDeclaration method

    // Deals with the class declarations
    public void visitClassDeclaration(GNode n) {
        if (IS_DEBUG)
            logger.debug("\t******************** Retrieving class data for: " + n.get(1) + " ********************");

        // Check to make sure we don't add the main test class into the header (it goes into main.cpp)
        String classNameNode = (String) n.get(1);
        if (classNameNode.startsWith("Test")) {
            // Adds the main class node to the data gatherer
            headerData.setMainClass(n);
            return;
        }

        // Class children layout
        // Node 0: Modifiers <-- class modifiers
        // Node 1: Test000 <-- class name
        // Node 3 (potentially null): Extension <-- if not extending anything, don't make this. If it does, 1st child is super class, 2nd is implementing class, fill with null as appropriate
        // Node 5: ClassBody <-- actual class body

        String className;
        List<Field> classFields = new ArrayList<>();
        List<Method> classMethods = new ArrayList<>();
        List<Constructor> classConstructors = new ArrayList<>();
        String classParent = "java.lang.Object";

        // Sets the class name
        if (IS_DEBUG)
            logger.debug("\tSetting class name to: " + classNameNode);
        className = classNameNode;

        // Sets the class parent
        if (n.get(3) != null) {
            GNode parentNameParentNode = (GNode) NodeUtil.dfs((GNode) n.get(3), "QualifiedIdentifier");
            if (parentNameParentNode != null)
                classParent = parentNameParentNode.get(0).toString();
        }

        if (IS_DEBUG)
            logger.debug("\tSetting class parent to: " + classParent);

        // Adds the class internals
        GNode classBody = (GNode) n.get(5);

        // We define a "class body child" to either be a method or a field
        for (int i = 0; i < classBody.size(); ++i) {
            GNode classChild = (GNode) classBody.get(i);
            if (classChild != null) {
                // NOTE: A classbody's child may be a field, method, or constructor.
                switch (classChild.getName()) {
                    case "MethodDeclaration":
                        classMethods.add(addMethodFromNode(classChild, className));
                        break;
                    case "ConstructorDeclaration":
                        classConstructors.add(addConstructorFromNode(classChild, className));
                        break;
                    default:
                        classFields.add(addFieldFromNode(classChild));
                        break;
                } // End of adding a classbody's child
            } // Done iterating through a non-null class body child
        } // End of iterating through each class body child

        if (IS_DEBUG)
            logger.debug("Adding class " + className + " to the header data collector\n\n");

        headerData.addClass(new CPPClass(className, null, getPackageNameString(), classFields, classMethods, classParent, classConstructors));
    } // End of the visitClassDeclaration method

    private Field addFieldFromNode(GNode classChild) {
        // Class body child is a field
        if (IS_DEBUG)
            logger.debug("\t******************** Adding field ********************");

        // Field children layout
        // Node 0: Modifiers <-- field modifiers
        // Node 1: Type <-- field type
        // Node 2: Declarators <-- field information (e.g. name)

        String fieldName;
        Type fieldType;
        List<String> fieldModifiers = new ArrayList<>();

        // Used to get the name and type dimension <-- latter is kinda wierd but alright
        GNode declaratorNode = (GNode) NodeUtil.dfs(classChild, "Declarator");

        // Sets the field name
        if (declaratorNode != null) {
            if (IS_DEBUG)
                logger.debug("\t\tSetting field name to: " + declaratorNode.get(0));
            fieldName = (String) declaratorNode.get(0);
        } else {
            if (IS_DEBUG)
                logger.debug("\t\tNo field declarator node found, directly accessing and setting field name to: " + classChild.get(2));
            fieldName = (String) classChild.get(2);
        }

        // Note: we search for the grandparent node with name "Type" because it's .get(0) can be
        // "QualifiedIdentifier" for classes, or "PrimitiveType" for primitives
        GNode fieldTypeGrandParentNode = (GNode) NodeUtil.dfs(classChild, "Type");
        assert fieldTypeGrandParentNode != null;
        // Sets the field type
        GNode fieldTypeParentNode = (GNode) fieldTypeGrandParentNode.get(0);

        String fieldTypeActual = typeConvert(fieldTypeParentNode.get(0).toString());
        if (IS_DEBUG)
            logger.debug("\t\tSetting field type to: " + fieldTypeActual);


        // Sets the field modifiers
        GNode fieldModifiersNode = classChild.getGeneric(0);
        for (int i = 0; i < fieldModifiersNode.size(); ++i)
            fieldModifiers.add(fieldModifiersNode.getGeneric(i).getString(0));

        assert declaratorNode != null;
        if (declaratorNode.get(1) != null) {
            GNode fieldDimensionNode = (GNode) declaratorNode.get(1);
            if (IS_DEBUG)
                logger.debug("\t\tSetting field dimension to: " + fieldDimensionNode.get(0).toString());
            fieldType = new Type(fieldTypeActual, fieldDimensionNode.get(0).toString());
        } else {
            if (IS_DEBUG)
                logger.debug("\t\tSetting field dimension to: null pointer");
            fieldType = new Type(fieldTypeActual, null);
        }


        return new Field(fieldName, fieldType, fieldModifiers, classChild);
    } // End of the addFieldFromNode method

    private Method addMethodFromNode(GNode classChild, String className) {
        // Class body child is a method
        if (IS_DEBUG)
            logger.debug("\t******************** Adding method ********************");

        // Method children layout
        // Node 0: Modifiers <-- method modifiers parent node
        // Node 2: Type <-- method return type node
        // Node 3: "toString" <-- method name (String!)
        // Node 4: FormalParameters node <-- method parameters parent node
        // Node 7: Block <-- actual method implementation

        // Gathered method data
        List<String> methodModifiers = new ArrayList<>();
        Type methodReturnType;
        String methodName;
        List<Parameter> methodParameters = new ArrayList<>();
        String methodSource;

        // Sets the method name
        methodName = (String) classChild.get(3);
        if (IS_DEBUG)
            logger.debug("\t\tSetting method name to: " + methodName);

        // Sets method modifiers
        GNode methodModifiersNode = (GNode) classChild.get(0);
        if (IS_DEBUG)
            logger.debug("\t\tAdding method " + methodName + "'s modifier(s)");

        for (int j = 0; j < methodModifiersNode.size(); ++j) {
            GNode methodModifier = (GNode) methodModifiersNode.get(j);
            if (methodModifier != null) {
                if (IS_DEBUG)
                    logger.debug("\t\t\tAdding method modifier: " + methodModifier.get(0).toString());
                methodModifiers.add(methodModifier.get(0).toString());
            }
        }

        // Sets the method return type
        GNode methodReturnTypeNode = (GNode) classChild.get(2);

        if (methodReturnTypeNode.toString().equals("VoidType()")) {
            if (IS_DEBUG)
                logger.debug("\t\tSetting method return type to void");
            methodReturnType = new Type("void", null);
        } else {
            if (IS_DEBUG)
                logger.debug("\t\tSetting method return type to a non-void value");
            GNode typeParentNode = (GNode) methodReturnTypeNode.get(0);
            GNode dimensionParentNode = (GNode) methodReturnTypeNode.get(1);

            String convertedType = typeConvert(typeParentNode.get(0).toString());

            if (dimensionParentNode != null)
                methodReturnType = new Type(convertedType, dimensionParentNode.get(0).toString());
            else
                methodReturnType = new Type(convertedType, null);

            if (IS_DEBUG)
                logger.debug("\t\tSetting method return type to: " + methodReturnTypeNode.toString());
        }

        // Sets method parameters
        if  (IS_DEBUG)
            logger.debug("\t\tSetting method parameters");
        addParameters(methodParameters, (GNode) classChild.get(4));

        // Sets method source (just the class name)
        if (IS_DEBUG)
            logger.debug("\t\tSetting method source to: " + className);
        methodSource = className;

        // Adds the method to the class
        return new Method(methodModifiers, methodReturnType, methodName, methodParameters, methodSource, classChild.getGeneric(7));
    } // End of the addMethodFromNode method

    private void addParameters(List<Parameter> methodParameters, GNode parameterParentNode) {

        for (int j = 0; parameterParentNode != null && j < parameterParentNode.size(); ++j) {
            // Parameter nodes
            // Node 0: Modifiers
            // Node 1: Type, .get(1) = actual type, .get(2) (optional) = dimensions
            // Node 3: parameter name

            GNode parameterNode = (GNode) parameterParentNode.get(j);
            List<String> parameterModifiers = new ArrayList<>();
            String parameterName;
            String parameterType;
            String parameterDimensions;

            // Sets the parameter name
            if (IS_DEBUG)
                logger.debug("\t\t\tSetting parameter name to: " + parameterNode.get(3).toString());
            parameterName = parameterNode.get(3).toString();

            // Sets the parameter type
            GNode parameterTypeNode = (GNode) NodeUtil.dfs(parameterNode, "Type");

            // Type Nodes
            // Node 0: Type
            // Node 1: Dimension (Optional, basically whether it's an array or not, null == not array)

            // Note: typing this variable name out hurt my soul
            assert parameterTypeNode != null;
            GNode parameterTypeActualParentNode = (GNode) parameterTypeNode.get(0);

            parameterType = typeConvert(parameterTypeActualParentNode.get(0).toString());
            if (IS_DEBUG)
                logger.debug("\t\t\tSetting parameter type to: " + parameterType);

            // Sets the parameter dimension
            GNode parameterDimensionNode = (GNode) parameterTypeNode.get(1);
            if (parameterDimensionNode != null) {
                if (IS_DEBUG)
                    logger.debug("\t\t\tSetting parameter dimension to: " + parameterDimensionNode.get(0).toString());
                parameterDimensions = parameterDimensionNode.get(0).toString();
            } else {
                if (IS_DEBUG)
                    logger.debug("\t\t\tSetting parameter dimension to: null pointer");
                parameterDimensions = null;
            }

            // Sets the parameter modifiers
            GNode parameterModifiersNode = parameterNode.getGeneric(0);
            for (int i = 0; i < parameterModifiersNode.size(); ++i)
                parameterModifiers.add(parameterModifiersNode.getGeneric(i).getString(0));

            // Adds the new parameter to the parameter list
            if (IS_DEBUG)
                logger.debug("\t\tAdding parameter '" + parameterName +  "' to the parameter list");
            methodParameters.add(new Parameter(parameterName, new Type(parameterType, parameterDimensions), parameterModifiers));
        } // End of iterating through each parameter
    } // End of the addParameters method

    private Constructor addConstructorFromNode(GNode classChild, String className) {
        // Class body child is a constructor
        if (IS_DEBUG)
            logger.debug("\t******************** Adding constructor ********************");

        // Constructor children layout
        // Node 0: Modifiers <-- constructor modifiers parent node
        // Node 2: "A" <-- constructor name (String!)
        // Node 3: FormalParameters node <-- constructor parameters parent node
        // Node 5: Block <-- actual method implementation (not needed here)

        // Gathered constructor data
        List<String> constructorModifiers = new ArrayList<>();
        String constructorName;
        List<Parameter> constructorParameters = new ArrayList<>();
        String constructorSource; // Class source

        // Sets the constructor name
        constructorName = (String) classChild.get(2);
        if (IS_DEBUG)
            logger.debug("\t\tSetting constructor name to: " + constructorName);

        // Sets the constructor modifiers
        GNode constructorModifiersNode = (GNode) classChild.get(0);
        if (IS_DEBUG)
            logger.debug("\t\tAdding constructor " + constructorName + "'s modifier(s)");

        for (int j = 0; j < constructorModifiersNode.size(); ++j) {
            GNode methodModifier = (GNode) constructorModifiersNode.get(j);
            if (methodModifier != null) {
                if (IS_DEBUG)
                    logger.debug("\t\t\tAdding constructor modifier: " + methodModifier.get(0).toString());
                constructorModifiers.add(methodModifier.get(0).toString());
            }
        }

        // Sets the constructor parameters
        if  (IS_DEBUG)
            logger.debug("\t\tSetting constructor parameters");
        addParameters(constructorParameters, (GNode) classChild.get(3));

        // Sets method source (just the class name)
        if (IS_DEBUG)
            logger.debug("\t\tSetting constructor source to: " + className);
        constructorSource = className;

        // Adds the constructor to the class
        return new Constructor(constructorModifiers, constructorName, constructorParameters, constructorSource, classChild.getGeneric(5));
    } // End of the addConstructorFromNode method

} // End of the CPPHeaderVisitor class