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

package com.twitter.chill

import org.specs._

import com.twitter.bijection.Bijection

import BijectionEnrichedKryo._

object Foo {
  def Bar = 1
}
object Globals {
  var temp = false
}

class CustomSerializationSpec extends Specification with BaseProperties {
  "Custom KryoSerializers and KryoDeserializers" should {
    "serialize objects that have registered serialization" in {

      /* These classes can be inside CustomSerializationSpec since their
       * serialization is precisely specified. */
      case class Point(x: Int, y: Int)
      case class Color(name: String)
      case class ColoredPoint(color: Color, point: Point) {
        override def toString = color + ":" + point
      }

      // write bijections
      implicit val pointBijection = Bijection.build[Point, (Int, Int)](
        Point.unapply(_).get)(
          (Point.apply _).tupled)
      implicit val colorBijection = Bijection.build[Color, String](
        Color.unapply(_).get)(
          Color.apply)
      implicit val coloredPointBijection = Bijection.build[ColoredPoint, (Color, Point)](
        ColoredPoint.unapply(_).get)(
          (ColoredPoint.apply _).tupled)

      val myInst = { () =>
        (new ScalaKryoInstantiator).newKryo
          // use the implicit bijection by specifying the type
          .forClassViaBijection[Point, (Int, Int)]
          // use an explicit bijection, avoiding specifying the type
          .forClassViaBijection(pointBijection)
          .forClassViaBijection(colorBijection)
          .forClassViaBijection(coloredPointBijection)
      }

      val color = Color("blue")
      val point = Point(5, 6)
      val coloredPoint = ColoredPoint(color, point)

      rt(myInst, coloredPoint) must_== coloredPoint
    }
    "use bijections" in {
      implicit val bij = Bijection.build[TestCaseClassForSerialization, (String, Int)] { s =>
        (s.x, s.y)
      } { tup => TestCaseClassForSerialization(tup._1, tup._2) }

      val inst = { () =>
        (new ScalaKryoInstantiator)
          .newKryo
          .forClassViaBijection[TestCaseClassForSerialization, (String, Int)]
      }
      rt(inst, TestCaseClassForSerialization("hey", 42)) must be_==(TestCaseClassForSerialization("hey", 42))
    }
    "Make sure KryoInjection and instances are Java Serializable" in {
      val ki = jrt(KryoInjection)
      ki.invert(ki(1)).get must be_==(1)
      val kii = jrt(KryoInjection.instance(new ScalaKryoInstantiator))
      kii.invert(kii(1)).get must be_==(1)
    }
  }
  "KryoInjection handle an example with closure to function" in {
    val x = rt(() => Foo.Bar)
    x() must be_==(Foo.Bar)
  }
  "handle a closure to println" in {
    Globals.temp = false
    val bytes = KryoInjection(() => {
      // println is in the CustomSpec, calling that creates a closure that gets
      // the whole spec, which is not serializable
      Predef.println();
      Globals.temp = true
    })
    val inv = KryoInjection.invert(bytes)
    inv.get.asInstanceOf[() => Unit].apply()
    Globals.temp must be_==(true)
  }
}
