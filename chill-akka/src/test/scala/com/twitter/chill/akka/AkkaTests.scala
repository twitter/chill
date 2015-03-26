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

package com.twitter.chill.akka

import akka.actor.{ Props, Actor, ActorRef, ActorSystem }
import akka.serialization._
import com.typesafe.config.ConfigFactory
import org.scalatest._

import scala.concurrent.Await

class AkkaTests extends WordSpec with Matchers {

  val system = ActorSystem("test-system", ConfigFactory.parseString("""
    akka.actor.serializers {
      kryo = "com.twitter.chill.akka.AkkaSerializer"
    }

    akka.actor.serialization-bindings {
      "scala.Product" = kryo
      "akka.actor.ActorRef" = kryo
    }
"""))

  // Get the Serialization Extension
  val serialization = SerializationExtension(system)

  "AkkaSerializer" should {
    "be selected for tuples" in {
      // Find the Serializer for it
      val serializer = serialization.findSerializerFor((1, 2, 3))
      serializer.getClass.equals(classOf[AkkaSerializer]) should equal(true)
    }

    "be selected for ActorRef" in {
      val actorRef = system.actorOf(Props.create(classOf[DummyActor], this), "test-actor")
      val serializer = serialization.findSerializerFor(actorRef)
      serializer.getClass.equals(classOf[AkkaSerializer]) should equal(true)
    }

    "serialize and deserialize ActorRef successfully" in {
      import scala.concurrent.duration._
      implicit val timeout = 5.seconds
      val actorRef = Await.result(system.actorSelection("akka://test-system/user/test-actor").resolveOne()(timeout), 6.seconds)

      val serialized = serialization.serialize(actorRef)
      serialized.isSuccess should equal(true)

      val deserialized = serialization.deserialize(serialized.get, classOf[ActorRef])
      deserialized.isSuccess should equal(true)

      deserialized.get.equals(actorRef) should equal(true)
    }

  }

  class DummyActor extends Actor {
    override def receive: Receive = { case msg => println(msg) }
  }
}
