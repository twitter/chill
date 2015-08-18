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

import _root_.java.io.{
  ByteArrayOutputStream,
  ByteArrayInputStream,
  ObjectInputStream,
  ObjectOutputStream
}

import com.twitter.bijection.Injection
import scala.util.Try

/**
 * An Injection to lift a type T into an Externalizer
 */
object ExternalizerInjection {
  implicit def builder[T]: ExternalizerInjection[T] = apply[T]

  def apply[T]: ExternalizerInjection[T] = new ExternalizerInjection[T]
}

class ExternalizerInjection[T] extends Injection[T, Externalizer[T]] {
  def apply(t: T): Externalizer[T] = {
    Externalizer(t)
  }
  def invert(extern: Externalizer[T]): Try[T] = Try {
    extern.get
  }
}
