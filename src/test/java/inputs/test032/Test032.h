#pragma once
#include <stdint.h>
#include <string>
#include "java_lang.h"

using namespace java::lang;
using namespace std;

namespace inputs {
namespace test032 {
namespace inputs {
namespace test032 {
struct __A;
struct __A_VT;

typedef __rt::Ptr< __A> A;

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
    static int32_t mInt(A, int32_t);
    static void mA(A, A);
    static void mDouble(A, double);
    static void mObject(A, Object);
    static void mObjectObject(A, Object, Object);
    static void mAObject(A, A, Object);

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
    void (*mAObject)(A, A, Object);
    void (*mObjectObject)(A, Object, Object);
    void (*mObject)(A, Object);
    void (*mDouble)(A, double);
    void (*mA)(A, A);
    int32_t (*mInt)(A, int32_t);

    __A_VT()
        : __isa(__A::__class()),
          __delete(&__rt::__delete<__A>),
          hashCode((int32_t(*)(A)) &__Object::hashCode),
          equals((bool(*)(A, Object)) &__Object::equals),
          getClass((Class(*)(A)) &__Object::getClass),
          toString((String(*)(A)) &__Object::toString),
          mAObject(&__A::mAObject),
          mObjectObject(&__A::mObjectObject),
          mObject(&__A::mObject),
          mDouble(&__A::mDouble),
          mA(&__A::mA),
          mInt(&__A::mInt) {
    }

};

} // End of the inputs namespace
} // End of the test032 namespace
} // End of the inputs namespace
} // End of the test032 namespace
