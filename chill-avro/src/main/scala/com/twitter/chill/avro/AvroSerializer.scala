/*

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
package com.twitter.chill.avro

import com.twitter.bijection.Injection
import com.twitter.bijection.avro.{GenericAvroCodecs, SpecificAvroCodecs}
import com.twitter.chill.{InjectiveSerializer, KSerializer}
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.avro.specific.SpecificRecordBase

import scala.reflect.ClassTag

/**
 * @author Mansur Ashraf
 * @since 2/9/14.
 */
object AvroSerializer {

  def SpecificRecordSerializer[T <: SpecificRecordBase: ClassTag]: KSerializer[T] = {
    implicit val inj = SpecificAvroCodecs[T]
    InjectiveSerializer.asKryo
  }

  def SpecificRecordBinarySerializer[T <: SpecificRecordBase: ClassTag]: KSerializer[T] = {
    implicit val inj = SpecificAvroCodecs.toBinary[T]
    InjectiveSerializer.asKryo
  }

  def SpecificRecordJsonSerializer[T <: SpecificRecordBase: ClassTag](schema: Schema): KSerializer[T] = {
    import com.twitter.bijection.StringCodec.utf8
    implicit val inj = SpecificAvroCodecs.toJson[T](schema)
    implicit val avroToArray = Injection.connect[T, String, Array[Byte]]
    InjectiveSerializer.asKryo
  }

  def GenericRecordSerializer[T <: GenericRecord: ClassTag](schema: Schema = null): KSerializer[T] = {
    implicit val inj = GenericAvroCodecs[T](schema)
    InjectiveSerializer.asKryo
  }
}
