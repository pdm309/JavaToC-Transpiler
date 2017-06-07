#include <iostream>

#include "java_lang.h"

using namespace java::lang;
using namespace std;

int main(void) {
  // Let's get started.
  cout << "--------------------------------------------" << endl;

  // Object o = new Object();
  Object o = new __Object();

  cout << "o.toString() : "
       << o->__vptr->toString(o)->data // o.toString()
       << endl;
  // ex. o.toString() : java.lang.Object@7fed885000c0

  // Class oClass = o.getClass();
  Class oClass = o->__vptr->getClass(o);

  cout << "oClass.getName()  : "
       << oClass->__vptr->getName(oClass)->data // oClass.getName()
       // oClass.getName()  : java.lang.Object
       << endl
       << "oClass.toString() : "
       << oClass->__vptr->toString(oClass)->data // oClass.toString()
       // oClass.toString() : class java.lang.Object
       << endl;

  // Class oClassClass = oClass.getClass();
  Class oClassClass = oClass->__vptr->getClass(oClass);

  cout << "oClassClass.getName()  : "
       << oClassClass->__vptr->getName(oClassClass)->data // oClassClass.getName()
       // oClassClass.getName()  : java.lang.Class
       << endl
       << "oClassClass.toString() : "
       << oClassClass->__vptr->toString(oClassClass)->data // oClassClass.toString()
       // oClassClass.toString() : class java.lang.Class
       << endl;

  // if (oClass.equals(oClassClass)) { ... } else { ... }
  if (oClass->__vptr->equals(oClass, (Object) oClassClass)) {
    cout << "oClass.equals(oClassClass)" << endl;
  } else {
    cout << "! oClass.equals(oClassClass)" << endl;
    // This is printed ^^^
  }

  // if (oClass.equals(l.getSuperclass())) { ... } else { ... }
  if (oClass->__vptr->equals(oClass, (Object) oClassClass->__vptr->getSuperclass(oClassClass))) {
    cout << "oClass.equals(oClassClass.getSuperclass())" << endl;
    // This is printed ^^^
  } else {
    cout << "! oClass.equals(oClassClass.getSuperclass())" << endl;
  }

  // if (oClass.isInstance(o)) { ... } else { ... }
  if (oClass->__vptr->isInstance(oClass, o)) {
    cout << "o instanceof oClass" << endl;
    // This is printed ^^^
  } else {
    cout << "! (o instanceof oClass)" << endl;
  }

  // if (oClassClass.isInstance(o)) { ... } else { ... }
  if (oClassClass->__vptr->isInstance(oClassClass, o)) {
    cout << "o instanceof oClassClass" << endl;
  } else {
    cout << "! (o instanceof oClassClass)" << endl;
    // This is printed ^^^
  }

  // HACK: Calling java.lang.Object.toString on oClass via o's vptr
  cout << o->__vptr->toString((Object) oClass)->data << endl;
  // ex. java.lang.Class@7fed88500000

  String str = new __String("Lets abuse weak typing!");

  Object oStr = (Object) str;
  cout << "oStr->toString "
       << oStr->toString(oStr)->data << endl
       // oStr->toString java.lang.String@7f89f9404dd0
       << "oStr->__vptr->toString "
       << oStr->__vptr->toString(oStr)->data
       // oStr->__vptr->toString Lets abuse weak typing!
       << endl;

  Class oStrClass = oStr->getClass(oStr);
  Class oStrVpClass = oStr->__vptr->getClass(oStr);
  cout << "oStr->getClass->getName() "
       << oStrClass->getName(oStrClass)->data  << endl
       // oStr->getClass->getName() java.lang.String
       << "oStr->__vptr->getClass->getName() "
       << oStrVpClass->getName(oStrVpClass)->data
       // oStr->__vptr->getClass->getName() java.lang.String
       << endl;

  // Done.
  cout << "--------------------------------------------" << endl;

  return 0;
}
