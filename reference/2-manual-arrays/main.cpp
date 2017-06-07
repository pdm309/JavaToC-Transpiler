#include <iostream>

#include "java_lang.h"

using namespace java::lang;
using namespace std;

int main(void) {
  cout << "--------------------------------------------" << endl;

  // int[] a = new int[5];
  ArrayOfInt a = new __ArrayOfInt(5);

  // Class aClass = a.getClass();
  Class aClass = a->__vptr->getClass(a);

  // aClass.getName()
  std::cout << "a.getClass.getName()  : "
  << aClass->__vptr->getName(aClass)->data << std::endl;

  // a[2]
  __rt::checkIndex(a->length, 2);
  std::cout << "a[2]  : " << a->__data[2] << std::endl;

  // a[10]
  try {
    __rt::checkIndex(a->length, 10);
    std::cout << "a[10] : " << a->__data[10] << std::endl;
  } catch (const ArrayIndexOutOfBoundsException& ex) {
    std::cout << "Caught ArrayIndexOutOfBoundsException"<< std::endl;
  }

  cout << "--------------------------------------------" << endl;
}
