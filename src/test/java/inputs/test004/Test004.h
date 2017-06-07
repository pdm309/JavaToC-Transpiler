#pragma once
#include <stdint.h>
#include <string>
#include "java_lang.h"

using namespace java::lang;
using namespace std;

namespace inputs {
namespace test004 {
struct __A;
struct __A_VT;

typedef __rt::Ptr< __A> A;

// __A's data layout
struct __A {

    // __A's fields
    __A_VT* __vptr;
    private: String fld;

    // __A's constructor
    public:
        __A();

    // __A's __init methods
    public:static void __init(A, String);

    // __A's methods
    public:static String getFld(A);

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
    String (*getFld)(A);

    __A_VT()
        : __isa(__A::__class()),
          __delete(&__rt::__delete<__A>),
          hashCode((int32_t(*)(A)) &__Object::hashCode),
          equals((bool(*)(A, Object)) &__Object::equals),
          getClass((Class(*)(A)) &__Object::getClass),
          toString((String(*)(A)) &__Object::toString),
          getFld(&__A::getFld) {
    }

};

} // End of the inputs namespace
} // End of the test004 namespace
