#pragma once
#include <stdint.h>
#include <string>
#include "java_lang.h"

using namespace java::lang;
using namespace std;

namespace inputs {
namespace test043 {
namespace inputs {
namespace test043 {
struct __A;
struct __A_VT;

struct __B;
struct __B_VT;

struct __C;
struct __C_VT;

typedef __rt::Ptr< __A> A;
typedef __rt::Ptr< __B> B;
typedef __rt::Ptr< __C> C;

// __A's data layout
struct __A {

    // __A's fields
    __A_VT* __vptr;

    // __A's constructor
    public:
        __A();

    // __A's __init methods
    static void __init(A);

    // __A's methods
    static void m(A);
    static A mA(A, A);

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
    A (*mA)(A, A);
    void (*m)(A);

    __A_VT()
        : __isa(__A::__class()),
          __delete(&__rt::__delete<__A>),
          hashCode((int32_t(*)(A)) &__Object::hashCode),
          equals((bool(*)(A, Object)) &__Object::equals),
          getClass((Class(*)(A)) &__Object::getClass),
          toString((String(*)(A)) &__Object::toString),
          mA(&__A::mA),
          m(&__A::m) {
    }

};

// __B's data layout
struct __B {

    // __B's fields
    __B_VT* __vptr;

    // __B's constructor
    public:
        __B();

    // __B's __init methods
    static void __init(B);

    // __B's methods
    static void m(B);
    static C mB(B, B);

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
    A (*mA)(B, A);
    C (*mB)(B, B);
    void (*m)(B);

    __B_VT()
        : __isa(__B::__class()),
          __delete(&__rt::__delete<__B>),
          hashCode((int32_t(*)(B)) &__Object::hashCode),
          equals((bool(*)(B, Object)) &__Object::equals),
          getClass((Class(*)(B)) &__Object::getClass),
          toString((String(*)(B)) &__Object::toString),
          mA((A(*)(B, A)) &__A::mA),
          mB(&__B::mB),
          m(&__B::m) {
    }

};

// __C's data layout
struct __C {

    // __C's fields
    __C_VT* __vptr;

    // __C's constructor
    public:
        __C();

    // __C's __init methods
    static void __init(C);

    // __C's methods
    static void m(C);

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
    A (*mA)(C, A);
    void (*m)(C);

    __C_VT()
        : __isa(__C::__class()),
          __delete(&__rt::__delete<__C>),
          hashCode((int32_t(*)(C)) &__Object::hashCode),
          equals((bool(*)(C, Object)) &__Object::equals),
          getClass((Class(*)(C)) &__Object::getClass),
          toString((String(*)(C)) &__Object::toString),
          mA((A(*)(C, A)) &__A::mA),
          m(&__C::m) {
    }

};

} // End of the inputs namespace
} // End of the test043 namespace
} // End of the inputs namespace
} // End of the test043 namespace
