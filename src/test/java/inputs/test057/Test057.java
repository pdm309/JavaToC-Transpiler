package inputs.test057;

class A {
    String a[];
    public int preInitialisedInt = 0;
    private String preInitialisedString = "Test";

    public int returnInt() {
        return 0;
    }
    public int returnInt(int i) {return i;}

    public static void doNothing() {
        System.out.println("doesn't really do much");
    }

    public String getString(String s) {return s;}
    public B getB(B b) {return b;}
    public Object getObject(Object o) {return o;}

    public int[] mangledMethodCall() {
        int[] returnArr = new int[2];
        returnArr[0] = returnInt();
        returnArr[1] = returnInt(5);
        String test = getString("Test");

        B tempB = new B(0, 3);
        B b = getB(tempB);

        Object tempO = new Object();
        Object o = getObject(tempO);

        return returnArr;
    }

    public A() {
        a = new String[1];
        a[0] = "A";
    }

    // Tests over-riding via extensions
    public String toString() {
        return "A";
    }
}

class B extends A {
    String b;
    int c;
    public String testMethod(String a, int b) {
        c = b;
        return a;
    }

    public B(int c, int d) {
        b = "B";
    }
    public B(int e) { c = e;}
}

class C extends B {
    public C() {
        super(0, 0);
    }

    public static int aStaticMethod(C c) {
        return 0;
    }

    public static void callingAStaticMethod() {
        C newC = new C();
        aStaticMethod(newC);
    }
}

class D {
    void emptyMethod() {}
}


public class Test057 {
    public static int giveInt(int a) {
        return a;
    }
    public static int giveInt() {return 1;}
//    public static D giveDObject() {return new D();}

    public static void main(String[] args) {
        // Testing string literal
        String eh = "testing";

        // Testing plain declaration
        B b1;

        // Testing normal declaration + assignment without parameters
        C c = new C();

        // Testing normal declaration + assignment with parameters
        B b2 = new B(0, 0);

        // Testing normal declaration + assignment with parameters + nested function call
        B b3 = new B(0, giveInt());
        B b4 = new B(giveInt(), giveInt(5));
        B b5 = new B(giveInt(5), giveInt(giveInt(10)));

        // Testing subtype polymorphism
        A a = new B(1, 1);

        // Testing standard system function call + literal arguments
        System.out.println(5);          // Int
        System.out.println("Testing");  // String
        System.out.println(5.5);        // Float
        System.out.println(true);       // Boolean
        System.out.println('a');        // Char

        // Testing standard system function call + static function call arguments
        System.out.println(giveInt());

        // Testing standard system function call + static function call arguments + parameters
        System.out.println(giveInt(5));

        // Testing standard system function call + object instance method call
        System.out.println(b2.toString());

        // Testing standard system function call + object field retrieval
        System.out.println(b2.c);
    } // End of the main method
} // End of the Test057 class