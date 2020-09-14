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

package com.twitter.chill.backwardcomp

import com.esotericsoftware.kryo.util.Util.{className, log}
import com.esotericsoftware.kryo.util.{DefaultClassResolver, IdentityObjectIntMap, IntMap}
import com.esotericsoftware.kryo.{Kryo, KryoException, Registration}
import com.esotericsoftware.minlog.Log.{DEBUG, TRACE, trace}
import com.twitter.chill.{Input, Output}

class BackwardCompatibleClassResolver extends DefaultClassResolver {
  import BackwardCompatibleClassResolver._

  private val fallbackByClass: Map[Class[_], Int] =
    Map(
      classOf[FallbackToHashSet1] -> 23,
      classOf[FallbackToHashSet2] -> 23,
      classOf[FallbackToHashMap1] -> 28,
      classOf[FallbackToHashMap2] -> 28,
      classOf[FallbackToMap11] -> 24,
      classOf[FallbackToMap12] -> 24,
      classOf[Range.Exclusive] -> 118
    )

  override def getRegistration(`type`: Class[_]): Registration = {
    if (`type` == classOf[Range.Exclusive]) {
      getRegistration(118)
    } else {
      super.getRegistration(`type`)
    }
  }

  override def getRegistration(classID: Int): Registration = {
    val registration = super.getRegistration(classID)
    if (registration != null) {
      fallbackByClass.get(registration.getType) match {
        case Some(value) => super.getRegistration(value)
        case None =>
          if (registration.getType == classOf[Range]) {
            new Registration(
              classOf[Range.Exclusive],
              registration.getSerializer,
              registration.getId
            )
          } else {
            registration
          }
      }
    } else {
      null
    }
  }

  override def readClass(input: Input): Registration = {
    val classID = input.readVarInt(true)
    classID match {
      case Kryo.NULL =>
        if (TRACE || (DEBUG && kryo.getDepth == 1)) log("Read", null)
        null
      case 1 => // Offset for NAME and NULL.
        readName(input)
      case _ =>
        val registration = getRegistration(classID - 2)
        if (registration == null) throw new KryoException("Encountered unregistered class ID: " + (classID - 2))
        if (TRACE) trace("kryo", "Read class " + (classID - 2) + ": " + className(registration.getType))

        registration
    }
  }

  def readIgnoredClass(input: Input): Unit = {
    val classId = input.readVarInt(true)
    if (classId == 1) {
      val nameId = input.readVarInt(true)
      if (nameIdToClass == null) {
        nameIdToClass = new IntMap[Class[_]]
      }
      if (nameIdToClass.get(nameId) == null) {
        val name = input.readString()
        nameIdToClass.put(nameId, IgnoredClassPlaceholder.getClass)
      }
    }
  }

  def writeFakeName[T](output: Output, name: String, placeholderType: Class[_]): Unit = {
    output.writeVarInt(1, true)
    if (classToNameId != null) {
      val nameId = classToNameId.get(placeholderType, -1)
      if (nameId != -1) {
        output.writeVarInt(nameId, true)
      } else {
        writeClassNameOnFirstEncounter(output, name, placeholderType)
      }
    } else {
      writeClassNameOnFirstEncounter(output, name, placeholderType)
    }
  }

  private def writeClassNameOnFirstEncounter[T](output: Output, name: String, placeholderType: Class[_]) = {
    // Only write the class name the first time encountered in object graph.
    val nameId = nextNameId
    nextNameId += 1
    if (classToNameId == null) classToNameId = new IdentityObjectIntMap[Class[_]]
    classToNameId.put(placeholderType, nameId)
    output.writeVarInt(nameId, true)
    output.writeString(name)
  }
}

object BackwardCompatibleClassResolver {
  abstract class FallbackToHashSet1
  abstract class FallbackToHashSet2
  abstract class FallbackToHashMap1
  abstract class FallbackToHashMap2
  abstract class FallbackToMap11
  abstract class FallbackToMap12

  object IgnoredClassPlaceholder
}