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

import com.twitter.bijection.{ Base64String, Bufferable, ImplicitBijection, Injection }

import org.objenesis.strategy.StdInstantiatorStrategy

import scala.collection.immutable.{
  BitSet,
  ListMap,
  HashMap,
  Queue
}

import scala.collection.mutable.{
  Builder,
  WrappedArray,
  Map => MMap,
  Set => MSet,
  ListBuffer,
  Queue => MQueue,
  Buffer
}

object KryoSerializer {

  import KryoImplicits.toRich //Add methods to Kryo

  // TODO: remove the registration methods, and use RichKryo

  def alreadyRegistered(k: Kryo, klass: Class[_]) =
    k.getClassResolver.getRegistration(klass) != null

  def registerInjections(newK: Kryo, pairs: TraversableOnce[InjectionPair[_]]) {
    pairs.foreach { pair: InjectionPair[_] =>
      if (!alreadyRegistered(newK, pair.klass)) {
        val serializer = InjectiveSerializer.asKryo(pair.injection)
        newK.register(pair.klass, serializer)
      } else {
        System.err.printf("%s is already registered in registerInjections.",
                          pair.klass.getName)
      }
    }
  }

  def registerInjectionDefaults(newK: Kryo, pairs: TraversableOnce[InjectionPair[_]]) {
    pairs.foreach { pair: InjectionPair[_] =>
      if (!alreadyRegistered(newK, pair.klass)) {
        val serializer = InjectiveSerializer.asKryo(pair.injection)
        newK.addDefaultSerializer(pair.klass, serializer)
        newK.register(pair.klass)
      } else {
        System.err.printf("%s is already registered in registerInjectionDefaults.",
                          pair.klass.getName)
      }
    }
  }

  def registerClasses(newK: Kryo, klasses: TraversableOnce[Class[_]]) {
    klasses.foreach { klass: Class[_] =>
      if (!alreadyRegistered(newK, klass))
        newK.register(klass)
    }
  }

  def registerCollectionSerializers(newK: Kryo) {
    /*
     * Note that subclass-based use: addDefaultSerializers, else: register
     * You should go from MOST specific, to least to specific when using
     * default serializers. The FIRST one found is the one used
     */
    // wrapper array is abstract
    newK.forSubclass[WrappedArray[Any]](new WrappedArraySerializer[Any])
      .forSubclass[BitSet](new BitSetSerializer)
      .forSubclass[java.util.PriorityQueue[AnyRef]](new PriorityQueueSerializer[AnyRef])
      .forTraversableSubclass(Queue.newBuilder[Any])
      // List is a sealed class, so there are only two subclasses:
      .forTraversableSubclass(List.newBuilder[Any])
      //Vector is a final class
      .forTraversableClass(Vector.newBuilder[Any])
      .forTraversableSubclass(IndexedSeq.newBuilder[Any])
      .forTraversableSubclass(Set.newBuilder[Any])
      // Add some maps
      .forTraversableSubclass(ListMap.newBuilder[Any,Any])
      .forTraversableSubclass(HashMap.newBuilder[Any,Any])
      // The above ListMap/HashMap must appear before this:
      .forTraversableSubclass(Map.newBuilder[Any,Any])
      // here are the mutable ones:
      .forTraversableSubclass(MQueue.newBuilder[Any], isImmutable = false)
      .forTraversableSubclass(MMap.newBuilder[Any,Any], isImmutable = false)
      .forTraversableSubclass(MSet.newBuilder[Any], isImmutable = false)
      .forTraversableSubclass(ListBuffer.newBuilder[Any], isImmutable = false)
      .forTraversableSubclass(Buffer.newBuilder[Any], isImmutable = false)
      // This should be last, lots of things are seq/iterable/traversable
      // These are questionable and might break things.
      // rarely will you only expect an iterable/traversable on the reverse
      .forTraversableSubclass(Seq.newBuilder[Any])
      .forTraversableSubclass(Iterable.newBuilder[Any])
      .forTraversableSubclass(Traversable.newBuilder[Any])
  }

  def registerAll(k: Kryo) {
    registerCollectionSerializers(k)
    // Register all 22 tuple serializers and specialized serializers
    ScalaTupleSerialization.register(k)
    k.forClassViaBijection[Symbol, String]
      .forClass[ClassManifest[Any]](new ClassManifestSerializer[Any])
      .forSubclass[Manifest[Any]](new ManifestSerializer[Any])
      .forSubclass[scala.Enumeration#Value](new EnumerationSerializer)
  }

  /** Use a bijection[A,B] then the KSerializer on B
   */
  def viaBijection[A,B](kser: KSerializer[B])(implicit bij: ImplicitBijection[A,B], cmf: ClassManifest[B]): KSerializer[A] =
    new KSerializer[A] {
      def write(k: Kryo, out: Output, obj: A) { kser.write(k, out, bij(obj)) }
      def read(k: Kryo, in: Input, cls: Class[A]) =
        bij.invert(kser.read(k, in, cmf.erasure.asInstanceOf[Class[B]]))
    }

  def viaBufferable[T](implicit b: Bufferable[T]): KSerializer[T] =
    InjectiveSerializer.asKryo[T](Bufferable.injectionOf[T])
}
