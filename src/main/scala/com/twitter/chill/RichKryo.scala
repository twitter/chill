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

import com.twitter.bijection.{ Bijection, Bufferable }

import scala.collection.mutable.Builder

/** Enrichment pattern to add methods to Kryo objects
 * TODO: make this a value-class in scala 2.10
 * This also follows the builder pattern to allow easily chaining this calls
 */
class RichKryo(k: Kryo) {
  def alreadyRegistered[T](implicit cmf: ClassManifest[T]): Boolean =
    k.getClassResolver.getRegistration(cmf.erasure) != null

  def bijectionForClass[T](implicit bij: Bijection[T, Array[Byte]], cmf: ClassManifest[T]):
    Kryo = {
      k.register(cmf.erasure, BijectiveSerializer.asKryo[T])
      k
    }

  def bijectionForClasses(pairs: TraversableOnce[BijectionPair[_]]): Kryo = {
    pairs.foreach { pair: BijectionPair[_] =>
      if (!alreadyRegistered(ClassManifest.fromClass(pair.klass))) {
        val serializer = BijectiveSerializer.asKryo(pair.bijection)
        k.register(pair.klass, serializer)
      } else {
        System.err.printf("%s is already registered in registerBijections.",
                          Array[String](pair.klass.getName))
      }
    }
    k
  }

  def bijectionForSubclass[T](implicit bij: Bijection[T, Array[Byte]], cmf: ClassManifest[T]):
    Kryo = {
      k.addDefaultSerializer(cmf.erasure, BijectiveSerializer.asKryo[T])
      k
    }

  def bijectionForSubclasses(pairs: TraversableOnce[BijectionPair[_]]): Kryo = {
    pairs.foreach { pair: BijectionPair[_] =>
      if (!alreadyRegistered(ClassManifest.fromClass(pair.klass))) {
        val serializer = BijectiveSerializer.asKryo(pair.bijection)
        k.addDefaultSerializer(pair.klass, serializer)
        k.register(pair.klass)
      } else {
        System.err.printf("%s is already registered in registerBijectionDefaults.",
                          Array[String](pair.klass.getName))
      }
    }
    k
  }

  def bufferableForClass[T](implicit b: Bufferable[T], cmf: ClassManifest[T]): Kryo = {
    k.register(cmf.erasure, KryoSerializer.viaBufferable[T])
    k
  }

  def forSubclass[T](kser: KSerializer[T])(implicit cmf: ClassManifest[T]): Kryo = {
    k.addDefaultSerializer(cmf.erasure, kser)
    k
  }

  def forTraversableSubclass[T, C <: Traversable[T]](b: Builder[T,C], isImmutable: Boolean = true)
    (implicit mf: ClassManifest[C]): Kryo = {
    k.addDefaultSerializer(mf.erasure, new TraversableSerializer(b, isImmutable))
    k
  }

  def forClass[T](kser: KSerializer[T])(implicit cmf: ClassManifest[T]): Kryo = {
    k.register(cmf.erasure, kser)
    k
  }

  def forTraversableClass[T, C <: Traversable[T]](b: Builder[T,C], isImmutable: Boolean = true)
    (implicit mf: ClassManifest[C]): Kryo =
    forClass(new TraversableSerializer(b, isImmutable))(mf)

  /** B has to already be registered, then use the KSerializer[B] to create KSerialzer[A]
   */
  def forClassViaBijection[A,B]
    (implicit bij: Bijection[A,B], acmf: ClassManifest[A], bcmf: ClassManifest[B]): Kryo = {
    val kserb = k.getSerializer(bcmf.erasure).asInstanceOf[KSerializer[B]]
    k.register(acmf.erasure, KryoSerializer.viaBijection[A,B](kserb))
    k
  }

  /** Use Java serialization, which is very slow.
   * avoid this if possible, but for very rare classes it is probably fine
   */
  def javaForClass[T<:java.io.Serializable](implicit cmf: ClassManifest[T]): Kryo = {
    k.register(cmf.erasure, new com.esotericsoftware.kryo.serializers.JavaSerializer)
    k
  }
  /** Use Java serialization, which is very slow.
   * avoid this if possible, but for very rare classes it is probably fine
   */
  def javaForSubclass[T<:java.io.Serializable](implicit cmf: ClassManifest[T]): Kryo = {
    k.addDefaultSerializer(cmf.erasure, new com.esotericsoftware.kryo.serializers.JavaSerializer)
    k
  }

  def registerClasses(klasses: TraversableOnce[Class[_]]): Kryo = {
    klasses.foreach { klass: Class[_] =>
      if (!alreadyRegistered(ClassManifest.fromClass(klass)))
        k.register(klass)
    }
    k
  }
}

object KryoImplicits {
  implicit def toRich(k: Kryo): RichKryo = new RichKryo(k)
}
