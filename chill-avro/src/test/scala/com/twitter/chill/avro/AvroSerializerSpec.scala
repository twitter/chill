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
import org.apache.avro.specific.SpecificRecordBase
import avro.FiscalRecord

/**
 * @author Mansur Ashraf
 * @since 2/9/14.
 */
object AvroSerializerSpec extends Specification {

  def getKryo[T <: SpecificRecordBase : Manifest](k: KSerializer[T]) = {
    val inst = {
      () => (new ScalaKryoInstantiator).newKryo.forClass(k)
    }
    KryoPool.withByteArrayOutputStream(1, inst)
  }

  val testRrecord = FiscalRecord.newBuilder().setCalendarDate("2012-01-01").setFiscalWeek(1).setFiscalYear(2012).build()

  "SpecificRecordSerializer" should {
    "Serialize and Deserialize Avro Record" in {
      val kryo = getKryo(AvroSerializer.SpecificRecordSerializer[FiscalRecord])
      val bytes = kryo.toBytesWithClass(testRrecord)
      val result = kryo.fromBytes(bytes).asInstanceOf[FiscalRecord]
      testRrecord must_== result
    }
  }

  "SpecificRecordBinarySerializer" should {
    "Serialize and Deserialize Avro Record" in {
      val kryo = getKryo(AvroSerializer.SpecificRecordBinarySerializer[FiscalRecord])
      val bytes = kryo.toBytesWithClass(testRrecord)
      val result = kryo.fromBytes(bytes).asInstanceOf[FiscalRecord]
      testRrecord must_== result
    }
  }

  "SpecificRecordJsonSerializer" should {
    "Serialize and Deserialize Avro Record" in {
      val kryo = getKryo(AvroSerializer.SpecificRecordJsonSerializer[FiscalRecord](FiscalRecord.SCHEMA$))
      val bytes = kryo.toBytesWithClass(testRrecord)
      val result = kryo.fromBytes(bytes).asInstanceOf[FiscalRecord]
      testRrecord must_== result
    }
  }
}
