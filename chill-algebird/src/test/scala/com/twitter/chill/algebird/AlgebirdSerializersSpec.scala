/*
Copyright 2014 Twitter, Inc.

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

package com.twitter.chill.algebird

import com.twitter.chill.{ ScalaKryoInstantiator, KryoPool }
import com.twitter.algebird.Operators._
import com.twitter.algebird._

import org.scalatest._


class AlgebirdSerializersSpec extends WordSpec with Matchers {
  implicit def string2Bytes(i: String) = i.toCharArray.map(_.toByte)
  implicit val skmMonoid = SketchMap.monoid[String, Long](SketchMapParams[String](1, 0.01, 1e-3, 10))

  val kryo = {
    val inst = () => {
      val newK = (new ScalaKryoInstantiator).newKryo
      newK.setReferences(false) // typical in production environment (scalding, spark)
      newK.register(classOf[com.twitter.algebird.SketchMap[_, _]], new SketchMapSerializer)
      (new AlgebirdRegistrar).apply(newK)
      newK
    }
    KryoPool.withByteArrayOutputStream(1, inst)
  }

  def roundtrip[X](x: X) {
    val bytes = kryo.toBytesWithClass(x)
    //println("bytes size : " + bytes.size)
    //println("bytes: " + new String(bytes, "UTF-8"))
    val result = kryo.fromBytes(bytes).asInstanceOf[X]
    result should equal(x)

  }

  def roundtripNoEq[X](x: X)(f: X => Any) {
    val bytes = kryo.toBytesWithClass(x)
    val result = kryo.fromBytes(bytes).asInstanceOf[X]
    f(result) should equal(f(x))
  }

  "kryo with AlgebirdRegistrar" should {
    "serialize and deserialize AveragedValue" in {
      roundtrip(AveragedValue(10L, 123.45))
    }

    "serialize and deserialize DecayedValue" in {
      roundtrip(DecayedValue.build(3.14, 20.2, 9.33))
    }

    "serialize and deserialize HyperLogLogMonoid" in {
      roundtripNoEq(new HyperLogLogMonoid(12))(_.bits)
    }

    "serialize and deserialize Moments" in {
      roundtrip(MomentsGroup.zero)
    }

    "serialize and deserialize HLL" in {
      val sparse = new HyperLogLogMonoid(4).create(Array(-127.toByte))
      val dense = new HyperLogLogMonoid(4).batchCreate(Seq(-127, 100, 23, 44, 15, 96, 10).map(x => Array(x.toByte)))
      roundtrip(sparse)
      roundtrip(dense)
    }

    "serialize and deserialize SparseVector and DenseVector" in {
      val sparse = AdaptiveVector.fromVector(Vector(1, 1, 1, 1, 1, 3), 1)
      val dense = AdaptiveVector.fromVector(Vector(1, 2, 3, 1, 2, 3), 1)
      roundtrip(sparse)
      roundtrip(dense)
    }

    "serialize and deserialize SketchMap" in {
      val skm = skmMonoid.create(Seq(("test1", 1L), ("test2", 2L)))
      roundtrip[SketchMap[String, Long]](skm)

      roundtripNoEq(skm)(_.heavyHitterKeys)
      roundtripNoEq(skm)(_.totalValue)
      roundtripNoEq(skm) { s => skmMonoid.frequency(s, "test1") }
      roundtripNoEq(skm) { s => skmMonoid.frequency(s, "test2") }

      // do it here explicit se/deserialization to check summation
      val bin = kryo.toBytesWithoutClass(skm)
      val skmAfter = kryo.fromBytes(bin, classOf[SketchMap[String, Long]])

      //should be able to sum afterwards
      a[scala.Throwable] shouldNot be (thrownBy (skmAfter + skmAfter))
      a[scala.Throwable] shouldNot be (thrownBy (skm + skmAfter))

      // do the same for "zero" or empty SketchMap
      val zero = skmMonoid.zero
      val binZero = kryo.toBytesWithoutClass(zero)
      val zeroAfter = kryo.fromBytes(binZero, classOf[SketchMap[String, Long]])

      zeroAfter.heavyHitterKeys should be (empty)
      zeroAfter.totalValue should be (0)

      //should be able to sum afterwards
      a[scala.Throwable] shouldNot be (thrownBy (zeroAfter + zeroAfter))
      a[scala.Throwable] shouldNot be (thrownBy (zero + zeroAfter))
    }

  }
}
