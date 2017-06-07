#include <iostream>

#include "java_lang.h"

using namespace java::lang;
using namespace std;

int main(void) {
  cout << "--------------------------------------------" << endl;

   // int[] a = new int[5];
  __rt::Array<int32_t>* a = new __rt::Array<int32_t>(5);

  // a[2]
  __rt::checkNotNull(a);
  __rt::checkIndex(a, 2);
  std::cout << "a[2]  : " << a->__data[2] << std::endl;

  // a[2] = 5;
  __rt::checkNotNull(a);
  __rt::checkIndex(a, 2);
  a->__data[2] = 5;

  // a[2]
  __rt::checkNotNull(a);
  __rt::checkIndex(a, 2);
  std::cout << "a[2]  : " << a->__data[2] << std::endl;

  // String[] ss = new String[5];
  __rt::Array<String>* ss = new __rt::Array<String>(5);

  // String s = "Hello";
  String s = __rt::literal("Hello");

  // ss[2] = "Hello";
  __rt::checkNotNull(ss);
  __rt::checkIndex(ss, 2);
  __rt::checkStore(ss, s);
  ss->__data[2] = s;

  std::cout << "ss[2] : " << ss->__data[2]->data << std::endl;

  cout << "--------------------------------------------" << endl;
}
