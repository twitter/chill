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

import org.scalatest._
import com.twitter.chill.{KSerializer, KryoPool, ScalaKryoInstantiator}
import avro.FiscalRecord
import org.apache.avro.generic.GenericRecordBuilder
import org.apache.avro.{Schema, SchemaBuilder}
import org.apache.avro.generic.GenericData.Record

import scala.reflect.ClassTag
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/**
 * @author Mansur Ashraf
 * @since 2/9/14.
 */
class AvroSerializerSpec extends AnyWordSpec with Matchers {
  def getKryo[T: ClassTag](k: KSerializer[T]): KryoPool = {
    val inst = { () => (new ScalaKryoInstantiator).newKryo.forClass(k) }
    KryoPool.withByteArrayOutputStream(1, inst)
  }

  val schema: Schema = SchemaBuilder
    .record("person")
    .fields
    .name("name")
    .`type`()
    .stringType()
    .noDefault()
    .name("ID")
    .`type`()
    .intType()
    .noDefault()
    .endRecord

  // Build an object conforming to the schema
  val user: Record = new GenericRecordBuilder(schema)
    .set("name", "Jeff")
    .set("ID", 1)
    .build

  val testRecord: FiscalRecord =
    FiscalRecord.newBuilder().setCalendarDate("2012-01-01").setFiscalWeek(1).setFiscalYear(2012).build()

  "SpecificRecordSerializer" should {
    "Serialize and Deserialize Avro Record" in {
      val kryo = getKryo(AvroSerializer.SpecificRecordSerializer[FiscalRecord])
      val bytes = kryo.toBytesWithClass(testRecord)
      val result = kryo.fromBytes(bytes).asInstanceOf[FiscalRecord]
      testRecord should equal(result)
    }
  }

  "SpecificRecordBinarySerializer" should {
    "Serialize and Deserialize Avro Record" in {
      val kryo = getKryo(AvroSerializer.SpecificRecordBinarySerializer[FiscalRecord])
      val bytes = kryo.toBytesWithClass(testRecord)
      val result = kryo.fromBytes(bytes).asInstanceOf[FiscalRecord]
      testRecord should equal(result)
    }
  }

  "SpecificRecordJsonSerializer" should {
    "Serialize and Deserialize Avro Record" in {
      val kryo = getKryo(AvroSerializer.SpecificRecordJsonSerializer[FiscalRecord](FiscalRecord.SCHEMA$))
      val bytes = kryo.toBytesWithClass(testRecord)
      val result = kryo.fromBytes(bytes).asInstanceOf[FiscalRecord]
      testRecord should equal(result)
    }
  }

  "GenericRecordSerializer" should {
    "Serialize and Deserialize Avro Record" in {
      val kryo = getKryo(AvroSerializer.GenericRecordSerializer[Record]())
      val userBytes = kryo.toBytesWithClass(user)
      val userResult = kryo.fromBytes(userBytes).asInstanceOf[Record]
      userResult.get("name").toString should equal("Jeff")
      userResult.get("ID") should equal(1)
      user.toString should equal(userResult.toString)

      val testRecordBytes = kryo.toBytesWithClass(testRecord)
      val testRecordResult = kryo.fromBytes(testRecordBytes).asInstanceOf[FiscalRecord]
      testRecord.toString should equal(testRecordResult.toString)
    }
  }
}
