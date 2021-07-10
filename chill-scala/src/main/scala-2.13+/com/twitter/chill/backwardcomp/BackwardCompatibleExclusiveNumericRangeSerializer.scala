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

import com.esotericsoftware.kryo.serializers.FieldSerializer
import com.twitter.chill.{Input, Kryo, Output}

class BackwardCompatibleExclusiveNumericRangeSerializer[T](kryo: Kryo, typ: Class[_])
    extends FieldSerializer[T](kryo, typ) {

  override def read(kryo: Kryo, input: Input, typ: Class[T]): T = {
    val result = create(kryo, input, typ)
    kryo.reference(result)

    val bitmap0 = input.readByte()
    val end = kryo.readClassAndObject(input)
    val hashCode = input.readVarInt(false)
    val isInclusive = input.readBoolean()
    val last = kryo.readClassAndObject(input)
    val num1 = kryo.readClassAndObject(input)
    val num2 = kryo.readClassAndObject(input)
    val numRangeElements = input.readVarInt(false)
    val start = kryo.readClassAndObject(input)
    val step = kryo.readClassAndObject(input)

    var idx = 0
    val fields = getFields
    while (idx < fields.length) {
      val field = fields(idx)
      idx = idx + 1

      field.getField.getName match {
        case "bitmap$0"                                     => field.getField.set(result, bitmap0)
        case "end"                                          => field.getField.set(result, end)
        case "hashCode"                                     => field.getField.set(result, hashCode)
        case "isInclusive"                                  => field.getField.set(result, isInclusive)
        case "length"                                       => field.getField.set(result, numRangeElements)
        case "num"                                          => field.getField.set(result, num1)
        case "scala$collection$immutable$NumericRange$$num" => field.getField.set(result, num2)
        case "start"                                        => field.getField.set(result, start)
        case "step"                                         => field.getField.set(result, step)
      }
    }

    result
  }

  override def write(kryo: Kryo, output: Output, `object`: T): Unit = {
    var length: Option[Int] = None
    var idx = 0
    val fields = getFields
    while (idx < fields.length) {
      val field = fields(idx)
      idx = idx + 1

      val value = field.getField.get(`object`)
      field.getField.getName match {
        case "bitmap$0"    => output.writeByte(value.asInstanceOf[Byte])
        case "end"         => kryo.writeClassAndObject(output, value)
        case "hashCode"    => output.writeVarInt(value.asInstanceOf[Int], false)
        case "isInclusive" => output.writeBoolean(value.asInstanceOf[Boolean])
        case "length" =>
          kryo.writeClassAndObject(output, null) // last
          length = Some(value.asInstanceOf[Int]) // storing length to be emitted after the implicits
        case "num" => kryo.writeClassAndObject(output, value)
        case "scala$collection$immutable$NumericRange$$num" =>
          kryo.writeClassAndObject(output, value)
          output.writeVarInt(length.get, false)
        case "start" => kryo.writeClassAndObject(output, value)
        case "step"  => kryo.writeClassAndObject(output, value)
      }
    }
  }
}
