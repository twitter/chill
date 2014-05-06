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

import org.specs.Specification
import com.twitter.chill.{KSerializer, ScalaKryoInstantiator, KryoPool}
import avro.FiscalRecord
import org.apache.avro.generic.GenericRecordBuilder
import org.apache.avro.SchemaBuilder
import org.apache.avro.generic.GenericData.Record

/**
 * @author Mansur Ashraf
 * @since 2/9/14.
 */
object AvroSerializerSpec extends Specification {

  def getKyro[T: Manifest](k: KSerializer[T]) = {
    val inst = {
      () => (new ScalaKryoInstantiator).newKryo.forClass(k)
    }
    KryoPool.withByteArrayOutputStream(1, inst)
  }

  val schema = SchemaBuilder
    .record("person")
    .fields
    .name("name").`type`().stringType().noDefault()
    .name("ID").`type`().intType().noDefault()
    .endRecord

  // Build an object conforming to the schema
  val user = new GenericRecordBuilder(schema)
    .set("name", "Jeff")
    .set("ID", 1)
    .build

  val testRrecord = FiscalRecord.newBuilder().setCalendarDate("2012-01-01").setFiscalWeek(1).setFiscalYear(2012).build()

  "SpecificRecordSerializer" should {
    "Serialize and Deserialize Avro Record" in {
      val kryo = getKyro(AvroSerializer.SpecificRecordSerializer[FiscalRecord])
      val bytes = kryo.toBytesWithClass(testRrecord)
      val result = kryo.fromBytes(bytes).asInstanceOf[FiscalRecord]
      testRrecord must_== result
    }
  }

  "SpecificRecordBinarySerializer" should {
    "Serialize and Deserialize Avro Record" in {
      val kryo = getKyro(AvroSerializer.SpecificRecordBinarySerializer[FiscalRecord])
      val bytes = kryo.toBytesWithClass(testRrecord)
      val result = kryo.fromBytes(bytes).asInstanceOf[FiscalRecord]
      testRrecord must_== result
    }
  }

  "SpecificRecordJsonSerializer" should {
    "Serialize and Deserialize Avro Record" in {
      val kryo = getKyro(AvroSerializer.SpecificRecordJsonSerializer[FiscalRecord](FiscalRecord.SCHEMA$))
      val bytes = kryo.toBytesWithClass(testRrecord)
      val result = kryo.fromBytes(bytes).asInstanceOf[FiscalRecord]
      testRrecord must_== result
    }
  }

  "GenericRecordSerializer" should {
    "Serialize and Deserialize Avro Record" in {
      val kryo = getKyro(AvroSerializer.GenericRecordSerializer[Record]())
      val userBytes = kryo.toBytesWithClass(user)
      val userResult = kryo.fromBytes(userBytes).asInstanceOf[Record]
      userResult.get("name").toString must_== "Jeff"
      userResult.get("ID") must_== 1
      user.toString must_== userResult.toString

      val testRecordBytes = kryo.toBytesWithClass(testRrecord)
      val testRecordResult = kryo.fromBytes(testRecordBytes).asInstanceOf[FiscalRecord]
      testRrecord.toString must_== testRecordResult.toString
    }
  }
}
