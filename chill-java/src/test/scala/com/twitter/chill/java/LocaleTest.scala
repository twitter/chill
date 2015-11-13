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

package com.twitter.chill.java

import org.scalatest._

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output

import com.esotericsoftware.shaded.org.objenesis.strategy.StdInstantiatorStrategy

import _root_.java.util.Locale

class LocaleSpec extends WordSpec with Matchers {

  def rt[A](k: Kryo, a: A): A = {
    val out = new Output(1000, -1)
    k.writeClassAndObject(out, a.asInstanceOf[AnyRef])
    val in = new Input(out.toBytes)
    k.readClassAndObject(in).asInstanceOf[A]
  }

  "A Locale Serializer" should {
    "serialize all the things" in {
      import scala.collection.JavaConverters._

      val kryo = new Kryo()
      kryo.setInstantiatorStrategy(new StdInstantiatorStrategy)
      LocaleSerializer.registrar()(kryo)

      Locale.getAvailableLocales.foreach { l =>
        rt(kryo, l) should equal(l)
      }
    }
  }
}
