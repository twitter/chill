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

import scala.collection.immutable.BitSet
import scala.collection.immutable.ListMap
import scala.collection.immutable.HashMap

import scala.collection.mutable.{ArrayBuffer => MArrayBuffer, HashMap => MHashMap}
import _root_.java.util.PriorityQueue
import _root_.java.util.Locale
import scala.collection.mutable

class ExtSomeRandom(val x: Int)

class ExternalizerSpec extends Specification with BaseProperties {

  noDetailedDiffs() //Fixes issue for scala 2.9

  def getKryo = KryoSerializer.registered.newKryo

  "KryoSerializers and KryoDeserializers" should {
    "Externalizer handle circular references with Java" in {
      val l = Array[AnyRef]("asdf", "defs")
      val ext = Externalizer(l)
      l(1) = ext

      ext.javaWorks must be_==(true)
    }
    "Externalizer handle circular references with Java2" in {
      val l = Array[AnyRef](null)
      val ext = Externalizer(l)
      l.update(0, ext) // make a loop
      (l(0) eq ext) must beTrue
      ext.javaWorks must be_==(true)
      //jrt(ext).get.toList must_==(l.toList)
      // Try Kryo also
      //rt(ext).get.toList must_==(l.toList)
 
      val nonJavaSer = Array(new ExtSomeRandom(2))
      jrt(nonJavaSer) must throwA[Exception]
      val ext2 = Externalizer(nonJavaSer)
      jrt(ext2).get(0).x must be_==(2)
      ext2.javaWorks must beFalse
 
     
    }

    "Externalizer handle circular references with kryo only serialzable objects" in {
       // Add on non-java serialziable and a loop
      val l3 = Array[AnyRef](null, null)
      val ext3 = Externalizer(l3)
      l3.update(0, ext3) // make a loop
      l3.update(1, new ExtSomeRandom(3)) // make a loop
      (l3(0) eq ext3) must beTrue
      ext3.javaWorks must be_==(false)
      (jrt(ext3).get)(1).asInstanceOf[ExtSomeRandom].x must_==(l3(1).asInstanceOf[ExtSomeRandom].x)
    }

    "Externalizer circular reference with scala tuples(java and kryo Serializable" in {
      val l4 = Array[AnyRef](null, null)
      val ext4 = Externalizer(l4)
      l4.update(0, ext4) // make a loop
      l4.update(1, (3, 7)) // make a loop
      (l4(0) eq ext4) must beTrue
      ext4.javaWorks must be_==(true)
      (rt(ext4).get)(1) must_==(l4(1))
      (jrt(ext4).get)(1) must_==(l4(1))
    }

    "Externalizer handle circular references with non Kryo Serializable members" in {
        val l4 = Array[AnyRef](null, null)
        val ext4 = Externalizer(l4)
        l4.update(0, ext4) // make a loop
        l4.update(1, new Locale("en")) // make a loop
        (l4(0) eq ext4) must beTrue
        ext4.javaWorks must be_==(true)
        (rt(new EmptyScalaKryoInstantiator(), ext4).get)(1)
        (jrt(ext4).get)(1) must_==(l4(1))
    }
  }
}
