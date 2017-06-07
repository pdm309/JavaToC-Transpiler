#pragma once
#include <stdint.h>
#include <string>
#include "java_lang.h"

using namespace java::lang;
using namespace std;

namespace inputs {
namespace test056 {
namespace inputs {
namespace test056 {
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
    String[] a;

    // __A's constructor
    public:
        __A();

    // __A's __init methods
    public:static void __init(A);

    // __A's methods
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

    __A_VT()
        : __isa(__A::__class()),
          __delete(&__rt::__delete<__A>),
          hashCode((int32_t(*)(A)) &__Object::hashCode),
          equals((bool(*)(A, Object)) &__Object::equals),
          getClass((Class(*)(A)) &__Object::getClass),
          toString((String(*)(A)) &__Object::toString) {
    }

};

// __B's data layout
struct __B {

    // __B's fields
    __B_VT* __vptr;
    String[] a;
    String b;
    int32_t c;

    // __B's constructor
    public:
        __B();

    // __B's __init methods
    public:static void __init(B, int32_t, int32_t);

    // __B's methods
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

    __B_VT()
        : __isa(__B::__class()),
          __delete(&__rt::__delete<__B>),
          hashCode((int32_t(*)(B)) &__Object::hashCode),
          equals((bool(*)(B, Object)) &__Object::equals),
          getClass((Class(*)(B)) &__Object::getClass),
          toString((String(*)(B)) &__Object::toString) {
    }

};

} // End of the inputs namespace
} // End of the test056 namespace
} // End of the inputs namespace
} // End of the test056 namespace
