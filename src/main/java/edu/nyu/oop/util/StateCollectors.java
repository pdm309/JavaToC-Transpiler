package edu.nyu.oop.util;

// General imports
import xtc.tree.GNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateCollectors {
    /***************************** Start CPPVisitor-specific classes ****************************/
    // An instance of this class will be mutated as the Ast is traversed.
    public static class HeaderData {
        private List<CPPClass> classes;
        private Map<String, CPPClass> getCPPClass;
        private GNode mainClass;
        private String packageName;

        public HeaderData() {
            classes = new ArrayList<>();
            getCPPClass = new HashMap<>();
        } // End of the HeaderData constructor

        // Getters
        public  List<CPPClass> getClasses() {return classes;}
        public CPPClass getGetCPPClass(String cppClass) {
            return getCPPClass.get(cppClass);
        }
        public GNode getMainClass() {
            if (!mainClass.getGeneric(mainClass.size() - 1).getName().equals("Package")) {
                if (!mainClass.hasVariable())
                    mainClass = GNode.ensureVariable(mainClass);

                GNode packageNode = GNode.create("Package");
                packageNode.add(packageName);
                mainClass.add(packageNode);
            }

            return mainClass;
        }
        public String getPackageName() {return packageName;}

        // Setters
        public void setMainClass(GNode mainClass) {this.mainClass = mainClass;}
        public void setPackageName(String packageName) {this.packageName = packageName;}

        public void addClass(CPPClass cppClass) {
            classes.add(cppClass);
            getCPPClass.put(cppClass.getName(), cppClass);
        } // End of addClasses method

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (CPPClass cppClass : classes)
                sb.append(cppClass.toString());
            return sb.toString();
        } // End of the toString method
    } // End of the HeaderData class

    public static class CPPClass {
        private String name;
        private CPPClass parent;
        private String parentName;
        private String packageName;
        private List<Field> fields;
        private List<Method> methods;
        private List<Constructor> constructors;

        public CPPClass(String name, CPPClass parent, String packageName, List<Field> fields, List<Method> methods, String parentName, List<Constructor> constructors) {
            this.name = name;
            this.parent = parent;
            this.packageName = packageName;
            this.fields = fields;
            this.methods = methods;
            this.parentName = parentName;
            this.constructors = constructors;
        } // End of the CPPClass constructor

        // Getters
        public String getName() {
            return name;
        }
        public List<Field> getFields() {
            return fields;
        }
        public List<Method> getMethods() {
            return methods;
        }
        public CPPClass getParent() {return parent;}
        public String getPackageName() {
            return packageName;
        }
        public String getParentName() {
            return parentName;
        }
        public List<Constructor> getConstructors() {return constructors;}

        public String toString() {
            StringBuilder sb = new StringBuilder();

            // Adds the class name
            sb.append("Class:\n");
            sb.append("    ").append(name).append("\n");

            // Adds the package name
            sb.append("    Package:\n");
            sb.append("        ").append(packageName).append("\n");

            // Adds the parent class
            sb.append("    Parent:\n");
            sb.append("        ").append(parent).append("\n");

            // Adds the fields
            sb.append("    Fields:\n");
            for (Field field : fields)
                sb.append(field.toString());

            // Adds the constructors
            sb.append("    Constructors:\n");
            for (Constructor constructor : constructors)
                sb.append(constructor.toString());

            // Adds the methods
            sb.append("    Methods:\n");
            for (Method method : methods)
                sb.append(method.toString());

            return sb.toString();
        } // End of the toString method

        public List<Method> getInitMethods() {
            List<Method> initMethods = new ArrayList<>();
            for (Method method: methods) {
                if (method.getName().contains("__init"))
                    initMethods.add(method);
            }
            return initMethods;
        } // End of the getInitMethods method

        public List<Method> getNonInitMethods() {
            List<Method> nonInitMethods = new ArrayList<>();
            for (Method method: methods) {
                if (!method.getName().contains("__init"))
                    nonInitMethods.add(method);
            }
            return nonInitMethods;
        } // End of the getNonInitMethods method

    } // End of the CPPClass class

    public static class Constructor {
        private List<String> modifiers;
        private String name;
        private List<Parameter> parameters;
        private String source; // Class source
        private GNode block;

        public Constructor(List<String> modifiers, String name, List<Parameter> parameters, String source, GNode block) {
            this.modifiers = modifiers;
            this.name = name;
            this.parameters = parameters;
            this.source = source;
            this.block = block;
        } // End of the constructor constructor <-- Great naming right there

        // Getters
        public List<String> getModifiers() {return modifiers;}
        public String getName() {return name;}
        public List<Parameter> getParameters() {return parameters;}
        public String getSource() {return source;}
        public GNode getBlock() {return block;}

        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("        Constructor:\n");
            sb.append("            ").append(name).append("\n");

            // Adds the modifiers
            sb.append("            Modifiers:\n");
            for (String modifier : modifiers) {
                sb.append("                Modifier:\n");
                sb.append("                    ").append(modifier).append("\n");
            }

            // Adds the parameters
            sb.append("            Parameters:\n");
            for (Parameter parameter : parameters) {
                sb.append("                Parameter:\n");
                sb.append("                    ").append(parameter.getName()).append("\n");
                sb.append("                    Type:\n");
                sb.append("                        ").append(parameter.getType().getType()).append("\n");
                sb.append("                        ").append(parameter.getType().getDimension()).append("\n");
            }

            // Adds which object this method belongs to
            sb.append("            Source:\n");
            sb.append("                ").append(source).append("\n");
            return sb.toString();
        } // End of the toString method
    } // End of the Constructor class

    public static class Method {
        private List<String> modifiers;
        private Type returnType;
        private String name;
        private List<Parameter> parameters;
        private String source; // Class source
        private GNode block;

        public Method(List<String> modifiers, Type returnType, String name, List<Parameter> parameters, String source, GNode block) {
            this.modifiers = modifiers;
            this.name = name;
            this.parameters = parameters;
            this.returnType = returnType;
            this.source = source;
            this.block = block;
        } // End of the method constructor

        // Getters
        public List<String> getModifiers() {return modifiers;}
        public Type getReturnType() {
            return returnType;
        }
        public String getName() {return name;}
        public List<Parameter> getParameters() {return parameters;}
        public String getSource() {return source;}
        public GNode getBlock() {return block;}

        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("        Method:\n");
            sb.append("            ").append(name).append("\n");
            sb.append("            Return Type:\n");
            sb.append("                ").append(returnType.getType()).append("\n");
            sb.append("                ").append(returnType.getDimension()).append("\n");

            // Adds the modifiers
            sb.append("            Modifiers:\n");
            for (String modifier : modifiers) {
                sb.append("                Modifier:\n");
                sb.append("                    ").append(modifier).append("\n");
            }

            // Adds the parameters
            sb.append("            Parameters:\n");
            for (Parameter parameter : parameters) {
                sb.append("                Parameter:\n");
                sb.append("                    ").append(parameter.getName()).append("\n");
                sb.append("                    Type:\n");
                sb.append("                        ").append(parameter.getType().getType()).append("\n");
                sb.append("                        ").append(parameter.getType().getDimension()).append("\n");
            }

            // Adds which object this method belongs to
            sb.append("            Source:\n");
            sb.append("                ").append(source).append("\n");
            return sb.toString();
        } // End of the toString method
    } // End of the method class

    public static class Parameter {
        private String name;
        private Type type;
        private List<String> modifiers;

        public Parameter(String name, Type type, List<String> modifiers) {
            this.name = name;
            this.type = type;
            this.modifiers = modifiers;
        } // End of the parameter constructor

        // Getters
        public String getName() {
            return name;
        }
        public Type getType() {
            return type;
        }
        public List<String> getModifiers() {return modifiers;}

        public String toString() {
            return "Name: " + name + "Type: " + type.toString();
        } // End of the toString method
    } // End of the parameter class

    public static class Field {
        private String name;
        private Type type;
        private List<String> modifiers;
        private GNode node;

        public Field(String name, Type type, List<String> modifiers, GNode node) {
            this.name = name;
            this.type = type;
            this.modifiers = modifiers;
            this.node = node;
        } // End of the field constructor

        // Getters
        public String getName() {
            return name;
        }
        public Type getType() {
            return type;
        }
        public List<String> getModifiers() {
            return modifiers;
        }
        public GNode getNode() {return node;}

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("        Field:\n");
            sb.append("            ").append(name).append("\n");

            // Adds the type
            sb.append("            Type:\n");
            sb.append("                ").append(type.getType()).append("\n");
            sb.append("                ").append(type.getDimension()).append("\n");

            // Adds the modifiers
            sb.append("            Modifiers:\n");
            for (String modifier : modifiers)
                sb.append("                Modifier: ").append(modifier).append("\n");

            return sb.toString();
        } // End of the toString method
    } // End of the Field class

    public static class Type {
        private String type;
        private String dimension;

        public Type(String type, String dimension) {
            this.type = type;
            this.dimension = dimension;
        } // End of the type constructor

        public String toString() {
            if (dimension == null)
                return type;
            else
                return type + dimension;
        } // End of the toString method

        // Getters
        public String getType() {
            return type;
        }
        public String getDimension() {
            return dimension;
        }
    } // End of the type class

    /***************************** End CPPVisitor-specific classes ****************************/

} // End of the StateCollectors class
