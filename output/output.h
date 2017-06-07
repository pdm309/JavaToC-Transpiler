#pragma once
#include <stdint.h>
#include <string>
#include "java_lang.h"

namespace java {
namespace lang {
struct __A;
struct __A_VT;

struct __B;
struct __B_VT;

typedef __A* A;
typedef __B* B;

struct __A {
    __A_VT* __vptr;
    String a;

    __A();
    static int32_t returnZero(A);
    static String toString(A);
    static Class __class();
    static __A_VT __vtable();

};
struct __A_VT {
    Class __isa;

    int32_t (*hashCode)(A);
    bool (*equals)(A, Object);
    Class (*getClass)(A);
    String (*toString)(A);
    int32_t (*returnZero)(A);

    __A_VT()
        : __isa(__A::__class()),
          hashCode((int32_t(*)(A)) &__Object::hashCode),
          equals((bool(*)(A, Object)) &__Object::equals),
          getClass((Class(*)(A)) &__Object::getClass),
          toString(&__A::toString),
          returnZero(&__A::returnZero)
    {
    }

};

struct __B {
    __B_VT* __vptr;
    String b;
    int32_t c;

    __B(int32_t, int32_t);
    static String testMethod(B, String, int32_t);
    static Class __class();
    static __B_VT __vtable();

};
struct __B_VT {
    Class __isa;

    int32_t (*hashCode)(B);
    bool (*equals)(B, Object);
    Class (*getClass)(B);
    String (*toString)(B);
    int32_t (*returnZero)(B);
    String (*testMethod)(B, String, int32_t);

    __B_VT()
        : __isa(__B::__class()),
          hashCode((int32_t(*)(B)) &__Object::hashCode),
          equals((bool(*)(B, Object)) &__Object::equals),
          getClass((Class(*)(B)) &__Object::getClass),
          toString((String(*)(B)) &__A::toString),
          returnZero((int32_t(*)(B)) &__A::returnZero),
          testMethod(&__B::testMethod)
    {
    }

};

}
}
