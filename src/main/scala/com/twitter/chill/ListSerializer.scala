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

// Long Lists cause stack overflows for Kryo because they are cons cells.
class ListSerializer[V, T <: List[V]](emptyList : List[V]) extends KSerializer[T] {
  // Lists are immutable, no need to copy them
  setImmutable(true)
  def write(kser: Kryo, out: Output, obj: T) {
    //Write the size:
    out.writeInt(obj.size, true)
    /*
     * An excellent question arises at this point:
     * How do we deal with List[List[T]]?
     * Since by the time this method is called, the ListSerializer has
     * already been registered, this iterative method will be used on
     * each element, and we should be safe.
     * The only risk is List[List[List[List[.....
     * But anyone who tries that gets what they deserve
     */
    obj.foreach { t =>
      val tRef = t.asInstanceOf[AnyRef]
      kser.writeClassAndObject(out, tRef)
      // After each itermediate object, flush
      out.flush
    }
  }

  def read(kser: Kryo, in: Input, cls: Class[T]) : T = {
    //Produce the reversed list:
    val size = in.readInt(true)
    if (size == 0) {
      emptyList.asInstanceOf[T]
    }
    else {
      (0 until size).foldLeft(emptyList) { (l, i) =>
        val iT = kser.readClassAndObject(in).asInstanceOf[V]
        iT :: l
      }.reverse.asInstanceOf[T]
    }
  }
}
