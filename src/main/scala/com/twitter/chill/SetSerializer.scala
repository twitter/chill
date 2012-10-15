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

class SetSerializer[V,T<:Set[V]](empty : Set[V]) extends KSerializer[T] {
  // Sets are immutable, no need to copy them
  setImmutable(true)
  def write(kser : Kryo, out : Output, obj : T) {
    out.writeInt(obj.size, true)
    obj.foreach { v =>
      val vRef = v.asInstanceOf[AnyRef]
      kser.writeClassAndObject(out, vRef)
      out.flush
    }
  }
  def read(kser : Kryo, in : Input, cls : Class[T]) : T = {
    val size = in.readInt(true)
    (0 until size).foldLeft(empty) { (set, i) =>
      set + (kser.readClassAndObject(in).asInstanceOf[V])
    }.asInstanceOf[T]
  }
}
