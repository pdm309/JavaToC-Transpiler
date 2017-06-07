#include "output.h"
#include "java_lang.h"
#include <iostream>

using namespace java::lang;
using namespace std;

namespace inputs {
namespace test002 {
  // Implementation of A
  __A::__A() : __vptr(&__vtable) {}

  String __A::toString(A __this) {
  return new __String("A");
  }

  // Internal accessor for java.lang.Object's class.
      Class __A::__class() {
        static Class k =
          new __Class(__rt::literal("inputs.test002.A"),  __Object::__class());
        return k;
      }

  __A_VT __A::__vtable;

} // End of the inputs namespace
} // End of the test002 namespace
