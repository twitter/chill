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

abstract class TraversableSerializer[T, C <: Traversable[T]] extends KSerializer[C] {

  def empty(size: Int): C
  def update(old: C, idx: Int, v: T): C
  // Override this if you need to:
  def finish(c: C): C = c
  override val isImmutable = true

  def write(kser: Kryo, out: Output, obj: C) {
    //Write the size:
    out.writeInt(obj.size, true)
    obj.foreach { t  =>
      val tRef = t.asInstanceOf[AnyRef]
      kser.writeClassAndObject(out, tRef)
      // After each intermediate object, flush
      out.flush
    }
  }

  def read(kser: Kryo, in: Input, cls: Class[C]) : C = {
    val size = in.readInt(true)
    finish {
      (0 until size).foldLeft(empty(size)) { (col, idx) =>
        update(col, idx, kser.readClassAndObject(in).asInstanceOf[T])
      }
    }
  }
}
