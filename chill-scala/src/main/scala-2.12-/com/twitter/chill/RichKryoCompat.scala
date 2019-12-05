package com.twitter.chill

import scala.collection.generic.CanBuildFrom

import scala.reflect._

trait RichKryoCompat { self: RichKryo =>

  def forTraversableSubclass[T, C <: Traversable[T]](
      c: C with Traversable[T],
      isImmutable: Boolean = true
  )(implicit mf: ClassTag[C], cbf: CanBuildFrom[C, T, C]): Kryo = {
    k.addDefaultSerializer(mf.runtimeClass, new TraversableSerializer(isImmutable)(cbf))
    k
  }

  def forTraversableClass[T, C <: Traversable[T]](
      c: C with Traversable[T],
      isImmutable: Boolean = true
  )(implicit mf: ClassTag[C], cbf: CanBuildFrom[C, T, C]): Kryo =
    forClass(new TraversableSerializer(isImmutable)(cbf))

  def forConcreteTraversableClass[T, C <: Traversable[T]](
      c: C with Traversable[T],
      isImmutable: Boolean = true
  )(implicit cbf: CanBuildFrom[C, T, C]): Kryo = {
    // a ClassTag is not used here since its runtimeClass method does not return the concrete internal type
    // that Scala uses for small immutable maps (i.e., scala.collection.immutable.Map$Map1)
    k.register(c.getClass, new TraversableSerializer(isImmutable)(cbf))
    k
  }
}
