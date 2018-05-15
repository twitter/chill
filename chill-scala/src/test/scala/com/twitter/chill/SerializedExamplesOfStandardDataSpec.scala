package com.twitter.chill

import scala.collection.JavaConverters
import scala.collection.immutable.NumericRange

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
    |This spec verifies for all registered classes of ScalaKryoInstantiators
    |- that the serialization class id is as expected,
    |- that a scala instance serializes to the expected binary representation,
    |- that the binary representation after deserializing & serializing
    |  does not change.
    |Note that it would be difficult to implement an equals check comparing
    |the scala instance with the instance obtained from deserialization, so
    |this is not implemented here.
    |
    |With ScalaKryoInstantiators, the registered classes""".stripMargin
    .should {
      "serialize as expected to the correct value (see above for details)"
        .in {
          val scalaVersion = scala.util.Properties.versionNumberString
          val examplesToOmit = omitExamplesInScalaVersion
            .filterKeys(scalaVersion.startsWith)
            .values.flatten.toSet
          examples.foreach {
            case (serId, (serialized, scala)) =>
              if (examplesToOmit.contains(serId))
                println(s"### SerializedExamplesOfStandardDataSpec: Omitting $serId in scala $scalaVersion")
              else
                checkSerialization(serialized, serId, scala)
          }
        }
      "all be covered by an example".in {
        val serIds = examples.map(_._1)
        assert(serIds == serIds.distinct,
          "duplicate keys in examples map detected")
        val exampleStrings = examples.map(_._2._1)
        assert(exampleStrings == exampleStrings.distinct,
          "duplicate example strings in examples map detected")
        assert(
          (serIds ++ specialCasesNotInExamplesMap).sorted ==
            Seq.range(0, kryo.getNextRegistrationId),
          s"there are approx ${kryo.getNextRegistrationId - serIds.size - specialCasesNotInExamplesMap.size} " +
            "examples missing for preregistered classes")
      }
    }

  // In older Scala versions, instances of the following classes have a serialized representation that differs from
  // the current Scala version 2.12.4:
  // 11 -> scala.collection.convert.Wrappers.IteratorWrapper
  // 29 -> scala.collection.immutable.Range$Inclusive
  val omitExamplesInScalaVersion: Map[String, Seq[Int]] = Map(
    "2.10." -> Seq(11, 29),
    "2.11." -> Seq(29))

  val specialCasesNotInExamplesMap: Seq[Int] = Seq(
    9 // no way to write an example for 9 -> void
    ) // TODO remove once no more needed ++ Seq.range(19, 143).filterNot(_ == 114)

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
    // Note: Instead of JavaConverters.***Converter(***).as***, in Scala 2.12
    // methods JavaConverters.*** can be used directly. For backwards compatibility,
    // the legacy methods to convert are used here.
    10 -> ("DAEBAHNjYWxhLmNvbGxlY3Rpb24uY29udmVydC5XcmFwcGVyc6QBdwEBAgQ=" ->
      JavaConverters.seqAsJavaListConverter(Seq(2)).asJava), // Wrappers$SeqWrapper
    11 -> ("DQEBAHNjYWxhLmNvbGxlY3Rpb24uY29udmVydC5XcmFwcGVyc6QBAQFzY2FsYS5jb2xsZWN0aW9uLkluZGV4ZWRTZXFMaWtlJEVsZW1lbnTzAW0BAQIBYQECBAIA" ->
      JavaConverters.asJavaIteratorConverter(Iterator(2)).asJava), // Wrappers$IteratorWrapper
    12 -> ("DgEBAHNjYWxhLmNvbGxlY3Rpb24uY29udmVydC5XcmFwcGVyc6QBGgEBJwECBAIE" ->
      JavaConverters.mapAsJavaMapConverter(Map(2 -> 2)).asJava), // Wrappers$MapWrapper
    13 -> ("DwEBAHNjYWxhLmNvbGxlY3Rpb24uY29udmVydC5XcmFwcGVyc6QBAQFqYXZhLnV0aWwuQ29sbGVjdGlvbnMkU2luZ2xldG9uTGlz9AECBA==" ->
      JavaConverters.asScalaBufferConverter(_root_.java.util.Collections.singletonList(2)).asScala), // Wrappers$JListWrapper
    14 -> ("EAEBAHNjYWxhLmNvbGxlY3Rpb24uY29udmVydC5XcmFwcGVyc6QBAQFqYXZhLnV0aWwuQ29sbGVjdGlvbnMkU2luZ2xldG9uTWHwAQIEAgQ=" ->
      JavaConverters.mapAsScalaMapConverter(_root_.java.util.Collections.singletonMap(2, 2)).asScala), // Wrappers$JMapWrapper
    15 -> ("EQECBA==" -> Some(2)),
    16 -> ("EgECBA==" -> Left(2)),
    17 -> ("EwECBA==" -> Right(2)),
    18 -> ("FAEBAgQ=" -> Vector(2)),
    19 -> ("FQEBAgQ=" -> Set(2)),
    20 -> ("FgECAgQCBg==" -> Set(2, 3)),
    21 -> ("FwEDAgQCBgII" -> Set(2, 3, 4)),
    22 -> ("GAEEAgQCBgIIAgo=" -> Set(2, 3, 4, 5)),
    // 23 -> TODO find a way to instantiate HashSet$HashTrieSet
    24 -> ("GgEBJwECBAIG" -> Map(2 -> 3)),
    25 -> ("GwECJwECBAIGJwECCAIK" -> Map(2 -> 3, 4 -> 5)),
    26 -> ("HAEDJwECBAIGJwECCAIKJwECDAIO" -> Map(2 -> 3, 4 -> 5, 6 -> 7)),
    27 -> ("HQEEJwECBAIGJwECCAIKJwECDAIOJwECEAIS" -> Map(2 -> 3, 4 -> 5, 6 -> 7, 8 -> 9)),
    // 28 -> TODO find a way to instantiate HashMap$HashTrieMap
    29 -> ("HwEMAAwIBgI=" -> new Range.Inclusive(3, 6, 1)),
    30 -> ("IAEBAgoAAQABAHNjYWxhLm1hdGguTnVtZXJpYyRJbnRJc0ludGVncmFspAEBAAMIAgQCAg==" ->
      new NumericRange.Inclusive[Int](2, 5, 1)),
    31 -> ("IQEBAgoAAAABAHNjYWxhLm1hdGguTnVtZXJpYyRJbnRJc0ludGVncmFspAEBAAMGAgQCAg==" ->
      new NumericRange.Exclusive[Int](2, 5, 1)),
    32 -> ("IgECAgYCCg==" -> scala.collection.mutable.BitSet(3, 5)),
    33 -> ("IwEBJwECBgIK" -> scala.collection.mutable.HashMap(3 -> 5)),
    34 -> ("JAEBAgY=" -> scala.collection.mutable.HashSet(3)),
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

  def checkSerialization(serializedExample: String,
    expectedSerializationId: Int,
    scalaInstance: AnyRef): Unit = {
    val idForScalaInstance = kryo.getRegistration(scalaInstance.getClass).getId
    assert(
      idForScalaInstance == expectedSerializationId,
      s"$scalaInstance is registered with ID $idForScalaInstance, but expected $expectedSerializationId")

    val serializedScalaInstance =
      try Base64.encodeBytes(pool.toBytesWithClass(scalaInstance))
      catch {
        case e: Throwable =>
          err(s"can't kryo serialize $scalaInstance: $e")
          throw e
      }
    assert(
      serializedScalaInstance == serializedExample,
      s"$scalaInstance with serialization id $idForScalaInstance serializes to $serializedScalaInstance, " +
        s"but the test example is $serializedExample")

    val bytes =
      try Base64.decode(serializedExample)
      catch {
        case e: Throwable =>
          err(s"can't base64 decode $serializedExample with serialization id $idForScalaInstance: $e", serializedScalaInstance)
          throw e
      }
    val deserialized =
      try pool.fromBytes(bytes)
      catch {
        case e: Throwable =>
          err(s"can't kryo deserialize $serializedExample with serialization id $idForScalaInstance: $e", serializedScalaInstance)
          throw e
      }
    val roundtrip =
      try Base64.encodeBytes(pool.toBytesWithClass(deserialized))
      catch {
        case e: Throwable =>
          err(s"can't kryo serialize roundtrip $deserialized with serialization id $idForScalaInstance: $e")
          throw e
      }

    assert(
      roundtrip == serializedExample,
      s"deserializing $serializedExample yields $deserialized (serialization id $idForScalaInstance), " +
        s"but expected $scalaInstance which serializes to $serializedScalaInstance")
  }
}
