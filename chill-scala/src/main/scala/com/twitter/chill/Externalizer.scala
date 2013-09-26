/*
Copyright 2013 Twitter, Inc.

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

import _root_.java.io.{
  ByteArrayOutputStream,
  ByteArrayInputStream,
  Externalizable,
  ObjectInput,
  ObjectOutput,
  ObjectInputStream,
  ObjectOutputStream
}

import com.esotericsoftware.kryo.serializers.JavaSerializer
import com.esotericsoftware.kryo.DefaultSerializer

object Externalizer {
  def apply[T](t: T): Externalizer[T] = {
    val x = new Externalizer[T]
    x.set(t)
    x
  }
}

/** This is a more fault-tolerant MeatLocker
 * that tries first to do Kryo serialization,
 * and then falls back to Java serialization if that does not
 * work. Of course, Java serialization may fail if the contained
 * item is not Java serializable
 */
@DefaultSerializer(classOf[JavaSerializer])
class Externalizer[T] extends Externalizable {
  private var item: Option[T] = None

  def getOption: Option[T] = item
  def get: T = item.get // This should never be None when get is called

  /** Unfortunately, Java serialization requires mutable objects if
   * you are going to control how the serialization is done.
   * Use the companion object to creat new instances of this
   */
  def set(it: T): Unit = {
    assert(item.isEmpty, "Tried to call .set on an already constructed Externalizer")
    item = Some(it)
  }

  /* Tokens used to distinguish if we used Kryo or Java */
  private val KRYO = 0
  private val JAVA = 1

  /** Override this to configure Kryo creation with a named subclass,
   * e.g.
   * class MyExtern[T] extends Externalizer[T] {
   *   override def kryo = myInstantiator
   * }
   * note that if this is not a named class on the classpath, we have to serialize
   * the KryoInstantiator at the same time, which would increase size.
   */
  protected def kryo: KryoInstantiator =
    (new ScalaKryoInstantiator).setReferences(true)

  // 1 here is 1 thread, since we will likely only serialize once
  // this should not be a val because we don't want to capture a reference
  private def kpool = KryoPool.withByteArrayOutputStream(1, kryo)

  /** Try to round-trip and see if it works without error
   */
  lazy val javaWorks: Boolean = {
    try {
      val baos = new ByteArrayOutputStream()
      val oos = new ObjectOutputStream(baos)
      oos.writeObject(item)
      val bytes = baos.toByteArray
      val testInput = new ByteArrayInputStream(bytes)
      val ois = new ObjectInputStream(testInput)
      ois.readObject // this may throw
      true
    }
    catch {
      case t: Throwable =>
        Option(System.getenv.get("CHILL_EXTERNALIZER_DEBUG"))
          .filter(_.toBoolean)
          .foreach { _ => t.printStackTrace }
        false
    }
  }

  private def safeToBytes: Option[Array[Byte]] = {
    try {
      val bytes = kpool.toBytesWithClass(item)
      // Make sure we can read without throwing
      fromBytes(bytes)
      Some(bytes)
    }
    catch {
      case t: Throwable =>
        Option(System.getenv.get("CHILL_EXTERNALIZER_DEBUG"))
          .filter(_.toBoolean)
          .foreach { _ => t.printStackTrace }
        None
    }
  }
  private def fromBytes(b: Array[Byte]): Option[T] =
    kpool.fromBytes(b).asInstanceOf[Option[T]]

  def readExternal(in: ObjectInput) {
    in.read match {
      case JAVA =>
        item = in.readObject.asInstanceOf[Option[T]]
      case KRYO =>
        val sz = in.readInt
        val buf = new Array[Byte](sz)
        in.readFully(buf)
        item = fromBytes(buf)
    }
  }

  protected def writeJava(out: ObjectOutput): Boolean =
    javaWorks && {
      out.write(JAVA)
      out.writeObject(item)
      true
    }

  protected def writeKryo(out: ObjectOutput): Boolean =
    safeToBytes.map { bytes =>
      out.write(KRYO)
      out.writeInt(bytes.size)
      out.write(bytes)
      true
    }.getOrElse(false)

  def writeExternal(out: ObjectOutput) {
    writeJava(out) || writeKryo(out) || {
      val inner = item.get
      sys.error("Neither Java nor Kyro works for class: %s instance: %s\nexport CHILL_EXTERNALIZER_DEBUG=true to see both stack traces"
        .format(inner.getClass, inner))
    }
  }
}
