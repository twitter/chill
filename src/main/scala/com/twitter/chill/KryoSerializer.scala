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

import com.twitter.bijection.{ Base64String, Bijection }

import org.objenesis.strategy.StdInstantiatorStrategy

import scala.collection.immutable.{
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
  def alreadyRegistered(k: Kryo, klass: Class[_]) =
    k.getClassResolver.getRegistration(klass) != null

  def registerBijections(newK: Kryo, pairs: TraversableOnce[BijectionPair[_]]) {
    pairs.foreach { pair: BijectionPair[_] =>
      if (!alreadyRegistered(newK, pair.klass)) {
        val serializer = BijectiveSerializer.asKryo(pair.bijection)
        newK.register(pair.klass, serializer)
      } else {
        System.err.printf("%s is already registered in registerBijections.",
                          Array[String](pair.klass.getName))
      }
    }
  }

  def registerBijectionDefaults(newK: Kryo, pairs: TraversableOnce[BijectionPair[_]]) {
    pairs.foreach { pair: BijectionPair[_] =>
      if (!alreadyRegistered(newK, pair.klass)) {
        val serializer = BijectiveSerializer.asKryo(pair.bijection)
        newK.addDefaultSerializer(pair.klass, serializer)
        newK.register(pair.klass)
      } else {
        System.err.printf("%s is already registered in registerBijectionDefaults.",
                          Array[String](pair.klass.getName))
      }
    }
  }

  def registerClasses(newK: Kryo, klasses: TraversableOnce[Class[_]]) {
    klasses.foreach { klass: Class[_] =>
      if (!alreadyRegistered(newK, klass))
        newK.register(klass)
    }
  }

  def addDefaultTraversable[T, C <: Traversable[T]](k: Kryo, b: Builder[T,C],
    isImmutable: Boolean = true)
    (implicit mf: ClassManifest[C]) {
    k.addDefaultSerializer(mf.erasure, new TraversableSerializer(b, isImmutable))
  }

  def registerTraversable[T, C <: Traversable[T]](k: Kryo, b: Builder[T,C])
    (implicit mf: ClassManifest[C]) {
    k.register(mf.erasure, new TraversableSerializer(b))
  }

  /** Use Java serialization, which is very slow.
   * avoid this if possible, but for very rare classes it is probably fine
   */
  def useJava[T<:java.io.Serializable](k: Kryo)(implicit cmf: ClassManifest[T]) {
    k.register(cmf.erasure, new com.esotericsoftware.kryo.serializers.JavaSerializer)
  }

  def registerCollectionSerializers(newK: Kryo) {
    /*
     * Note that subclass-based use: addDefaultSerializers, else: register
     * You should go from MOST specific, to least to specific when using
     * default serializers. The FIRST one found is the one used
     */
    // wrapper array is abstract
    newK.addDefaultSerializer(classOf[WrappedArray[Any]], new WrappedArraySerializer[Any])

    addDefaultTraversable(newK, Queue.newBuilder[Any])
    // List is a sealed class, so there are only two subclasses:
    addDefaultTraversable(newK, List.newBuilder[Any])
    //Vector is a final class
    registerTraversable(newK, Vector.newBuilder[Any])
    addDefaultTraversable(newK, IndexedSeq.newBuilder[Any])
    addDefaultTraversable(newK, Set.newBuilder[Any])

    // Add some maps
    addDefaultTraversable(newK, ListMap.newBuilder[Any,Any])
    addDefaultTraversable(newK, HashMap.newBuilder[Any,Any])

    // The above ListMap/HashMap must appear before this:
    addDefaultTraversable(newK, Map.newBuilder[Any,Any])

    // here are the mutable ones:
    addDefaultTraversable(newK, MQueue.newBuilder[Any], isImmutable = false)
    addDefaultTraversable(newK, MMap.newBuilder[Any,Any], isImmutable = false)
    addDefaultTraversable(newK, MSet.newBuilder[Any], isImmutable = false)
    addDefaultTraversable(newK, ListBuffer.newBuilder[Any], isImmutable = false)
    addDefaultTraversable(newK, Buffer.newBuilder[Any], isImmutable = false)

    // This should be last, lots of things are seq/iterable/traversable
    // These are questionable and might break things.
    // rarely will you only expect an iterable/traversable on the reverse
    addDefaultTraversable(newK, Seq.newBuilder[Any])
    addDefaultTraversable(newK, Iterable.newBuilder[Any])
    addDefaultTraversable(newK, Traversable.newBuilder[Any])
  }

  def registerAll(k: Kryo) {
    registerCollectionSerializers(k)
    // Register all 22 tuple serializers and specialized serializers
    ScalaTupleSerialization.register(k)
    k.register(classOf[Symbol], new SymbolSerializer)
    k.register(classOf[ClassManifest[_]], new ClassManifestSerializer[Any])
    k.addDefaultSerializer(classOf[Manifest[_]], new ManifestSerializer[Any])
    k.addDefaultSerializer(classOf[scala.Enumeration$Value], new EnumerationSerializer)
  }

  /** B has to already be registered
   */
  def registerViaBijection[A,B](k: Kryo)(implicit bij: Bijection[A,B], cmf: ClassManifest[B]) {
    val cls = cmf.erasure
    val kserb = k.getSerializer(cls).asInstanceOf[KSerializer[B]]
    k.register(cls, viaBijection[A,B](kserb))
  }

  /** Use a bijection[A,B] then the KSerializer on B
   */
  def viaBijection[A,B](kser: KSerializer[B])
    (implicit bij: Bijection[A,B], cmf: ClassManifest[B]): KSerializer[A] =
    new KSerializer[A] {
      def write(k: Kryo, out: Output, obj: A) { kser.write(k, out, bij(obj)) }
      def read(k: Kryo, in: Input, cls: Class[A]) =
        bij.invert(kser.read(k, in, cmf.erasure.asInstanceOf[Class[B]]))
    }
}

// TODO: Cache the kryo returned by getKryo.
object KryoBijection extends Bijection[AnyRef, Array[Byte]] with KryoSerializer {
  override def apply(obj: AnyRef): Array[Byte] = serialize(obj)
  override def invert(bytes: Array[Byte]) = deserialize[AnyRef](bytes)
}

@deprecated("Use com.twitter.chill.KryoBijection instead", "0.1.0")
trait KryoSerializer {
  def getKryo : Kryo = {
    val k = new Kryo {
      lazy val objSer = new ObjectSerializer[AnyRef]

      override def getDefaultSerializer(klass : Class[_]) : KSerializer[_] = {
        if(isSingleton(klass))
          objSer
        else
          super.getDefaultSerializer(klass)
      }

      def isSingleton(klass : Class[_]) : Boolean = {
        classOf[scala.ScalaObject].isAssignableFrom(klass) &&
          klass.getName.last == '$'
      }
    }

    k.setRegistrationRequired(false)
    k.setInstantiatorStrategy(new StdInstantiatorStrategy)
    KryoSerializer.registerAll(k)
    k
  }

  def serialize(ag : AnyRef) : Array[Byte] = {
    val output = new Output(1 << 12, 1 << 30)
    getKryo.writeClassAndObject(output, ag)
    output.toBytes
  }

  def deserialize[T](bytes : Array[Byte]) : T = {
    getKryo.readClassAndObject(new Input(bytes))
      .asInstanceOf[T]
  }

  def serializeBase64(ag: AnyRef): String =
    Bijection[Array[Byte], Base64String](serialize(ag)).str

  def deserializeBase64[T](str: String): T =
    deserialize[T](Bijection.invert[Array[Byte], Base64String](Base64String(str)))
}
