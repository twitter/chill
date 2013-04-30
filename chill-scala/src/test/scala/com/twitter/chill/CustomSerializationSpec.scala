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

/*
* This is just a test case for Kryo to deal with. It should
* be outside KryoSpec, otherwise the enclosing class, KryoSpec
* will also need to be serialized
*/
case class Point(x: Int, y: Int)
case class Color(name: String)
case class ColoredPoint(color: Color, point: Point)

class CustomSerializationSpec extends Specification with BaseProperties {
  "Custom KryoSerializers and KryoDeserializers" should {
    "serialize objects that have registered serialization" in {
      import KryoImplicits.toRich

      // write bijections
      implicit val pointBijection = Bijection.build[Point, (Int,Int)](
        Point.unapply(_).get)(
        (Point.apply _).tupled)
      implicit val colorBijection = Bijection.build[Color, String](
        Color.unapply(_).get)(
        Color.apply)
      implicit val coloredPointBijection = Bijection.build[ColoredPoint, (Color, Point)](
        ColoredPoint.unapply(_).get)(
        (ColoredPoint.apply _).tupled)

      val MyKryoInjection = new KryoBijection {
        override def getKryo = super.getKryo
          .forClassViaBijection[Point, (Int,Int)]
          .forClassViaBijection[Color, (String)]
          .forClassViaBijection[ColoredPoint, (Color, Point)]
      }

      val color = Color("blue")
      val point = Point(5, 6)
      val coloredPoint = ColoredPoint(color, point)

      rt(coloredPoint) must_== coloredPoint
    }
  }
}
