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

class VectorSerializer[T] extends KSerializer[Vector[T]] {
  // Vectors are immutable, no need to copy them
  setImmutable(true)
  def write(kser: Kryo, out: Output, obj: Vector[T]) {
    //Write the size:
    out.writeInt(obj.size, true)
    obj.foreach { t  =>
      val tRef = t.asInstanceOf[AnyRef]
      kser.writeClassAndObject(out, tRef)
      // After each intermediate object, flush
      out.flush
    }
  }

  def read(kser: Kryo, in: Input, cls: Class[Vector[T]]) : Vector[T] = {
    //Produce the reversed list:
    val size = in.readInt(true)
    if (size == 0) {
      /*
       * this is only here at compile time.  The type T is erased, but the
       * compiler verifies that we are intending to return a type T here.
       */
      Vector.empty[T]
    }
    else {
      (0 until size).foldLeft(Vector.empty[T]) { (vec, i) =>
        val iT = kser.readClassAndObject(in).asInstanceOf[T]
        vec :+ iT
      }
    }
  }
}
