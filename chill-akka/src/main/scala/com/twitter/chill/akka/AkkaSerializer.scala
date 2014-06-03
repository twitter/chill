/*
Copyright 2013 Twitter, Inc.

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

package com.twitter.chill.akka

import akka.actor.{ExtendedActorSystem, ActorRef}
import akka.serialization.Serializer

import com.twitter.chill._
import com.twitter.chill.config.ConfiguredInstantiator

import net.jpountz.lz4.LZ4Factory

/**
 * To use, add a key to your config like:
 *
 * {{{
 *
 *    akka.actor.serializers {
 *      kryo = "com.twitter.chill.akka.AkkaSerializer"
 *    }
 * }}}
 *
 * Then for the super-classes of all your message types,
 *   for instance, scala.Product, write:
 * {{{
 *    akka.actor.serialization-bindings {
 *      "scala.Product" = kryo
 *    }
 * }}}
 *
 * Kryo is not thread-safe so we use an object pool to avoid over allocating.
 */
class AkkaSerializer(system: ExtendedActorSystem) extends Serializer {

  /** You can override this to easily change your serializers.
   * If you do so, make sure to change the config to use the name of
   * your new class
   */
  def kryoInstantiator: KryoInstantiator =
    (new ScalaKryoInstantiator).withRegistrar(new ActorRefSerializer(system))

  /**
   * Since each thread only needs 1 Kryo, the pool doesn't need more
   * space than the number of threads. We guess that there are 4 hyperthreads /
   * core and then multiple by the nember of cores.
   */
  def poolSize: Int = {
    val GUESS_THREADS_PER_CORE = 4
    GUESS_THREADS_PER_CORE * Runtime.getRuntime.availableProcessors
  }

  val kryoPool: KryoPool =
    KryoPool.withByteArrayOutputStream(poolSize, kryoInstantiator)

  def includeManifest: Boolean = false
  def identifier = 8675309

  /**
   * Optional commpression of serialized values
   */
  val compression = if (system.settings.config.hasPath("akka.chill.compression"))
    system.settings.config.getBoolean("akka.chill.compression") else false

  /**
   * Use lazy val so that we do not depend on the lz4 jar it compression is not enabled
   */
  lazy val lz4factory = LZ4Factory.fastestInstance

  def compress(inputBuff: Array[Byte]): Array[Byte] = {
    val inputSize = inputBuff.length
    val lz4 = lz4factory.fastCompressor
    val maxOutputSize = lz4.maxCompressedLength(inputSize);
    val outputBuff = new Array[Byte](maxOutputSize + 4)
    val outputSize = lz4.compress(inputBuff, 0, inputSize, outputBuff, 4, maxOutputSize)

    // encode an Int lenght in the first 4 bytes
    outputBuff(0) = (inputSize       & 0xff).toByte
    outputBuff(1) = (inputSize >> 8  & 0xff).toByte
    outputBuff(2) = (inputSize >> 16 & 0xff).toByte
    outputBuff(3) = (inputSize >> 24 & 0xff).toByte
    outputBuff.take(outputSize+4)
  }

  def decompress(inputBuff: Array[Byte]): Array[Byte] = {
    // the first 4 bytes are the original size
    val size: Int = (inputBuff(0).asInstanceOf[Int] & 0xff)       |
                    (inputBuff(1).asInstanceOf[Int] & 0xff) << 8  |
                    (inputBuff(2).asInstanceOf[Int] & 0xff) << 16 |
                    (inputBuff(3).asInstanceOf[Int] & 0xff) << 24
    val lz4 = lz4factory.fastDecompressor()
    val outputBuff = new Array[Byte](size)
    lz4.decompress(inputBuff, 4, outputBuff, 0, size)
    outputBuff
  }

  def toBinary(obj: AnyRef): Array[Byte] =
    if (compression)
      compress(kryoPool.toBytesWithClass(obj))
    else
      kryoPool.toBytesWithClass(obj)

  def fromBinary(bytes: Array[Byte], clazz: Option[Class[_]]): AnyRef =
    kryoPool.fromBytes(if (compression) decompress(bytes) else bytes)
}

/** Uses the Config system of chill.config to Configure at runtime which KryoInstantiator to use
 * Overriding kryoInstantiator and using your own class name is probably easier for most cases.
 * See ConfiguredInstantiator static methods for how to build up a correct Config with
 * your reflected or serialized instantiators.
 */
class ConfiguredAkkaSerializer(system: ExtendedActorSystem) extends AkkaSerializer(system) {
  override def kryoInstantiator: KryoInstantiator =
    (new ConfiguredInstantiator(new AkkaConfig(system.settings.config)))
      .withRegistrar(new ActorRefSerializer(system))
}
