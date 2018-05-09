package com.twitter.chill

import scala.collection.JavaConverters

import org.scalatest.{ Matchers, WordSpec }

class SerializedExamplesOfStandardDataSpec extends WordSpec with Matchers {
  s"""
    |Projects using chill to persist serialized data (for example in event sourcing
    |scenarios) depend on the serialized representation of the pre-registered
    |classes being stable. Therefore, it is important that updates to chill avoid
    |changing the serialization of pre-registered classes as far as possible.
    |When changing a serialization becomes necessary, details of the changes should
    |be mentioned in the release notes.
    |
    |With ScalaKryoInstantiators, the registered classes""".stripMargin
    .should {
      "serialize to the provided examples and back with the provided serialization id"
        .in {
          examples.foreach {
            case (serId, (serialized, scala)) =>
              checkSerialization(serialized, serId, scala)
          }
        }
      "should all be covered by an example".in {
        val serIds = examples.map(_._1)
        assert(serIds == serIds.distinct,
          "duplicate keys in examples map detected")
        val exampleStrings = examples.map(_._2._1)
        assert(exampleStrings == exampleStrings.distinct,
          "duplicate example strings in examples map detected")
        val specialCasesNotInExamplesMap = Seq(9) // no way to write an example for 9 -> void
        assert((serIds ++ specialCasesNotInExamplesMap).sorted ==
          Seq.range(0, kryo.getNextRegistrationId),
          "examples missing for preregistered classes")
      }
    }

  val examples = Seq(
    0 -> ("AgI=" -> Int.box(1)),
    1 -> ("AwFhYuM=" -> "abc"),
    2 -> ("BD+AAAA=" -> Float.box(1)),
    3 -> ("BQE=" -> Boolean.box(true)),
    4 -> ("BgE=" -> Byte.box(1)),
    5 -> ("BwBh" -> Char.box('a')),
    6 -> ("CAAB" -> Short.box(1)),
    7 -> ("CQI=" -> Long.box(1)),
    8 -> ("Cj/wAAAAAAAA" -> Double.box(1)),
    // 9 -> void is a special case
    10 -> ("DAEBAHNjYWxhLmNvbGxlY3Rpb24uY29udmVydC5XcmFwcGVyc6QBdwEBAgQ=" ->
      JavaConverters.seqAsJavaList(Seq(2))), // Wrappers$SeqWrapper
    // FIXME equals seems not to work...
    11 -> ("DQEBAHNjYWxhLmNvbGxlY3Rpb24uY29udmVydC5XcmFwcGVyc6QBAQFzY2FsYS5jb2xsZWN0aW9uLkluZGV4ZWRTZXFMaWtlJEVsZW1lbnTzAW0BAQIBYQECBAIA" ->
      JavaConverters.asJavaIterator(Iterator(2))), // Wrappers$IteratorWrapper
    12 -> ("EQECBA==" -> Some(2)), // Wrappers$MapWrapper
    13 -> ("EQECBA==" -> Some(2)),
    14 -> ("EQECBA==" -> Some(2)),
    15 -> ("EQECBA==" -> Some(2)),
    16 -> ("EgECBA==" -> Left(2)),
    17 -> ("EwECBA==" -> Right(2)),
    18 -> ("FAEBAgQ=" -> Vector(2)),
    114 -> ("dAE=" -> None))

  val kryo: KryoBase = {
    val instantiator = new ScalaKryoInstantiator()
    instantiator.setRegistrationRequired(true)
    instantiator.newKryo
  }
  val pool: KryoPool =
    KryoPool.withByteArrayOutputStream(4, new ScalaKryoInstantiator())
  def err(message: String): Unit =
    System.err.println(s"\n##########\n$message\n##########\n")
  def err(message: String, serialized: String): Unit =
    System.err.println(
      s"\n##########\n$message\nThe example serialized is $serialized\n##########\n")

  def checkSerialization(example: String, serId: Int, expected: AnyRef): Unit = {
    val serialized =
      try Base64.encodeBytes(pool.toBytesWithClass(expected))
      catch {
        case e: Throwable =>
          err(s"can't kryo serialize $expected: $e"); throw e
      }
    val bytes =
      try Base64.decode(example)
      catch {
        case e: Throwable =>
          err(s"can't base64 decode $example: $e", serialized); throw e
      }
    val deserialized =
      try pool.fromBytes(bytes)
      catch {
        case e: Throwable =>
          err(s"can't kryo deserialize $example: $e", serialized); throw e
      }
    val idOfExpected = kryo.getRegistration(expected.getClass).getId
    assert(
      idOfExpected == serId,
      s"$expected is registered with ID $idOfExpected, but expected $serId")
    assert(
      deserialized == expected,
      s"deserializing $example yields $deserialized, but expected $expected which serializes to $serialized")
    assert(
      serialized == example,
      s"$expected serializes to $serialized, but the test example is $example")
  }
}
