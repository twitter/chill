package com.twitter.chill

import org.scalatest.{ Matchers, WordSpec }

class SerializedExamplesOfStandardDataSpec extends WordSpec with Matchers {
  s"""
    |Projects using chill to persist serialized data (for example in event sourcing
    |scenarios) depend on the serialized representation of the pre-registered
    |classes being stable. Therefore, it is important that updates to chill avoid
    |changing the serialization of the pre-registered classes as far as possible.
    |When changing a serialization becomes necessary, details of the changes should
    |be mentioned in the release notes.
    |
    |For the ScalaKryoInstantiators, the registered classes""".stripMargin
    .should {

      "serialize to the provided examples".in {
        checkDeserialization("dAE=", None)
        checkDeserialization("dAE=", Some(2))
      }
    }

  val kryo: KryoPool =
    KryoPool.withByteArrayOutputStream(4, new ScalaKryoInstantiator())
  def err(message: String): Unit =
    System.err.println(s"\n##########\n$message\n##########\n")
  def err(message: String, serialized: Array[Byte]): Unit =
    System.err.println(
      s"\n##########\n$message\nThe example serialized is ${Base64.encodeBytes(serialized)}\n##########\n")
  def checkDeserialization(example: String, expected: AnyRef): Unit = {
    val serialized = try kryo.toBytesWithClass(expected)
    catch {
      case e: Throwable => err(s"can't kryo serialize $expected: $e"); throw e
    }
    val bytes = try Base64.decode(example)
    catch {
      case e: Throwable => err(s"can't base64 decode $example: $e", serialized); throw e
    }
    val deserialized = try kryo.fromBytes(bytes)
    catch {
      case e: Throwable => err(s"can't kryo deserialize $example: $e", serialized); throw e
    }
    assert(deserialized == expected)
  }
}
