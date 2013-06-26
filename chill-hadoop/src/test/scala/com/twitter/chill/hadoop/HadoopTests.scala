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

package com.twitter.chill.hadoop

import org.specs._

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import org.objenesis.strategy.StdInstantiatorStrategy

import java.io.{ByteArrayOutputStream => BAOut, ByteArrayInputStream => BAIn}

class HadoopTests extends Specification {
  noDetailedDiffs() //Fixes issue for scala 2.9

  def rt[A <: AnyRef](k: KryoSerialization, a: A): A = {
    val out = new BAOut
    val cls = a.getClass.asInstanceOf[Class[AnyRef]]
    val ks = k.getSerializer(cls)
    ks.open(out)
    ks.serialize(a)
    ks.close

    val in = new BAIn(out.toByteArray)
    val kd = k.getDeserializer(cls)
    kd.open(in)
    val res = kd.deserialize(null)
    kd.close
    res.asInstanceOf[A]
  }

  "KryoSerialization" should {
    "accept anything" in {
      val ks = new KryoSerialization
      Seq(classOf[List[_]], classOf[Int], this.getClass).forall { cls =>
        ks.accept(cls)
      } must beTrue
    }
    "loan out Kryo instances" in {
      val ks = new KryoSerialization
      ks.borrowKryo must notBeNull
    }
    "accept returned Kryo instances with 100 total" in {
      val ks = new KryoSerialization
      (0 to 1000).map { i =>
        val k = ks.borrowKryo
        // don't do this in real code:
        // since we release right away, only one is created
        ks.releaseKryo(k)
        k
      }.toSet.size must be_==(1)
    }
    "Serialize a list of random things" in {
      val ks = new KryoSerialization
      val things = List(1.asInstanceOf[AnyRef], "hey", (1, 2))

      things.map { rt(ks, _) } must be_==(things)
    }
  }
}
