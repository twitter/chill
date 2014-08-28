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

import org.scalatest._

class ClosureCleanerSpec extends WordSpec with Matchers {
  def debug(x: AnyRef) {
    println(x.getClass)
    println(x.getClass.getDeclaredFields.map { _.toString }.mkString("  "))
  }
  "ClosureCleaner" should {
    "clean normal objects" in {
      val myList = List(1, 2, 3)
      ClosureCleaner(myList)
      myList should equal(List(1, 2, 3))
    }
    "clean actual closures" in {
      val myFun = { x: Int => x * 2 }

      ClosureCleaner(myFun)
      myFun(1) should equal(2)
      myFun(2) should equal(4)

      case class Test(x: Int)
      val t = Test(3)
      ClosureCleaner(t)
      t should equal(Test(3))
    }

    "handle outers with constructors" in {

      class Test(x: String) {
        val l = x.size
        def rev(y: String) = (x + y).size
      }

      val t = new Test("you all everybody")
      val fn = t.rev _
      //debug(fn)
      //println(ClosureCleaner.getOutersOf(fn))
      ClosureCleaner(fn)
      fn("hey") should equal(20)
    }
    "Handle functions in traits" in {
      val fn = BaseFns2.timesByMult
      ClosureCleaner(fn)
      fn(10) should equal(50)
    }
    "Handle captured vals" in {
      val answer = 42
      val fn = { x: Int => answer * x }
      ClosureCleaner(fn)
      fn(10) should equal(420)
    }
  }
}
