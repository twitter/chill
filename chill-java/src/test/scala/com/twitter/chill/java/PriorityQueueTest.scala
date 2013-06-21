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

package com.twitter.chill.java

import org.specs._

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import org.objenesis.strategy.StdInstantiatorStrategy

class PriorityQueueSpec extends Specification {
  noDetailedDiffs() //Fixes issue for scala 2.9

  def rt[A](k: Kryo, a: A): A = {
    val out = new Output(1000, -1)
    k.writeClassAndObject(out, a.asInstanceOf[AnyRef])
    val in = new Input(out.toBytes)
    k.readClassAndObject(in).asInstanceOf[A]
  }

  "A PriorityQueue Serializer" should {
    "handle PriorityQueue" in {
      import scala.collection.JavaConverters._

      val kryo = new Kryo()
      kryo.setInstantiatorStrategy(new StdInstantiatorStrategy)
      PriorityQueueSerializer.register(kryo)
      val ord = Ordering.fromLessThan[(Int,Int)] { (l, r) => l._1 < r._1 }
      val q = new java.util.PriorityQueue[(Int,Int)](3, ord)
      q.add((2,3))
      q.add((4,5))
      def toList[A](q: java.util.PriorityQueue[A]): List[A] =
        q.iterator.asScala.toList
      val qlist = toList(q)
      val newQ = rt(kryo, q)
      toList(newQ) must be_==(qlist)
      newQ.add((1,1))
      newQ.add((2,1)) must beTrue
      // Now without an ordering:
      val qi = new java.util.PriorityQueue[Int](3)
      qi.add(2)
      qi.add(5)
      val qilist = toList(qi)
      toList(rt(kryo, qi)) must be_==(qilist)
    }
  }
}
