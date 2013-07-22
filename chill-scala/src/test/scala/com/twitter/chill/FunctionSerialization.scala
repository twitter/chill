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

import com.esotericsoftware.kryo.serializers.FieldSerializer

trait AwesomeFns {
  val myfun = { x: Int => 2*x }
}

object BaseFns extends AwesomeFns {
  val myfun2 = { x: Int => 4*x }
  def apply(x: Int) = myfun.apply(x)
}

trait AwesomeFn2 {
  def mult: Int
  val timesByMult = { x: Int => mult * x }
}

object BaseFns2 extends AwesomeFn2 {
  def mult = 5
}

object Foo { def Bar = 1 }

class FunctionSerialization extends Specification with BaseProperties {
  noDetailedDiffs() //Fixes issue for scala 2.9

  "Serialize objects with Fns" should {
    "fn calling" in {
      //rt(fn).apply(4) must be_==(8)
      // In the object:
      rt(BaseFns.myfun2).apply(4) must be_==(16)

      // Inherited from the trait:
      rt(BaseFns.myfun).apply(4) must be_==(8)
    }
    "roundtrip the object" in {
      rt(BaseFns) must be_==(BaseFns)
    }
    "Handle traits with abstract vals/def" in {
      val bf2 = rt(BaseFns2)
      (bf2 eq BaseFns2) must beTrue
      bf2 must be_==(BaseFns2)
      bf2.timesByMult(10) must be_==(50)
      val rtTBM = rt(BaseFns2.timesByMult)
      rtTBM.apply(10) must be_==(50)
    }
    "KryoInjection handle an example with closure to function" in {
      val x = rt(() => Foo.Bar)
      x() must be_==(Foo.Bar)
    }
  }
}
