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

trait KryoBijection extends Bijection[AnyRef, Array[Byte]] {
  def getKryo: Kryo = {
    val k = new KryoBase
    k.setRegistrationRequired(false)
    k.setInstantiatorStrategy(new StdInstantiatorStrategy)
    KryoSerializer.registerAll(k)
    k
  }

  override def apply(obj: AnyRef): Array[Byte] = {
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
object KryoInjection extends Injection[AnyRef, Array[Byte]] {
  override def apply(obj: AnyRef) = KryoBijection(obj)
  override def invert(bytes: Array[Byte]) = allCatch.opt(KryoBijection.invert(bytes))
}
