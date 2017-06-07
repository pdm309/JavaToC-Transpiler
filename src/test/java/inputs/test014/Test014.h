#pragma once
#include <stdint.h>
#include <string>
#include "java_lang.h"

using namespace java::lang;
using namespace std;

namespace inputs {
namespace test014 {
struct __A;
struct __A_VT;

typedef __rt::Ptr< __A> A;

// __A's data layout
struct __A {

    // __A's fields
    __A_VT* __vptr;
    A some;

    // __A's constructor
    public:
        __A();

    // __A's __init methods
    static void __init(A);

    // __A's methods
    public:static void printOther(A, A);

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
    void (*printOther)(A, A);

    __A_VT()
        : __isa(__A::__class()),
          __delete(&__rt::__delete<__A>),
          hashCode((int32_t(*)(A)) &__Object::hashCode),
          equals((bool(*)(A, Object)) &__Object::equals),
          getClass((Class(*)(A)) &__Object::getClass),
          toString((String(*)(A)) &__Object::toString),
          printOther(&__A::printOther) {
    }

};

} // End of the inputs namespace
} // End of the test014 namespace
