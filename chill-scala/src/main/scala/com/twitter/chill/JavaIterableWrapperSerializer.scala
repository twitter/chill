package com.twitter.chill

import _root_.java.lang.{Iterable => JIterable}
import scala.collection.JavaConverters.asJavaIterableConverter

/**
 * A Kryo serializer for serializing results returned by asJavaIterable.
 *
 * The underlying object is scala.collection.convert.Wrappers$IterableWrapper. Kryo deserializes this into an
 * AbstractCollection, which unfortunately doesn't work.
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

  override def read(kryo: Kryo, in: Input, clz: Class[_ <: JIterable[_]]): JIterable[_] =
    kryo.readClassAndObject(in) match {
      case scalaIterable: Iterable[_] =>
        asJavaIterableConverter(scalaIterable).asJava
      case javaIterable: JIterable[_] =>
        javaIterable
    }
}

private object JavaIterableWrapperSerializer {
  // The class returned by asJavaIterable (scala.collection.convert.Wrappers$IterableWrapper).
  val wrapperClass: Class[_ <: JIterable[Int]] =
    asJavaIterableConverter(Seq.empty[Int]).asJava.getClass

  // Get the underlying method so we can use it to get the Scala collection for serialization.
  private val underlyingMethodOpt =
    try Some(wrapperClass.getDeclaredMethod("underlying"))
    catch {
      case e: Exception =>
        None
    }
}
