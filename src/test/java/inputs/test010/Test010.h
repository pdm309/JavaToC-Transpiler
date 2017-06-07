#pragma once
#include <stdint.h>
#include <string>
#include "java_lang.h"

using namespace java::lang;
using namespace std;

namespace inputs {
namespace test010 {
struct __A;
struct __A_VT;

struct __B1;
struct __B1_VT;

struct __B2;
struct __B2_VT;

struct __C;
struct __C_VT;

typedef __rt::Ptr< __A> A;
typedef __rt::Ptr< __B1> B1;
typedef __rt::Ptr< __B2> B2;
typedef __rt::Ptr< __C> C;

// __A's data layout
struct __A {

    // __A's fields
    __A_VT* __vptr;
    String a;

    // __A's constructor
    public:
        __A();

    // __A's __init methods
    static void __init(A);

    // __A's methods
    public:static void setA(A, String);
    public:static void printOther(A, A);
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
    void (*printOther)(A, A);
    void (*setA)(A, String);

    __A_VT()
        : __isa(__A::__class()),
          __delete(&__rt::__delete<__A>),
          hashCode((int32_t(*)(A)) &__Object::hashCode),
          equals((bool(*)(A, Object)) &__Object::equals),
          getClass((Class(*)(A)) &__Object::getClass),
          toString(&__A::toString),
          printOther(&__A::printOther),
          setA(&__A::setA) {
    }

};

// __B1's data layout
struct __B1 {

    // __B1's fields
    __B1_VT* __vptr;
    String a;
    String b;

    // __B1's constructor
    public:
        __B1();

    // __B1's __init methods
    static void __init(B1);

    // __B1's methods
    static Class __class();

    // The vtable for __B1
    static __B1_VT __vtable;

};

// B1's vtable
struct __B1_VT {
    Class __isa;

    void (*__delete)(__B1*);
    int32_t (*hashCode)(B1);
    bool (*equals)(B1, Object);
    Class (*getClass)(B1);
    String (*toString)(B1);
    void (*printOther)(B1, A);
    void (*setA)(B1, String);

    __B1_VT()
        : __isa(__B1::__class()),
          __delete(&__rt::__delete<__B1>),
          hashCode((int32_t(*)(B1)) &__Object::hashCode),
          equals((bool(*)(B1, Object)) &__Object::equals),
          getClass((Class(*)(B1)) &__Object::getClass),
          toString((String(*)(B1)) &__A::toString),
          printOther((void(*)(B1, A)) &__A::printOther),
          setA((void(*)(B1, String)) &__A::setA) {
    }

};

// __B2's data layout
struct __B2 {

    // __B2's fields
    __B2_VT* __vptr;
    String a;
    String b;

    // __B2's constructor
    public:
        __B2();

    // __B2's __init methods
    static void __init(B2);

    // __B2's methods
    static Class __class();

    // The vtable for __B2
    static __B2_VT __vtable;

};

// B2's vtable
struct __B2_VT {
    Class __isa;

    void (*__delete)(__B2*);
    int32_t (*hashCode)(B2);
    bool (*equals)(B2, Object);
    Class (*getClass)(B2);
    String (*toString)(B2);
    void (*printOther)(B2, A);
    void (*setA)(B2, String);

    __B2_VT()
        : __isa(__B2::__class()),
          __delete(&__rt::__delete<__B2>),
          hashCode((int32_t(*)(B2)) &__Object::hashCode),
          equals((bool(*)(B2, Object)) &__Object::equals),
          getClass((Class(*)(B2)) &__Object::getClass),
          toString((String(*)(B2)) &__A::toString),
          printOther((void(*)(B2, A)) &__A::printOther),
          setA((void(*)(B2, String)) &__A::setA) {
    }

};

// __C's data layout
struct __C {

    // __C's fields
    __C_VT* __vptr;
    String a;
    String b;
    String c;

    // __C's constructor
    public:
        __C();

    // __C's __init methods
    static void __init(C);

    // __C's methods
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
    void (*printOther)(C, A);
    void (*setA)(C, String);

    __C_VT()
        : __isa(__C::__class()),
          __delete(&__rt::__delete<__C>),
          hashCode((int32_t(*)(C)) &__Object::hashCode),
          equals((bool(*)(C, Object)) &__Object::equals),
          getClass((Class(*)(C)) &__Object::getClass),
          toString((String(*)(C)) &__A::toString),
          printOther((void(*)(C, A)) &__A::printOther),
          setA((void(*)(C, String)) &__A::setA) {
    }

};

} // End of the inputs namespace
} // End of the test010 namespace
