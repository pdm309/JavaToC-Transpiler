#pragma once
#include <stdint.h>
#include <string>
#include "java_lang.h"

using namespace java::lang;
using namespace std;

namespace inputs {
namespace test041 {
namespace inputs {
namespace test041 {
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
    static void mObjectObject(A, Object, Object);
    static void mAObject(A, A, Object);
    static void mObjectA(A, Object, A);

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
    void (*mObjectA)(A, Object, A);
    void (*mAObject)(A, A, Object);
    void (*mObjectObject)(A, Object, Object);

    __A_VT()
        : __isa(__A::__class()),
          __delete(&__rt::__delete<__A>),
          hashCode((int32_t(*)(A)) &__Object::hashCode),
          equals((bool(*)(A, Object)) &__Object::equals),
          getClass((Class(*)(A)) &__Object::getClass),
          toString((String(*)(A)) &__Object::toString),
          mObjectA(&__A::mObjectA),
          mAObject(&__A::mAObject),
          mObjectObject(&__A::mObjectObject) {
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
    static void mObjectObject(B, Object, Object);
    static void mBObject(B, B, Object);
    static void mObjectB(B, Object, B);

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
    void (*mObjectA)(B, Object, A);
    void (*mAObject)(B, A, Object);
    void (*mObjectB)(B, Object, B);
    void (*mBObject)(B, B, Object);
    void (*mObjectObject)(B, Object, Object);

    __B_VT()
        : __isa(__B::__class()),
          __delete(&__rt::__delete<__B>),
          hashCode((int32_t(*)(B)) &__Object::hashCode),
          equals((bool(*)(B, Object)) &__Object::equals),
          getClass((Class(*)(B)) &__Object::getClass),
          toString((String(*)(B)) &__Object::toString),
          mObjectA((void(*)(B, Object, A)) &__A::mObjectA),
          mAObject((void(*)(B, A, Object)) &__A::mAObject),
          mObjectB(&__B::mObjectB),
          mBObject(&__B::mBObject),
          mObjectObject(&__B::mObjectObject) {
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
    static void m(C, C, C);

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
    void (*mObjectA)(C, Object, A);
    void (*mAObject)(C, A, Object);
    void (*mObjectObject)(C, Object, Object);
    void (*m)(C, C, C);

    __C_VT()
        : __isa(__C::__class()),
          __delete(&__rt::__delete<__C>),
          hashCode((int32_t(*)(C)) &__Object::hashCode),
          equals((bool(*)(C, Object)) &__Object::equals),
          getClass((Class(*)(C)) &__Object::getClass),
          toString((String(*)(C)) &__Object::toString),
          mObjectA((void(*)(C, Object, A)) &__A::mObjectA),
          mAObject((void(*)(C, A, Object)) &__A::mAObject),
          mObjectObject((void(*)(C, Object, Object)) &__A::mObjectObject),
          m(&__C::m) {
    }

};

} // End of the inputs namespace
} // End of the test041 namespace
} // End of the inputs namespace
} // End of the test041 namespace
