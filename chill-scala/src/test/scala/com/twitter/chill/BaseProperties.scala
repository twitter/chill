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

import java.io._
import com.twitter.bijection.Injection

trait BaseProperties {
  def serialize[T](t: T): Array[Byte] = KryoInjection(t.asInstanceOf[AnyRef])
  def deserialize[T](bytes: Array[Byte]): T =
    KryoInjection.invert(bytes).get.asInstanceOf[T]

  def rt[T](t: T): T = rt[T](KryoInjection, t)
  def rt[T](k: Injection[AnyRef, Array[Byte]], t: T): T = {
    val bytes = k(t.asInstanceOf[AnyRef])
    k.invert(bytes).get.asInstanceOf[T]
  }

  // using java serialization. TODO: remove when this is shipped in bijection
  def jserialize[T <: Serializable](t: T): Array[Byte] = {
    val bos = new ByteArrayOutputStream
    val out = new ObjectOutputStream(bos)
    try {
      out.writeObject(t)
      bos.toByteArray
    }
    finally {
      out.close
      bos.close
    }
  }
  def jdeserialize[T](bytes: Array[Byte])(implicit cmf: ClassManifest[T]): T = {
    val cls = cmf.erasure.asInstanceOf[Class[T]]
    val bis = new ByteArrayInputStream(bytes)
    val in = new ObjectInputStream(bis);
    try {
      cls.cast(in.readObject)
    }
    finally {
      bis.close
      in.close
   }
  }
  def jrt[T <: Serializable](t: T)(implicit cmf: ClassManifest[T]): T =
    jdeserialize(jserialize(t))
}
