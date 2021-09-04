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

import com.twitter.bijection.{Injection, Inversion}
import _root_.java.io.Serializable

import scala.util.Try

/**
 * A default KryoInjection that uses the ScalaKryoInstantiator with ByteArrayOutputStream as the backing
 * buffer
 */
object KryoInjection extends Injection[Any, Array[Byte]] {
  def apply(obj: Any): Array[Byte] = ScalaKryoInstantiator.defaultPool.toBytesWithClass(obj)
  def invert(b: Array[Byte]): Try[Any] = Inversion.attempt(b) {
    ScalaKryoInstantiator.defaultPool.fromBytes(_)
  }

  /**
   * Create a new KryoInjection instance that serializes items using the supplied KryoPool instance. For this
   * to be serializable, you need the call by name semantics
   */
  def instance(kryoPool: => KryoPool): Injection[Any, Array[Byte]] =
    new KryoInjectionInstance(kryoPool)

  /**
   * Creates a small pool (size 2) and uses it as an Injection Note the implicit in the package from () =>
   * Kryo to KryoInstatiator. It is ESSENTIAL that this function is allocating new Kryos, or we will not be
   * thread-safe
   */
  def instance(ki: KryoInstantiator, poolSize: Int = 2): Injection[Any, Array[Byte]] =
    new KryoInjectionInstance(KryoPool.withByteArrayOutputStream(poolSize, ki))
}

/**
 * Use this if you want to control the KryoPool. This is thread-safe since the KryoPool is
 */
class KryoInjectionInstance(lazyKryoP: => KryoPool) extends Injection[Any, Array[Byte]] {
  private val mutex = new AnyRef with Serializable // some serializable object
  @transient private var kpool: KryoPool = null

  private def kryoP: KryoPool = mutex.synchronized {
    if (null == kpool) {
      kpool = lazyKryoP
    }
    kpool
  }

  def apply(obj: Any): Array[Byte] = kryoP.toBytesWithClass(obj)
  def invert(b: Array[Byte]): Try[Any] = Inversion.attempt(b)(kryoP.fromBytes(_))
}
