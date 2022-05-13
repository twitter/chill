/*
 * Copyright 2013 Twitter Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twitter.chill.protobuf

import com.twitter.chill.{KryoInstantiator, KryoPool}
import com.twitter.chill.protobuf.TestMessages.FatigueCount

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.serializers.FieldSerializer

import com.google.protobuf.Message

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ProtobufTest extends AnyWordSpec with Matchers {
  def buildKyroPoolWithProtoSer(): KryoPool =
    KryoPool.withByteArrayOutputStream(
      1,
      new KryoInstantiator {
        override def newKryo(): Kryo = {
          val k = new Kryo
          k.addDefaultSerializer(classOf[Message], classOf[ProtobufSerializer])
          k
        }
      }
    )

  def buildFatigueCount(target: Long, id: Long, count: Int, recentClicks: List[Long]): FatigueCount = {
    val bldr = FatigueCount
      .newBuilder()
      .setTargetId(target)
      .setSuggestedId(id)
      .setServeCount(count)

    recentClicks.foreach(bldr.addRecentClicks(_))
    bldr.build
  }

  "Protobuf round-trips" in {
    val kpool = buildKyroPoolWithProtoSer()

    kpool.deepCopy(buildFatigueCount(12L, -1L, 42, List(1L, 2L))) should equal(
      buildFatigueCount(12L, -1L, 42, List(1L, 2L))
    )

    // Without the protobuf serializer, it will use FieldSerializer
    // With newer versions of protoc code gen this might be ok but not advisable!
    val kryoInstantiator = new KryoInstantiator
    val ser = kryoInstantiator.newKryo().getSerializer(classOf[Message])
    ser shouldBe a[FieldSerializer[_]]

    val kpoolBusted = KryoPool.withByteArrayOutputStream(1, kryoInstantiator)
    kpoolBusted.deepCopy(buildFatigueCount(12L, -1L, 42, List(1L, 2L))) should equal(
      buildFatigueCount(12L, -1L, 42, List(1L, 2L))
    )
  }

  "Default Instance of Should be Ser-DeSer correctly" in {
    val kpool = buildKyroPoolWithProtoSer()

    kpool.deepCopy(FatigueCount.getDefaultInstance) should equal(FatigueCount.getDefaultInstance)
  }
}
