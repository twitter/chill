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

import _root_.java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import com.twitter.bijection.Injection
import scala.util.Try

/**
 * A Injection to serialize an externalizer to bytes
 *
 * This method isn't safe since calling get on an Externalizer[T] could still throw despite the inversion
 * here having worked as advertized. Since it is at the point of the get we unpack the inner box.
 */
object ExternalizerCodec {
  implicit def apply[T]: ExternalizerCodec[T] = new ExternalizerCodec[T]
}

class ExternalizerCodec[T] extends Injection[Externalizer[T], Array[Byte]] {
  def apply(extern: Externalizer[T]): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(baos)
    oos.writeObject(extern)
    baos.toByteArray
  }
  def invert(bytes: Array[Byte]): Try[Externalizer[T]] = Try {
    val testInput = new ByteArrayInputStream(bytes)
    val ois = new ObjectInputStream(testInput)
    ois.readObject.asInstanceOf[Externalizer[T]] // this may throw
  }
}
