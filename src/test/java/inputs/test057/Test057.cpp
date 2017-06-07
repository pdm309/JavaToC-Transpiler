#include "java_lang.h"

using namespace java::lang;

namespace inputs {
namespace test005 {

    // java.lang.String(<literal>)
    __A::__A() : __vptr(&__vtable),
        a() {

            // a = new String[1];
            a = new __rt::Array<String>(1);

            // a[0] = "A";
            __rt::checkNotNull(a);
              __rt::checkIndex(a, 0);
              __rt::checkStore(a, __rt::literal("A"));
              a->__data[0] = __rt::literal("A");
        }

      // Internal accessor for java.lang.String's class.
      Class __A::__class() {
        static Class k =
          new __Class(__rt::literal("java.lang.A"), __Object::__class());
        return k;
      }
      __A_VT __A::__vtable;

      /////////////////////////////////////////

          // java.lang.String(<literal>)
          __B::__B() : __vptr(&__vtable),
              b() {
              b = new __String("B");
              a = new __String("B");
              cout << a->__vptr->toString(a)->data << endl;
          }

            // Internal accessor for java.lang.String's class.
            Class __B::__class() {
              static Class k =
                new __Class(__rt::literal("java.lang.B"), __A::__class());
              return k;
            }
            __B_VT __B::__vtable;
  }
}
