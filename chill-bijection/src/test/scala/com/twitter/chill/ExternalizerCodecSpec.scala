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

import com.twitter.bijection.Injection
import scala.util.Try
import org.scalatest.wordspec.AnyWordSpec

class NotSerializable {
  val x = "abcd"
  override def equals(other: Any): Boolean =
    other match {
      case i: NotSerializable => true
      case _                  => false
    }
}
class ExternalizerCodecSpec extends AnyWordSpec {
  import ExternalizerCodec._
  import ExternalizerInjection._

  def rt[T: ExternalizerInjection: ExternalizerCodec](t: T): Try[T] = {
    val connectedInj = Injection.connect[T, Externalizer[T], Array[Byte]]
    connectedInj.invert(connectedInj(t))
  }

  "The codec on Externalizer" should {
    "round trip objects" in {
      val x = rt(() => Foo.Bar).get
      assert(x() == Foo.Bar)
    }
    "round not serializable classes" in {
      val baseSerializable = new NotSerializable
      val x = rt(baseSerializable).get
      assert(x == baseSerializable, "Should be value equal")
      assert(x ne baseSerializable, "Should not be reference equal, since through a serializer")
    }
  }
}
