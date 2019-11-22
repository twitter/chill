package com.twitter.chill

import _root_.java.lang.{Iterable => JIterable}

/**
 * A Kryo serializer for serializing results returned by asJavaIterable.
 *
 * The underlying object is scala.collection.convert.Wrappers$IterableWrapper.
 * Kryo deserializes this into an AbstractCollection, which unfortunately doesn't work.
 *
 * Ported from Apache Spark's KryoSerializer.scala.
 */
private class JavaIterableWrapperSerializer extends KSerializer[JIterable[_]] {
  import JavaIterableWrapperSerializer._

  override def write(kryo: Kryo, out: Output, obj: JIterable[_]): Unit =
    // If the object is the wrapper, simply serialize the underlying Scala Iterable object.
    // Otherwise, serialize the object itself.
    if (obj.getClass == wrapperClass && underlyingMethodOpt.isDefined) {
      kryo.writeClassAndObject(out, underlyingMethodOpt.get.invoke(obj))
    } else {
      kryo.writeClassAndObject(out, obj)
    }

  override def read(kryo: Kryo, in: Input, clz: Class[JIterable[_]]): JIterable[_] =
    kryo.readClassAndObject(in) match {
      case scalaIterable: Iterable[_] =>
        scala.collection.JavaConversions.asJavaIterable(scalaIterable)
      case javaIterable: JIterable[_] =>
        javaIterable
    }
}

private object JavaIterableWrapperSerializer {
  // The class returned by asJavaIterable (scala.collection.convert.Wrappers$IterableWrapper).
  val wrapperClass: Class[_ <: JIterable[Int]] =
    scala.collection.JavaConversions.asJavaIterable(Seq(1)).getClass

  // Get the underlying method so we can use it to get the Scala collection for serialization.
  private val underlyingMethodOpt = {
    try Some(wrapperClass.getDeclaredMethod("underlying"))
    catch {
      case e: Exception =>
        None
    }
  }
}
