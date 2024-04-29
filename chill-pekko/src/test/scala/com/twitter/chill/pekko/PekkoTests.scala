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

package com.twitter.chill.pekko

import org.apache.pekko.actor.{Actor, ActorRef, ActorSystem, Props}
import org.apache.pekko.serialization._
import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PekkoTests extends AnyWordSpec with Matchers {
  object IncActor {
    def props: Props = Props(IncActor())
  }

  case class IncActor() extends Actor {
    def receive: Receive = { case x: Int =>
      sender() ! (x + 1)
    }
  }

  val system: ActorSystem = ActorSystem(
    "example",
    ConfigFactory.parseString("""
    pekko.actor.serializers {
      kryo = "com.twitter.chill.pekko.PekkoSerializer"
    }

    pekko.actor.serialization-bindings {
      "scala.Product" = kryo
      "org.apache.pekko.actor.ActorRef" = kryo
    }
""")
  )

  // Get the Serialization Extension
  val serialization: Serialization = SerializationExtension(system)

  "PekkoSerializer" should {
    "be selected for tuples" in {
      // Find the Serializer for it
      val serializer = serialization.findSerializerFor((1, 2, 3))
      serializer.getClass.equals(classOf[PekkoSerializer]) should equal(true)
    }

    def actorRef(i: Int) = system.actorOf(IncActor.props, "incActor" + i)

    "be selected for ActorRef" in {
      val serializer = serialization.findSerializerFor(actorRef(1))
      serializer.getClass.equals(classOf[PekkoSerializer]) should equal(true)
    }

    "serialize and deserialize ActorRef successfully" in {
      val actor = actorRef(2)
      val serialized = serialization.serialize(actor)
      serialized.isSuccess should equal(true)

      val deserialized = serialization.deserialize(serialized.get, classOf[ActorRef])
      deserialized.isSuccess should equal(true)

      deserialized.get.equals(actor) should equal(true)
    }
  }
}
