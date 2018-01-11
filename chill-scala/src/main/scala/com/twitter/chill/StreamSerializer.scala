package com.twitter.chill

class StreamSerializer[T]() extends KSerializer[Stream[T]] {

  def write(kser: Kryo, out: Output, stream: Stream[T]) {
    out.writeInt(stream.length, true)
    stream.foreach { t => kser.writeClassAndObject(out, t) }
    out.flush()
  }

  def read(kser: Kryo, in: Input, cls: Class[Stream[T]]): Stream[T] = {
    val size = in.readInt(true)
    Stream.fill(size)(kser.readClassAndObject(in).asInstanceOf[T])
  }
}
