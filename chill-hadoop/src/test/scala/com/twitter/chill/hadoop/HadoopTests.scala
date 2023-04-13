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

import com.esotericsoftware.kryo.kryo5.Kryo
import com.esotericsoftware.kryo.kryo5.objenesis.strategy.StdInstantiatorStrategy;

import java.io.{ByteArrayInputStream => BAIn, ByteArrayOutputStream => BAOut}
import org.apache.hadoop.conf.Configuration

import com.twitter.chill.config.ConfiguredInstantiator
import com.twitter.chill.KryoInstantiator
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AnyKryoInstantiator extends KryoInstantiator {
  override def newKryo: Kryo = {
    val k = new Kryo
    k.register(Class.forName("com.twitter.chill.hadoop.HadoopTests"))
    k.register(Class.forName("scala.collection.immutable.List"))
    k
  }
}
class StdKryoInstantiator extends KryoInstantiator {
  override def newKryo: Kryo = {
    val k = new Kryo
    k.register(Class.forName("scala.Tuple2$mcII$sp"))
    k.setInstantiatorStrategy(new StdInstantiatorStrategy)
    k
  }
}
class HadoopTests extends AnyWordSpec with Matchers {
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
      ConfiguredInstantiator.setReflect(hc, classOf[AnyKryoInstantiator])

      val ks = new KryoSerialization(conf)
      Seq(classOf[List[_]], classOf[Int], this.getClass).forall { cls =>
        ks.accept(cls)
      } should equal(true)
    }
    "Serialize a list of random things" in {
      val conf = new Configuration
      val hc = new HadoopConfig(conf)
      // Scala needs this instantiator
      ConfiguredInstantiator.setReflect(hc, classOf[StdKryoInstantiator])

      val ks = new KryoSerialization(conf)
      val things = List(1.asInstanceOf[AnyRef], "hey", (1, 2))

      things.map(rt(ks, _)) should equal(things)
    }
  }
}
