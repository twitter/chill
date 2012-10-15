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

class MapSerializer[K,V,T <: Map[K,V]](emptyMap : Map[K,V]) extends KSerializer[T] {
  // Maps are immutable, no need to copy them
  setImmutable(true)
  def write(kser: Kryo, out: Output, obj: T) {
    out.writeInt(obj.size, true)
    obj.foreach { pair : (K,V) =>
      val kRef = pair._1.asInstanceOf[AnyRef]
      kser.writeClassAndObject(out, kRef)
      out.flush

      val vRef = pair._2.asInstanceOf[AnyRef]
      kser.writeClassAndObject(out, vRef)
      out.flush
    }
  }

  def read(kser: Kryo, in: Input, cls: Class[T]) : T = {
    val size = in.readInt(true)
    if (size == 0) {
      emptyMap.asInstanceOf[T]
    }
    else {
      (0 until size).foldLeft(emptyMap) { (map, i) =>
        val key = kser.readClassAndObject(in).asInstanceOf[K]
        val value = kser.readClassAndObject(in).asInstanceOf[V]

        map + (key -> value)
      }.asInstanceOf[T]
    }
  }
}
