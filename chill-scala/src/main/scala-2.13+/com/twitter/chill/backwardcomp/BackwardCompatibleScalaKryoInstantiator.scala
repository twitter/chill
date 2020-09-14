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

package com.twitter.chill.backwardcomp

import com.esotericsoftware.kryo.util.MapReferenceResolver
import com.twitter.chill.java.{Java8ClosureRegistrar, PackageRegistrar}
import com.twitter.chill._

import scala.collection.JavaConverters._
import scala.collection.immutable.{
  BitSet,
  HashMap,
  HashSet,
  ListMap,
  ListSet,
  NumericRange,
  Queue,
  Range,
  SortedMap,
  SortedSet,
  TreeMap,
  TreeSet,
  WrappedString
}
import scala.collection.mutable
import scala.collection.mutable.{
  Buffer,
  ListBuffer,
  WrappedArray,
  BitSet => MBitSet,
  HashMap => MHashMap,
  HashSet => MHashSet,
  Map => MMap,
  Queue => MQueue,
  Set => MSet
}
import scala.reflect.ClassTag
import scala.util.matching.Regex

/** Makes an empty instantiator then registers everything */
class BackwardCompatibleScalaKryoInstantiator extends EmptyScalaKryoInstantiator {
  override def newKryo: KryoBase = {
    val k = new KryoBase(new BackwardCompatibleClassResolver, new MapReferenceResolver)
    k.setRegistrationRequired(false)
    k.setInstantiatorStrategy(new org.objenesis.strategy.StdInstantiatorStrategy)

    // Handle cases where we may have an odd classloader setup like with libjars
    // for hadoop
    val classLoader = Thread.currentThread.getContextClassLoader
    k.setClassLoader(classLoader)

    val reg = new BackwardCompatibleAllScalaRegistrar
    reg(k)
    k
  }
}

/**
 * Note that additional scala collections registrations are provided by [[AllScalaRegistrar]]. They have not been
 * included in this registrar for backwards compatibility reasons.
 */
class BackwardCompatibleScalaCollectionsRegistrar extends IKryoRegistrar {
  def apply(newK: Kryo): Unit = {
    // for binary compat this is here, but could be moved to RichKryo
    def useField[T](cls: Class[T]): Unit = {
      val fs = new BackwardCompatibleJavaWrappersSerializer(newK, cls)
      fs.setIgnoreSyntheticFields(false) // scala generates a lot of these attributes
      newK.register(cls, fs)
    }
    // The wrappers are private classes:
    useField(List(1, 2, 3).asJava.getClass)
    useField(List(1, 2, 3).iterator.asJava.getClass)
    useField(Map(1 -> 2, 4 -> 3).asJava.getClass)
    useField(new _root_.java.util.ArrayList().asScala.getClass)
    useField(new _root_.java.util.HashMap().asScala.getClass)

    /*
     * Note that subclass-based use: addDefaultSerializers, else: register
     * You should go from MOST specific, to least to specific when using
     * default serializers. The FIRST one found is the one used
     */
    newK
      // wrapper array is abstract
      .forSubclass[WrappedArray[Any]](new WrappedArraySerializer[Any])
      .forSubclass[BitSet](new BitSetSerializer)
      .forSubclass[SortedSet[Any]](new SortedSetSerializer)
      .forClass[Some[Any]](new SomeSerializer[Any])
      .forClass[Left[Any, Any]](new LeftSerializer[Any, Any])
      .forClass[Right[Any, Any]](new RightSerializer[Any, Any])
      .forTraversableSubclass(Queue.empty[Any])
      // List is a sealed class, so there are only two subclasses:
      .forTraversableSubclass(List.empty[Any])
      // Add ListBuffer subclass before Buffer to prevent the more general case taking precedence
      .forTraversableSubclass(ListBuffer.empty[Any], isImmutable = false)
      // add mutable Buffer before Vector, otherwise Vector is used
      .forTraversableSubclass(Buffer.empty[Any], isImmutable = false)
      // Vector is a final class
      .forTraversableClass(Vector.empty[Any])
      .forTraversableSubclass(ListSet.empty[Any])
      // specifically register small sets since Scala represents them differently
      .forConcreteTraversableClass(Set[Any]('a))
      .forConcreteTraversableClass(Set[Any]('a, 'b))
      .forConcreteTraversableClass(Set[Any]('a, 'b, 'c))
      .forConcreteTraversableClass(Set[Any]('a, 'b, 'c, 'd))
      // default set implementation
      .forConcreteTraversableClass(HashSet[Any]('a, 'b, 'c, 'd, 'e))
      // specifically register small maps since Scala represents them differently
      .forConcreteTraversableClass(Map[Any, Any]('a -> 'a))
      .forConcreteTraversableClass(Map[Any, Any]('a -> 'a, 'b -> 'b))
      .forConcreteTraversableClass(Map[Any, Any]('a -> 'a, 'b -> 'b, 'c -> 'c))
      .forConcreteTraversableClass(Map[Any, Any]('a -> 'a, 'b -> 'b, 'c -> 'c, 'd -> 'd))
      // default map implementation
      .forConcreteTraversableClass(HashMap[Any, Any]('a -> 'a, 'b -> 'b, 'c -> 'c, 'd -> 'd, 'e -> 'e))
      // The normal fields serializer works for ranges
      .registerClasses(
        Seq(classOf[Range.Inclusive], classOf[NumericRange.Inclusive[_]])
      )

    newK.register(
      classOf[NumericRange.Exclusive[_]],
      new BackwardCompatibleExclusiveNumericRangeSerializer(newK, classOf[NumericRange.Exclusive[_]])
    )
    // Add some maps

    newK
      .forSubclass[SortedMap[Any, Any]](new SortedMapSerializer)
      .forTraversableSubclass(ListMap.empty[Any, Any])
      .forTraversableSubclass(HashMap.empty[Any, Any])
      // The above ListMap/HashMap must appear before this:
      .forTraversableSubclass(Map.empty[Any, Any])
      // here are the mutable ones:
      .forTraversableClass(MBitSet.empty, isImmutable = false)
      .forTraversableClass(MHashMap.empty[Any, Any], isImmutable = false)
      .forTraversableClass(MHashSet.empty[Any], isImmutable = false)
      .forTraversableSubclass(MQueue.empty[Any], isImmutable = false)
      .forTraversableSubclass(MMap.empty[Any, Any], isImmutable = false)
      .forTraversableSubclass(MSet.empty[Any], isImmutable = false)
  }
}

/** Registrar for everything that was registered in chill 0.9.2 - included for backwards compatibility. */
class BackwardCompatibleAllScalaRegistrar_0_9_2 extends IKryoRegistrar {
  def apply(k: Kryo): Unit = {
    new BackwardCompatibleScalaCollectionsRegistrar()(k)
    new JavaWrapperCollectionRegistrar()(k)

    // Register all 22 tuple serializers and specialized serializers
    ScalaTupleSerialization.register(k)
    k.forClass[Symbol](new KSerializer[Symbol] {
      override def isImmutable = true
      def write(k: Kryo, out: Output, obj: Symbol): Unit = out.writeString(obj.name)
      def read(k: Kryo, in: Input, cls: Class[Symbol]): Symbol = Symbol(in.readString)
    }).forSubclass[Regex](new RegexSerializer)
      .forClass[ClassTag[Any]](new ClassTagSerializer[Any])
      .forSubclass[Manifest[Any]](new ManifestSerializer[Any])
      .forSubclass[scala.Enumeration#Value](new EnumerationSerializer)

    // use the singleton serializer for boxed Unit
    val boxedUnit = scala.runtime.BoxedUnit.UNIT
    k.register(boxedUnit.getClass, new SingletonSerializer(boxedUnit))
    PackageRegistrar.all()(k)
    new Java8ClosureRegistrar()(k)
  }
}

/**
 * Registers all the scala (and java) serializers we have. The registrations are designed to cover most of
 * scala.collecion.immutable, so they can be used in long term persistence scenarios that run with
 * setRegistrationRequired(true).
 *
 * When adding new serializers, add them to the end of the list, so compatibility is not broken needlessly
 * for projects using chill for long term persistence - see com.twitter.chill.RegistrationIdsSpec.
 */
class BackwardCompatibleAllScalaRegistrar extends IKryoRegistrar {
  def apply(k: Kryo): Unit = {
    new BackwardCompatibleAllScalaRegistrar_0_9_2()(k)

    k.registerClasses(
      Seq(
        classOf[Array[Byte]],
        classOf[Array[Short]],
        classOf[Array[Int]],
        classOf[Array[Long]],
        classOf[Array[Float]],
        classOf[Array[Double]],
        classOf[Array[Boolean]],
        classOf[Array[Char]],
        classOf[Array[String]],
        classOf[Array[Any]],
        classOf[Class[_]], // needed for the WrappedArraySerializer
        classOf[Any], // needed for scala.collection.mutable.WrappedArray$ofRef
        mutable.WrappedArray.make(Array[Byte]()).getClass,
        mutable.WrappedArray.make(Array[Short]()).getClass,
        mutable.WrappedArray.make(Array[Int]()).getClass,
        mutable.WrappedArray.make(Array[Long]()).getClass,
        mutable.WrappedArray.make(Array[Float]()).getClass,
        mutable.WrappedArray.make(Array[Double]()).getClass,
        mutable.WrappedArray.make(Array[Boolean]()).getClass,
        mutable.WrappedArray.make(Array[Char]()).getClass,
        mutable.WrappedArray.make(Array[String]()).getClass,
        None.getClass,
        classOf[Queue[_]],
        Nil.getClass,
        classOf[::[_]],
        classOf[Range],
        classOf[WrappedString],
        classOf[TreeSet[_]],
        classOf[TreeMap[_, _]],
        // The most common orderings for TreeSet and TreeMap
        Ordering.Byte.getClass,
        Ordering.Short.getClass,
        Ordering.Int.getClass,
        Ordering.Long.getClass,
        Ordering.Float.getClass,
        Ordering.Double.getClass,
        Ordering.Boolean.getClass,
        Ordering.Char.getClass,
        Ordering.String.getClass
      )
    ).forConcreteTraversableClass(Set[Any]())
      .forConcreteTraversableClass(ListSet[Any]())
      .forConcreteTraversableClass(ListSet[Any]('a))

    k.register(classOf[BackwardCompatibleClassResolver.FallbackToHashSet1])
    k.register(classOf[BackwardCompatibleClassResolver.FallbackToHashSet2])

    k.forConcreteTraversableClass(Map[Any, Any]())

    k.register(classOf[BackwardCompatibleClassResolver.FallbackToHashMap1])
    k.register(classOf[BackwardCompatibleClassResolver.FallbackToHashMap2])

    k.forConcreteTraversableClass(ListMap[Any, Any]())
      .forConcreteTraversableClass(ListMap('a -> 'a))
    k.register(classOf[Stream.Cons[_]], new StreamSerializer[Any])
    k.register(Stream.empty[Any].getClass)
    k.forClass[scala.runtime.VolatileByteRef](new VolatileByteRefSerializer)
    k.forClass[BigDecimal](new BigDecimalSerializer)
    k.register(Queue.empty[Any].getClass)
    k.register(classOf[BackwardCompatibleClassResolver.FallbackToMap11])
    k.register(classOf[BackwardCompatibleClassResolver.FallbackToMap12])
    k.forConcreteTraversableClass(Map(1 -> 2).keySet)
  }
}
