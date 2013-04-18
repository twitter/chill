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

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.{ Serializer => KSerializer }
import com.esotericsoftware.kryo.io.{ Input, Output }

import java.util.{PriorityQueue => PQue, Comparator}
import scala.collection.JavaConverters._

class PriorityQueueSerializer[A<:AnyRef] extends KSerializer[PQue[A]] {
  private val field = {
    val f = classOf[PQue[A]].getDeclaredField("comparator")
    f.setAccessible(true)
    f
  }
  def getComparator(q: PQue[A]): Comparator[A] = {
    field.get(q).asInstanceOf[Comparator[A]]
  }
  def write(k: Kryo, o: Output, v: PQue[A]) {
    k.writeClassAndObject(o, getComparator(v))
    o.writeInt(v.size, true)
    v.iterator.asScala.foreach { (a: A) => k.writeClassAndObject(o, a); o.flush }
  }
  def read(k: Kryo, i: Input, c: Class[PQue[A]]): PQue[A] = {
    val comp = k.readClassAndObject(i).asInstanceOf[Comparator[A]]
    val sz = i.readInt(true)
    // can't create with size 0:
    val result = new PQue[A](1 max sz, comp)
    var idx = 0
    while(idx < sz) {
      result.add(k.readClassAndObject(i).asInstanceOf[A])
      idx += 1
    }
    result
  }
}
