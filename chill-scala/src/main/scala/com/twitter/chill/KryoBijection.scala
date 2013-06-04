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
import com.esotericsoftware.kryo.io.{ Input, Output }
import com.twitter.bijection.{ Bijection, Injection }
import org.objenesis.strategy.StdInstantiatorStrategy

import scala.util.control.Exception.allCatch

/**
 * KryoBijection is split into a trait and companion object to allow
 * users to override KryoBijection's getKryo method.
 *
 * TODO: Cache the kryo returned by getKryo.
 */

object KryoBijection extends KryoBijection

trait KryoBijection extends Bijection[Any, Array[Byte]] {
  def getKryo: Kryo = {
    val k = new KryoBase
    k.setRegistrationRequired(false)
    k.setInstantiatorStrategy(new StdInstantiatorStrategy)
    KryoSerializer.registerAll(k)
    k
  }

  override def apply(obj: Any): Array[Byte] = {
    val output = new Output(1 << 12, 1 << 30)
    getKryo.writeClassAndObject(output, obj)
    output.toBytes
  }

  override def invert(bytes: Array[Byte]) =
    getKryo.readClassAndObject(new Input(bytes))

}

/**
 * TODO: Delete KryoBijection, use KryoInjection everywhere.
 */
object KryoInjection extends Injection[Any, Array[Byte]] {
  // Create a default injection to use, 4KB init, max 16 MB
  private val kinject = instance(init = 1 << 12, max = 1 << 24)

  override def apply(obj: Any) = kinject.synchronized { kinject(obj) }
  override def invert(bytes: Array[Byte]) = kinject.synchronized { kinject.invert(bytes) }

  /**
   * Create a new KryoInjection instance that serializes items using
   * the supplied Kryo instance. The buffer used for serialization is
   * initialized, by default, to 1KB, with a max size of
   * 16MB. Configure these limits by passing in new values for "init"
   * and "max" respectively.
   */
  def instance(
    kryo: Kryo = KryoBijection.getKryo,
    init: Int = 1 << 10,
    max: Int = 1 << 24
  ): Injection[Any, Array[Byte]] =
    new KryoInjectionInstance(kryo, new Output(init, max))
}

/**
 * Reuse the Output and Kryo, which is faster
 * register any additional serializers you need before passing in the
 * Kryo instance
 */
class KryoInjectionInstance(kryo: Kryo, output: Output) extends Injection[Any, Array[Byte]] {
  private val input: Input = new Input

  def apply(obj: Any): Array[Byte] = {
    output.clear
    kryo.writeClassAndObject(output, obj)
    output.toBytes
  }

  def invert(b: Array[Byte]): Option[Any] = {
    input.setBuffer(b)
    allCatch.opt(kryo.readClassAndObject(input))
  }
}
