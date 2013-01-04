package com.twitter.chill

import com.esotericsoftware.kryo.{ Kryo, Serializer => KSerializer }
import com.esotericsoftware.kryo.io.{ Input, Output }
import com.twitter.bijection.Bijection

/**
 *  @author Oscar Boykin
 *  @author Sam Ritchie
 *
 * This KryoSerializer extension delegates the actual serialization to
 * an instance of Bijection[T,Array[Byte]].
 */

object BijectiveSerializer {
  // Importing this implicit into scope will allow bijections to be
  // registered as Kryo Serializers, given an instance of Kryo.
  def asKryo[T](implicit bijection: Bijection[T,Array[Byte]]) =
    new BijectiveSerializer(bijection)
}

class BijectiveSerializer[T] private (bijection: Bijection[T, Array[Byte]]) extends KSerializer[T] {
  def write(kser: Kryo, out: Output, obj: T) {
    val bytes = bijection(obj)
    out.writeInt(bytes.length, true)
    out.writeBytes(bytes);
  }

  def read(kser: Kryo, in: Input, cls: Class[T]): T = {
    val bytes = new Array[Byte](in.readInt(true))
    in.readBytes(bytes)
    bijection.invert(bytes)
  }
}
