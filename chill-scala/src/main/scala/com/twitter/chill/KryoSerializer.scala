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

import com.twitter.bijection.{ Bufferable, ImplicitBijection }

import scala.collection.immutable.{
  BitSet,
  ListMap,
  HashMap,
  Queue
}

import scala.collection.mutable.{
  WrappedArray,
  Map => MMap,
  Set => MSet,
  ListBuffer,
  Queue => MQueue,
  Buffer
}
import scala.util.matching.Regex

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
    newK
      .forSubclass[Regex](new RegexSerializer)
      // wrapper array is abstract
      .forSubclass[WrappedArray[Any]](new WrappedArraySerializer[Any])
      .forSubclass[BitSet](new BitSetSerializer)
      .forClass[Some[Any]](new SomeSerializer[Any])
      .forClass[Left[Any, Any]](new LeftSerializer[Any, Any])
      .forClass[Right[Any, Any]](new RightSerializer[Any, Any])
      .forTraversableSubclass(Queue.empty[Any])
      // List is a sealed class, so there are only two subclasses:
      .forTraversableSubclass(List.empty[Any])
      // add mutable Buffer before Vector, otherwise Vector is used
      .forTraversableSubclass(Buffer.empty[Any], isImmutable = false)
      // Vector is a final class
      .forTraversableClass(Vector.empty[Any])
      .forTraversableSubclass(IndexedSeq.empty[Any])
      // specifically register small sets since Scala represents them differently
      .forConcreteTraversableClass(Set[Any]('a))
      .forConcreteTraversableClass(Set[Any]('a, 'b))
      .forConcreteTraversableClass(Set[Any]('a, 'b, 'c))
      .forConcreteTraversableClass(Set[Any]('a, 'b, 'c, 'd))
      .forConcreteTraversableClass(Set[Any]('a, 'b, 'c, 'd, 'e))
      .forTraversableSubclass(Set.empty[Any])
      // specifically register small maps since Scala represents them differently
      .forConcreteTraversableClass(Map[Any, Any]('a -> 'a))
      .forConcreteTraversableClass(Map[Any, Any]('a -> 'a, 'b -> 'b))
      .forConcreteTraversableClass(Map[Any, Any]('a -> 'a, 'b -> 'b, 'c -> 'c))
      .forConcreteTraversableClass(Map[Any, Any]('a -> 'a, 'b -> 'b, 'c -> 'c, 'd -> 'd))
      .forConcreteTraversableClass(Map[Any, Any]('a -> 'a, 'b -> 'b, 'c -> 'c, 'd -> 'd, 'e -> 'e))
      // Add some maps
      .forTraversableSubclass(ListMap.empty[Any,Any])
      .forTraversableSubclass(HashMap.empty[Any,Any])
      // The above ListMap/HashMap must appear before this:
      .forTraversableSubclass(Map.empty[Any,Any])
      // here are the mutable ones:
      .forTraversableSubclass(MQueue.empty[Any], isImmutable = false)
      .forTraversableSubclass(MMap.empty[Any,Any], isImmutable = false)
      .forTraversableSubclass(MSet.empty[Any], isImmutable = false)
      .forTraversableSubclass(ListBuffer.empty[Any], isImmutable = false)
      // This should be last, lots of things are seq/iterable/traversable
      // These are questionable and might break things.
      // rarely will you only expect an iterable/traversable on the reverse
      .forTraversableSubclass(Seq.empty[Any])
      .forTraversableSubclass(Iterable.empty[Any])
      .forTraversableSubclass(Traversable.empty[Any])
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
