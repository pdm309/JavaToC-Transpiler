package inputs.test056;

class A {
    String a[];

    public A() {
        a = new String[1];
        a[0] = "A";
    }
}

class B extends A {
    String b;
    int c;

    public B(int c, int d) {
        b = "B";
    }
}


public class Test056 {
    public static void main(String[] args) {
        B b = new B(0, 0);
        System.out.println(b.a);
        System.out.println(b.b);
    }
}