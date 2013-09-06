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

import org.specs._

import akka.actor.{ ActorRef, ActorSystem }
import akka.serialization._
import com.typesafe.config.ConfigFactory

class AkkaTests extends Specification {

  noDetailedDiffs() //Fixes issue for scala 2.9

  val system = ActorSystem("example", ConfigFactory.parseString("""
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
      val serializer = serialization.findSerializerFor((1,2,3))
      serializer.getClass.equals(classOf[AkkaSerializer]) must beTrue
    }
  }
}
