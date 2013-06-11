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
import com.esotericsoftware.kryo.io.{ ByteBufferInputStream, Input, Output }

import com.twitter.bijection.{ Bufferable, Bijection, ImplicitBijection, Injection }

import java.io.InputStream
import java.nio.ByteBuffer
import java.util.{ Map => JMap }

import scala.collection.generic.CanBuildFrom
import scala.util.control.Exception.allCatch

/** Enrichment pattern to add methods to Kryo objects
 * TODO: make this a value-class in scala 2.10
 * This also follows the builder pattern to allow easily chaining this calls
 */
class RichKryo(k: Kryo) {
  def alreadyRegistered[T](implicit cmf: ClassManifest[T]): Boolean =
    k.getClassResolver.getRegistration(cmf.erasure) != null

  def injectionForClass[T](implicit inj: Injection[T, Array[Byte]], cmf: ClassManifest[T]): Kryo = {
    k.register(cmf.erasure, InjectiveSerializer.asKryo[T])
    k
  }

  def injectionForClasses(pairs: TraversableOnce[InjectionPair[_]]): Kryo = {
    pairs.foreach { pair: InjectionPair[_] =>
      if (!alreadyRegistered(ClassManifest.fromClass(pair.klass))) {
        val serializer = InjectiveSerializer.asKryo(pair.injection)
        k.register(pair.klass, serializer)
      } else {
        System.err.printf("%s is already registered in injectionForClasses.", pair.klass.getName)
      }
    }
    k
  }

  def injectionForSubclass[T](implicit inj: Injection[T, Array[Byte]], cmf: ClassManifest[T]): Kryo = {
    k.addDefaultSerializer(cmf.erasure, InjectiveSerializer.asKryo[T])
    k
  }

  def injectionForSubclasses(pairs: TraversableOnce[InjectionPair[_]]): Kryo = {
    pairs.foreach { pair: InjectionPair[_] =>
      if (!alreadyRegistered(ClassManifest.fromClass(pair.klass))) {
        val serializer = InjectiveSerializer.asKryo(pair.injection)
        k.addDefaultSerializer(pair.klass, serializer)
        k.register(pair.klass)
      } else {
        System.err.printf("%s is already registered in injectionForSubclasses.", pair.klass.getName)
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

  def forTraversableSubclass[T, C <: Traversable[T]](c: C with Traversable[T], isImmutable: Boolean = true)
    (implicit mf: ClassManifest[C], cbf: CanBuildFrom[C, T, C]): Kryo = {
    k.addDefaultSerializer(mf.erasure, new TraversableSerializer(isImmutable)(cbf))
    k
  }

  def forClass[T](kser: KSerializer[T])(implicit cmf: ClassManifest[T]): Kryo = {
    k.register(cmf.erasure, kser)
    k
  }

  def forTraversableClass[T, C <: Traversable[T]](c: C with Traversable[T], isImmutable: Boolean = true)
    (implicit mf: ClassManifest[C], cbf: CanBuildFrom[C, T, C]): Kryo =
    forClass(new TraversableSerializer(isImmutable)(cbf))

  def forConcreteTraversableClass[T, C <: Traversable[T]](c: C with Traversable[T], isImmutable: Boolean = true)
    (implicit cbf: CanBuildFrom[C, T, C]): Kryo = {
    // a ClassManifest is not used here since its erasure method does not return the concrete internal type
    // that Scala uses for small immutable maps (i.e., scala.collection.immutable.Map$Map1)
    k.register(c.getClass, new TraversableSerializer(isImmutable)(cbf))
    k
  }

  /** B has to already be registered, then use the KSerializer[B] to create KSerialzer[A]
   */
  def forClassViaBijection[A,B](implicit bij: ImplicitBijection[A,B], acmf: ClassManifest[A], bcmf: ClassManifest[B]): Kryo = {
    val kserb = k.getSerializer(bcmf.erasure).asInstanceOf[KSerializer[B]]
    k.register(acmf.erasure, KryoSerializer.viaBijection[A,B](kserb))
    k
  }

  /** Helpful override to alleviate rewriting types. */
  def forClassViaBijection[A,B](bij: Bijection[A,B])(implicit acmf: ClassManifest[A], bcmf: ClassManifest[B]): Kryo = {
    implicit def implicitBij = bij
    this.forClassViaBijection[A, B]
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

  /**
    * Populate the wrapped KryoInstance with Injections registered
    * within the supplied configuration map (using the methods defined
    * in KryoRegistrationHelper).
    */
  def populateFromConfig(conf: JMap[_,_]): Kryo = {
    KryoRegistrationHelper.registerInjections(k, conf)
    KryoRegistrationHelper.registerInjectionDefaults(k, conf)
    KryoRegistrationHelper.registerKryoClasses(k, conf)
    k
  }

  def fromInputStream(s: InputStream): Option[AnyRef] = {
    // Can't reuse Input and call Input#setInputStream everytime
    val streamInput = new Input(s)
    allCatch.opt(k.readClassAndObject(streamInput))
  }

  def fromByteBuffer(b: ByteBuffer): Option[AnyRef] = {
    // Can't reuse Input and call Input#setInputStream everytime
    val s           = new ByteBufferInputStream(b)
    val streamInput = new Input(s)
    allCatch.opt(k.readClassAndObject(streamInput))
  }
}

object KryoImplicits {
  implicit def toRich(k: Kryo): RichKryo = new RichKryo(k)
}
