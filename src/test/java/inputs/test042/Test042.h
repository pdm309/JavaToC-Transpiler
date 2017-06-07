#pragma once
#include <stdint.h>
#include <string>
#include "java_lang.h"

using namespace java::lang;
using namespace std;

namespace inputs {
namespace test042 {
namespace inputs {
namespace test042 {
struct __A;
struct __A_VT;

struct __B;
struct __B_VT;

typedef __rt::Ptr< __A> A;
typedef __rt::Ptr< __B> B;

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
    static B mB(B, B);
    static A mA(B, A);

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
    B (*mB)(B, B);
    void (*m)(B);

    __B_VT()
        : __isa(__B::__class()),
          __delete(&__rt::__delete<__B>),
          hashCode((int32_t(*)(B)) &__Object::hashCode),
          equals((bool(*)(B, Object)) &__Object::equals),
          getClass((Class(*)(B)) &__Object::getClass),
          toString((String(*)(B)) &__Object::toString),
          mA(&__B::mA),
          mB(&__B::mB),
          m(&__B::m) {
    }

};

} // End of the inputs namespace
} // End of the test042 namespace
} // End of the inputs namespace
} // End of the test042 namespace
