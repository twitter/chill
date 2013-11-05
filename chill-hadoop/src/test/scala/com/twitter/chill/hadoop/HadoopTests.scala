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
import org.apache.hadoop.conf.Configuration;

import com.twitter.chill.config.ConfiguredInstantiator;
import com.twitter.chill.KryoInstantiator;

class StdKryoInstantiator extends KryoInstantiator {
  override def newKryo = {
    val k = new Kryo
    k.setInstantiatorStrategy(new StdInstantiatorStrategy)
    k
  }
}
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
      val conf = new Configuration
      val hc = new HadoopConfig(conf)
      ConfiguredInstantiator.setReflect(hc, classOf[KryoInstantiator])

      val ks = new KryoSerialization(conf)
      Seq(classOf[List[_]], classOf[Int], this.getClass).forall { cls =>
        ks.accept(cls)
      } must beTrue
    }
    "Serialize a list of random things" in {
      val conf = new Configuration
      val hc = new HadoopConfig(conf)
      // Scala needs this instantiator
      ConfiguredInstantiator.setReflect(hc, classOf[StdKryoInstantiator])

      val ks = new KryoSerialization(conf)
      val things = List(1.asInstanceOf[AnyRef], "hey", (1, 2))

      things.map { rt(ks, _) } must be_==(things)
    }
  }
}
