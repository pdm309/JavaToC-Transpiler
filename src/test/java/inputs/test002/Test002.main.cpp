#include <iostream>

#include "ptr.h"
#include "java_lang.h"
#include "output.h"

using namespace inputs::test002;
using namespace java::lang;
using namespace std;

int main(void) {
  A a = new __A();

  Object o = (Object) a;

  cout << o->__vptr->toString(o)->data << endl;

} // End of the main method
