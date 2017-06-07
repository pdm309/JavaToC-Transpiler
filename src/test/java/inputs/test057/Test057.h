#pragma once
#include <stdint.h>
#include <string>
#include "java_lang.h"

using namespace java::lang;
using namespace std;

namespace inputs {
namespace test057 {
namespace inputs {
namespace test057 {
struct __A;
struct __A_VT;

struct __B;
struct __B_VT;

struct __C;
struct __C_VT;

struct __D;
struct __D_VT;

typedef __rt::Ptr< __A> A;
typedef __rt::Ptr< __B> B;
typedef __rt::Ptr< __C> C;
typedef __rt::Ptr< __D> D;

// __A's data layout
struct __A {

    // __A's fields
    __A_VT* __vptr;
    String[] a;
    public: int32_t preInitialisedInt;
    private: String preInitialisedString;

    // __A's constructor
    public:
        __A();

    // __A's __init methods
    public:static void __init(A);

    // __A's methods
    public:static int32_t returnInt(A);
    public:static int32_t returnIntInt(A, int32_t);
    public:static void doNothing(A);
    public:static String getString(A, String);
    public:static B getB(A, B);
    public:static Object getObject(A, Object);
    public:static int32_t mangledMethodCall(A);
    public:static String toString(A);

    // Function returning class object representing __A
    static Class __class();

    // The vtable for __A
    static __A_VT __vtable;

};

// A's vtable
struct __A_VT {
    Class __isa;

    void (*__delete)(__A*);
    int32_t (*hashCode)(A);
    bool (*equals)(A, Object);
    Class (*getClass)(A);
    String (*toString)(A);
    int32_t (*mangledMethodCall)(A);
    Object (*getObject)(A, Object);
    B (*getB)(A, B);
    String (*getString)(A, String);
    int32_t (*returnIntInt)(A, int32_t);
    int32_t (*returnInt)(A);

    __A_VT()
        : __isa(__A::__class()),
          __delete(&__rt::__delete<__A>),
          hashCode((int32_t(*)(A)) &__Object::hashCode),
          equals((bool(*)(A, Object)) &__Object::equals),
          getClass((Class(*)(A)) &__Object::getClass),
          toString(&__A::toString),
          mangledMethodCall(&__A::mangledMethodCall),
          getObject(&__A::getObject),
          getB(&__A::getB),
          getString(&__A::getString),
          returnIntInt(&__A::returnIntInt),
          returnInt(&__A::returnInt) {
    }

};

// __B's data layout
struct __B {

    // __B's fields
    __B_VT* __vptr;
    String[] a;
    public: int32_t preInitialisedInt;
    String b;
    int32_t c;

    // __B's constructor
    public:
        __B();

    // __B's __init methods
    public:static void __init(B, int32_t, int32_t);

    // __B's methods
    public:static void __init(B, int32_t);
    public:static String testMethod(B, String, int32_t);

    // Function returning class object representing __B
    static Class __class();

    // The vtable for __B
    static __B_VT __vtable;

};

// B's vtable
struct __B_VT {
    Class __isa;

    void (*__delete)(__B*);
    int32_t (*hashCode)(B);
    bool (*equals)(B, Object);
    Class (*getClass)(B);
    String (*toString)(B);
    int32_t (*mangledMethodCall)(B);
    Object (*getObject)(B, Object);
    B (*getB)(B, B);
    String (*getString)(B, String);
    int32_t (*returnIntInt)(B, int32_t);
    int32_t (*returnInt)(B);
    String (*testMethod)(B, String, int32_t);

    __B_VT()
        : __isa(__B::__class()),
          __delete(&__rt::__delete<__B>),
          hashCode((int32_t(*)(B)) &__Object::hashCode),
          equals((bool(*)(B, Object)) &__Object::equals),
          getClass((Class(*)(B)) &__Object::getClass),
          toString((String(*)(B)) &__A::toString),
          mangledMethodCall((int32_t(*)(B)) &__A::mangledMethodCall),
          getObject((Object(*)(B, Object)) &__A::getObject),
          getB((B(*)(B, B)) &__A::getB),
          getString((String(*)(B, String)) &__A::getString),
          returnIntInt((int32_t(*)(B, int32_t)) &__A::returnIntInt),
          returnInt((int32_t(*)(B)) &__A::returnInt),
          testMethod(&__B::testMethod) {
    }

};

// __C's data layout
struct __C {

    // __C's fields
    __C_VT* __vptr;
    String[] a;
    public: int32_t preInitialisedInt;
    String b;
    int32_t c;

    // __C's constructor
    public:
        __C();

    // __C's __init methods
    public:static void __init(C);

    // __C's methods
    public:static int32_t aStaticMethod(C, C);
    public:static void callingAStaticMethod(C);

    // Function returning class object representing __C
    static Class __class();

    // The vtable for __C
    static __C_VT __vtable;

};

// C's vtable
struct __C_VT {
    Class __isa;

    void (*__delete)(__C*);
    int32_t (*hashCode)(C);
    bool (*equals)(C, Object);
    Class (*getClass)(C);
    String (*toString)(C);
    int32_t (*mangledMethodCall)(C);
    Object (*getObject)(C, Object);
    B (*getB)(C, B);
    String (*getString)(C, String);
    int32_t (*returnIntInt)(C, int32_t);
    int32_t (*returnInt)(C);
    String (*testMethod)(C, String, int32_t);

    __C_VT()
        : __isa(__C::__class()),
          __delete(&__rt::__delete<__C>),
          hashCode((int32_t(*)(C)) &__Object::hashCode),
          equals((bool(*)(C, Object)) &__Object::equals),
          getClass((Class(*)(C)) &__Object::getClass),
          toString((String(*)(C)) &__A::toString),
          mangledMethodCall((int32_t(*)(C)) &__A::mangledMethodCall),
          getObject((Object(*)(C, Object)) &__A::getObject),
          getB((B(*)(C, B)) &__A::getB),
          getString((String(*)(C, String)) &__A::getString),
          returnIntInt((int32_t(*)(C, int32_t)) &__A::returnIntInt),
          returnInt((int32_t(*)(C)) &__A::returnInt),
          testMethod((String(*)(C, String, int32_t)) &__B::testMethod) {
    }

};

// __D's data layout
struct __D {

    // __D's fields
    __D_VT* __vptr;

    // __D's constructor
    public:
        __D();

    // __D's __init methods
    static void __init(D);

    // __D's methods
    static void emptyMethod(D);

    // Function returning class object representing __D
    static Class __class();

    // The vtable for __D
    static __D_VT __vtable;

};

// D's vtable
struct __D_VT {
    Class __isa;

    void (*__delete)(__D*);
    int32_t (*hashCode)(D);
    bool (*equals)(D, Object);
    Class (*getClass)(D);
    String (*toString)(D);
    void (*emptyMethod)(D);

    __D_VT()
        : __isa(__D::__class()),
          __delete(&__rt::__delete<__D>),
          hashCode((int32_t(*)(D)) &__Object::hashCode),
          equals((bool(*)(D, Object)) &__Object::equals),
          getClass((Class(*)(D)) &__Object::getClass),
          toString((String(*)(D)) &__Object::toString),
          emptyMethod(&__D::emptyMethod) {
    }

};

} // End of the inputs namespace
} // End of the test057 namespace
} // End of the inputs namespace
} // End of the test057 namespace
