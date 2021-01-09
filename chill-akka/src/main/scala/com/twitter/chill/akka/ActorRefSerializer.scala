package com.twitter.chill.akka

/**
 * Copyright 2012 Roman Levenstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
import akka.actor.{ActorPath, ActorRef, ExtendedActorSystem}
import akka.serialization.Serialization
import com.esotericsoftware.kryo.kryo5.{Kryo, Serializer}
import com.esotericsoftware.kryo.kryo5.io.{Input, Output}

import com.twitter.chill.{toRich, IKryoRegistrar}

/**
 * * This module provides helper classes for serialization of Akka-specific classes.
 *
 * @author
 *   Roman Levenstein
 * @author
 *   P. Oscar Boykin
 */
class ActorRefSerializer(system: ExtendedActorSystem) extends Serializer[ActorRef] with IKryoRegistrar {
  def apply(kryo: Kryo): Unit =
    if (!kryo.alreadyRegistered(classOf[ActorRef])) {
      kryo.forClass[ActorRef](this)
      kryo.forSubclass[ActorRef](this)
    }

  override def read(kryo: Kryo, input: Input, typ: Class[_ <: ActorRef]): ActorRef = {
    val path = ActorPath.fromString(input.readString())
    system.provider.resolveActorRef(path)
  }

  override def write(kryo: Kryo, output: Output, obj: ActorRef): Unit =
    output.writeString(Serialization.serializedActorPath(obj))
}
