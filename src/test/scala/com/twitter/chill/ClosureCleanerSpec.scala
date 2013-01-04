/*
Copyright 2012 Twitter, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.twitter.chill

import org.specs._

class ClosureCleanerSpec extends Specification {
  def debug(x: AnyRef) {
    println(x.getClass)
    println(x.getClass.getDeclaredFields.map { _.toString }.mkString("  "))
  }
  "Should clean normal objects" in {
    val myList = List(1,2,3)
    ClosureCleaner(myList)
    myList must be_==(List(1,2,3))
  }
  "Should clean actual closures" in {
    val myFun = { x: Int => x*2 }

    ClosureCleaner(myFun)
    myFun(1) must be_==(2)
    myFun(2) must be_==(4)

    case class Test(x : Int)
    val t = Test(3)
    ClosureCleaner(t)
    t must be_==(Test(3))
  }

  "Should handle outers with constructors" in {

    class Test(x: String) {
      val l = x.size
      def rev(y: String) = (x + y).size
    }

    val t = new Test("you all everybody")
    val fn = t.rev _
    //debug(fn)
    //println(ClosureCleaner.getOutersOf(fn))
    ClosureCleaner(fn)
    fn("hey") must be_==(20)
  }
}
