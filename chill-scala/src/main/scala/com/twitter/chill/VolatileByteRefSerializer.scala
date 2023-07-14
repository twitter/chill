package com.twitter.chill

import scala.runtime.VolatileByteRef

class VolatileByteRefSerializer extends KSerializer[VolatileByteRef] {
  def write(kser: Kryo, out: Output, item: VolatileByteRef): Unit =
    out.writeByte(item.elem)

  def read(kser: Kryo, in: Input, cls: Class[_ <: VolatileByteRef]) =
    new VolatileByteRef(in.readByte)
}
