package com.twitter.chill

import _root_.java.io.Serializable

import com.twitter.bijection.Injection

/**
 * @author
 *   Oscar Boykin
 * @author
 *   Sam Ritchie
 *
 * This KryoSerializer extension delegates the actual serialization to an instance of
 * Injection[T,Array[Byte]].
 */
object InjectiveSerializer {
  // Importing this implicit into scope will allow bijections to be
  // registered as Kryo Serializers, given an instance of Kryo.
  def asKryo[T](implicit injection: Injection[T, Array[Byte]]) =
    new InjectiveSerializer(injection)
}

class InjectiveSerializer[T] private (injection: Injection[T, Array[Byte]])
    extends KSerializer[T]
    with Serializable {
  def write(kser: Kryo, out: Output, obj: T): Unit = {
    val bytes = injection(obj)
    out.writeInt(bytes.length, true)
    out.writeBytes(bytes)
  }

  def read(kser: Kryo, in: Input, cls: Class[_ <: T]): T = {
    val bytes = new Array[Byte](in.readInt(true))
    in.readBytes(bytes)
    injection.invert(bytes).get
  }
}
