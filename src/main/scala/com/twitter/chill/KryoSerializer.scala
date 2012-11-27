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

import scala.collection.immutable.ListMap
import scala.collection.immutable.HashMap
import scala.collection.mutable.WrappedArray

object KryoSerializer {
  def alreadyRegistered(k: Kryo, klass: Class[_]) =
    k.getClassResolver.getRegistration(klass) != null

  def registerBijections(newK: Kryo, pairs: TraversableOnce[BijectionPair[_]]) {
    pairs.foreach { pair: BijectionPair[_] =>
      if (!alreadyRegistered(newK, pair.klass)) {
        val serializer = BijectiveSerializer.asKryo(pair.bijection)
        newK.register(pair.klass)
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

  def registerCollectionSerializers(newK: Kryo) {
    newK.register(classOf[Symbol], new SymbolSerializer)

    // Subclass-based (addDefaultSerializers)

    /* Note that you should go from MOST specific, to least to specific when using
     * default serializers. The FIRST one found is the one used
     */
    // wrapper array is abstract
    newK.addDefaultSerializer(classOf[WrappedArray[Any]], new WrappedArraySerializer[Any])

    // List is a sealed class, so there are only two subclasses:
    newK.addDefaultSerializer(classOf[List[Any]],
      new ListSerializer[AnyRef,List[AnyRef]](List[AnyRef]()))
    //Vector is a final class
    newK.addDefaultSerializer(classOf[Vector[Any]], new VectorSerializer[Any])
    newK.addDefaultSerializer(classOf[Set[Any]], new SetSerializer[Any,Set[Any]](Set[Any]()))
    // Add some maps
    newK.addDefaultSerializer(classOf[ListMap[Any,Any]],
      new MapSerializer[Any,Any,ListMap[Any,Any]](ListMap[Any,Any]()))
    newK.addDefaultSerializer(classOf[HashMap[Any,Any]],
      new MapSerializer[Any,Any,HashMap[Any,Any]](HashMap[Any,Any]()))
    // The above ListMap/HashMap must appear before this:
    newK.addDefaultSerializer(classOf[Map[Any,Any]],
      new MapSerializer[Any,Any,Map[Any,Any]](Map[Any,Any]()))
  }

  def registerAll(k: Kryo) {
    registerCollectionSerializers(k)
    // Register all 22 tuple serializers and specialized serializers
    ScalaTupleSerialization.register(k)
    k.register(classOf[ClassManifest[_]], new ClassManifestSerializer[Any])
    k.addDefaultSerializer(classOf[Manifest[_]], new ManifestSerializer[Any])
    k.addDefaultSerializer(classOf[scala.Enumeration$Value], new EnumerationSerializer)
  }
}

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
