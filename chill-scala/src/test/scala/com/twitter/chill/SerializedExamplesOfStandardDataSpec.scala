package com.twitter.chill

import scala.collection.{ JavaConversions, JavaConverters }
import scala.collection.immutable.{ HashMap, HashSet, ListMap, ListSet, NumericRange, Queue }
import scala.runtime.VolatileByteRef

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
            case (serId, (serialized, (scala: AnyRef, useObjectEquality: Boolean))) =>
              if (examplesToOmit.contains(serId))
                println(s"### SerializedExamplesOfStandardDataSpec: Omitting $serId in scala $scalaVersion")
              else
                checkSerialization(serialized, serId, scala, useObjectEquality)
            case (serId, (serialized, scala)) =>
              if (examplesToOmit.contains(serId))
                println(s"### SerializedExamplesOfStandardDataSpec: Omitting $serId in scala $scalaVersion")
              else
                checkSerialization(serialized, serId, scala, useObjectEquality = false)
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
  // the current Scala version 2.12.6:
  // 11 -> scala.collection.convert.Wrappers.IteratorWrapper
  // 29 -> scala.collection.immutable.Range$Inclusive
  val omitExamplesInScalaVersion: Map[String, Seq[Int]] = Map(
    "2.10." -> Seq(11, 29, 118),
    "2.11." -> Seq(29, 118))

  val specialCasesNotInExamplesMap: Seq[Int] = Seq(
    9, 23, 28, 71, 84, 91, 92)

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
    // 23 -> class HashSet$HashTrieSet
    24 -> ("GgEBJwECBAIG" -> Map(2 -> 3)),
    25 -> ("GwECJwECBAIGJwECCAIK" -> Map(2 -> 3, 4 -> 5)),
    26 -> ("HAEDJwECBAIGJwECCAIKJwECDAIO" -> Map(2 -> 3, 4 -> 5, 6 -> 7)),
    27 -> ("HQEEJwECBAIGJwECCAIKJwECDAIOJwECEAIS" -> Map(2 -> 3, 4 -> 5, 6 -> 7, 8 -> 9)),
    // 28 -> class HashMap$HashTrieMap
    29 -> ("HwEMAAwIBgI=" -> new Range.Inclusive(3, 6, 1)),
    30 -> ("IAEBAgoAAQABAHNjYWxhLm1hdGguTnVtZXJpYyRJbnRJc0ludGVncmFspAEBAAMIAgQCAg==" ->
      new NumericRange.Inclusive[Int](2, 5, 1)),
    31 -> ("IQEBAgoAAAABAHNjYWxhLm1hdGguTnVtZXJpYyRJbnRJc0ludGVncmFspAEBAAMGAgQCAg==" ->
      new NumericRange.Exclusive[Int](2, 5, 1)),
    32 -> ("IgECAgYCCg==" -> scala.collection.mutable.BitSet(3, 5)),
    33 -> ("IwEBJwECBgIK" -> scala.collection.mutable.HashMap(3 -> 5)),
    34 -> ("JAEBAgY=" -> scala.collection.mutable.HashSet(3)),
    35 -> ("JQF3AQECBA==" ->
      JavaConversions.asJavaCollection(Seq(2))), // Wrappers$IterableWrapper
    36 -> ("JgEDAYJh" -> Tuple1("a")),
    37 -> ("JwEDAYJhAwGCYg==" -> ("a", "b")),
    38 -> ("KAECAgIEAgY=" -> (1, 2, 3)),
    39 -> ("KQECAgIEAgYCCA==" -> (1, 2, 3, 4)),
    40 -> ("KgECAgIEAgYCCAIK" -> (1, 2, 3, 4, 5)),
    41 -> ("KwECAgIEAgYCCAIKAgw=" -> (1, 2, 3, 4, 5, 6)),
    42 -> ("LAECAgIEAgYCCAIKAgwCDg==" -> (1, 2, 3, 4, 5, 6, 7)),
    43 -> ("LQECAgIEAgYCCAIKAgwCDgIQ" -> (1, 2, 3, 4, 5, 6, 7, 8)),
    44 -> ("LgECAgIEAgYCCAIKAgwCDgIQAhI=" -> (1, 2, 3, 4, 5, 6, 7, 8, 9)),
    45 -> ("LwECAgIEAgYCCAIKAgwCDgIQAhICAA==" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0)),
    46 -> ("MAECAgIEAgYCCAIKAgwCDgIQAhICAAIC" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1)),
    47 -> ("MQECAgIEAgYCCAIKAgwCDgIQAhICAAICAgQ=" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2)),
    48 -> ("MgECAgIEAgYCCAIKAgwCDgIQAhICAAICAgQCBg==" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3)),
    49 -> ("MwECAgIEAgYCCAIKAgwCDgIQAhICAAICAgQCBgII" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4)),
    50 -> ("NAECAgIEAgYCCAIKAgwCDgIQAhICAAICAgQCBgIIAgo=" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5)),
    51 -> ("NQECAgIEAgYCCAIKAgwCDgIQAhICAAICAgQCBgIIAgoCDA==" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6)),
    52 -> ("NgECAgIEAgYCCAIKAgwCDgIQAhICAAICAgQCBgIIAgoCDAIO" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7)),
    53 -> ("NwECAgIEAgYCCAIKAgwCDgIQAhICAAICAgQCBgIIAgoCDAIOAhA=" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8)),
    54 -> ("OAECAgIEAgYCCAIKAgwCDgIQAhICAAICAgQCBgIIAgoCDAIOAhACEg==" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9)),
    55 -> ("OQECAgIEAgYCCAIKAgwCDgIQAhICAAICAgQCBgIIAgoCDAIOAhACEgIA" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0)),
    56 -> ("OgECAgIEAgYCCAIKAgwCDgIQAhICAAICAgQCBgIIAgoCDAIOAhACEgIAAgI=" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1)),
    57 -> ("OwECAgIEAgYCCAIKAgwCDgIQAhICAAICAgQCBgIIAgoCDAIOAhACEgIAAgICBA==" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2)),
    58 -> ("PAEAAAAAAAAAAQ==" -> Tuple1(1L)),
    59 -> ("PQEAAAAB" -> Tuple1(1)),
    60 -> ("PgE/8AAAAAAAAA==" -> Tuple1(1.0)),
    61 -> ("PwEAAAAAAAAAAQAAAAAAAAAC" -> (1L, 2L)),
    62 -> ("QAEAAAAAAAAAAQAAAAI=" -> (1L, 2)),
    63 -> ("QQEAAAAAAAAAAUAAAAAAAAAA" -> (1L, 2.0)),
    64 -> ("QgEAAAABAAAAAAAAAAI=" -> (1, 2L)),
    65 -> ("QwEAAAABAAAAAg==" -> (1, 2)),
    66 -> ("RAEAAAABQAAAAAAAAAA=" -> (1, 2.0)),
    67 -> ("RQE/8AAAAAAAAAAAAAAAAAAC" -> (1.0, 2L)),
    68 -> ("RgE/8AAAAAAAAAAAAAI=" -> (1.0, 2)),
    69 -> ("RwE/8AAAAAAAAEAAAAAAAAAA" -> (1.0, 2.0)),
    70 -> ("SAGCYQ==" -> Symbol("a")),
    // 71 -> interface scala.reflect.ClassTag
    72 -> ("SgE=" -> runtime.BoxedUnit.UNIT),
    73 -> ("SwEDagICAgQCBg==" -> _root_.java.util.Arrays.asList(1, 2, 3)),
    74 -> ("TAECAAAAAAAAAAAAAAAAAAAAAA==" -> new _root_.java.util.BitSet(65)),
    75 -> ("TQEAAA==" -> new _root_.java.util.PriorityQueue[Int](7)),
    76 -> ("TgFhKuI=" -> _root_.java.util.regex.Pattern.compile("a*b")),
    77 -> ("TwEA" -> new _root_.java.sql.Date(0)),
    78 -> ("UAEH" -> new _root_.java.sql.Time(7)),
    79 -> ("UQEDwI23AQ==" -> new _root_.java.sql.Timestamp(3)),
    80 -> ("UgGB" -> new _root_.java.net.URI("")),
    81 -> ("UwEwLjAuMC6wAg==" -> new _root_.java.net.InetSocketAddress(2)),
    82 -> ("VAECBA==" -> new _root_.java.util.UUID(1, 2)),
    83 -> ("VQGs7QAFc3IAEGphdmEudXRpbC5Mb2NhbGV++BFgnDD57AMABkkACGhhc2hjb2RlTAAHY291bnRyeXQAEkxqYXZhL2xhbmcvU3RyaW5nO0wACmV4dGVuc2lvbnNxAH4AAUwACGxhbmd1YWdlcQB+AAFMAAZzY3JpcHRxAH4AAUwAB3ZhcmlhbnRxAH4AAXhw/////3QAAHEAfgADdAACZW5xAH4AA3EAfgADeA==" ->
      _root_.java.util.Locale.ENGLISH),
    // 84 -> class java.text.SimpleDateFormat - this case has two very special aspects:
    // a) SimpleDateFormat("") serializes to about 40.000 bytes
    // b) each time you serialize SimpleDateFormat(""), you get a slightly different binary representation.
    // Probably, one should write a custom serializer for this class...
    85 -> ("VwEBAGphdmEudXRpbC5Db2xsZWN0aW9ucyRFbXB0eUxpc/QB" ->
      _root_.java.util.Collections.unmodifiableCollection(_root_.java.util.Collections.EMPTY_LIST)),
    86 -> ("WAEBAGphdmEudXRpbC5Db2xsZWN0aW9ucyRFbXB0eUxpc/QB" ->
      _root_.java.util.Collections.unmodifiableList(_root_.java.util.Collections.EMPTY_LIST)),
    87 -> ("WQEBAGphdmEudXRpbC5MaW5rZWRMaXP0AQA=" ->
      _root_.java.util.Collections.unmodifiableList(new _root_.java.util.LinkedList[Int]())),
    88 -> ("WgEBAGphdmEudXRpbC5Db2xsZWN0aW9ucyRTaW5nbGV0b25NYfABAgICBA==" ->
      _root_.java.util.Collections.unmodifiableMap[Int, Int](_root_.java.util.Collections.singletonMap(1, 2))),
    89 -> ("WwEBAGphdmEudXRpbC5Db2xsZWN0aW9ucyRFbXB0eVNl9AE=" ->
      _root_.java.util.Collections.unmodifiableSet(_root_.java.util.Collections.EMPTY_SET)),
    90 -> ("XAEBAMEBamF2YS51dGlsLkNvbGxlY3Rpb25zJFVubW9kaWZpYWJsZU5hdmlnYWJsZU1hcCRFbXB0eU5hdmlnYWJsZU1hcAEA" ->
      _root_.java.util.Collections.unmodifiableSortedMap[Int, Int](_root_.java.util.Collections.emptySortedMap())),
    // 91 -> class java.util.Collections$UnmodifiableSortedSet
    // With the following implementation, we have a problem
    // 91 -> ("XQEBAMEBamF2YS51dGlsLkNvbGxlY3Rpb25zJFVubW9kaWZpYWJsZU5hdmlnYWJsZVNldCRFbXB0eU5hdmlnYWJsZVNldAEA" ->
    //   _root_.java.util.Collections.unmodifiableSortedSet[Int](_root_.java.util.Collections.emptySortedSet())),
    // because we get an exception in the test with the root cause:
    // com.twitter.chill.Instantiators$ can not access a member of class java.util.Collections$UnmodifiableNavigableSet$EmptyNavigableSet with modifiers "public"
    // 92 -> class com.esotericsoftware.kryo.serializers.ClosureSerializer$Closure"""

    93 -> ("XwECgA==" -> Array(Byte.MinValue)),
    94 -> ("YAECf/8=" -> Array(Short.MaxValue)),
    95 -> ("YQEC/////w8=" -> Array(Int.MinValue)),
    96 -> ("YgEC/v//////////" -> Array(Long.MaxValue)),
    97 -> ("YwECAAAAAQ==" -> Array(Float.MinPositiveValue)),
    98 -> ("ZAEC/+////////8=" -> Array(Double.MinValue)),
    99 -> ("ZQECAQ==" -> Array(true)),
    100 -> ("ZgECAHg=" -> Array('x')),
    101 -> ("ZwECAWNh9A==" -> Array("cat")),
    102 -> ("aAEDAgQDAW1vdXPl" -> Array(2, "mouse")),
    103 -> ("aQECAQ==" -> classOf[Int]),
    104 -> ("agE=" -> new Object()),
    105 -> ("awEBBgFfAQKA" -> wrapByteArray(Array(Byte.MinValue))),
    106 -> ("bAEBCAFgAQJ//w==" -> wrapShortArray(Array(Short.MaxValue))),
    107 -> ("bQEBAgFhAQL/////Dw==" -> wrapIntArray(Array(Int.MinValue))),
    108 -> ("bgEBCQFiAQL+//////////8=" -> wrapLongArray(Array(Long.MaxValue))),
    109 -> ("bwEBBAFjAQIAAAAB" -> wrapFloatArray(Array(Float.MinPositiveValue))),
    110 -> ("cAEBCgFkAQL/7////////w==" -> wrapDoubleArray(Array(Double.MinValue))),
    111 -> ("cQEBBQFlAQIB" -> wrapBooleanArray(Array(true))),
    112 -> ("cgEBBwFmAQIAeA==" -> wrapCharArray(Array('x'))),
    113 -> ("cwEBAwBnAQIBY2H0" -> wrapRefArray(Array("cat"))),
    114 -> ("dAE=" -> None),
    115 -> ("dQEA" -> collection.immutable.Queue()),
    116 -> ("dgEA" -> Nil),
    117 -> ("dwEBAgQ=" -> (2 :: Nil)),
    118 -> ("eAEGAAQEAgI=" -> collection.immutable.Range(1, 3)),
    119 -> ("eQEBdGHj" -> wrapString("tac")),
    120 -> ("egECfgECBAIG" -> collection.immutable.TreeSet(3, 2)),
    121 -> ("ewEBfgEnAQIGAgQ=" -> collection.immutable.TreeMap(3 -> 2)),
    122 -> ("fAE=" -> math.Ordering.Byte),
    123 -> ("fQE=" -> math.Ordering.Short),
    124 -> ("fgE=" -> math.Ordering.Int),
    125 -> ("fwE=" -> math.Ordering.Long),
    126 -> ("gAEB" -> math.Ordering.Float),
    127 -> ("gQEB" -> math.Ordering.Double),
    128 -> ("ggEB" -> math.Ordering.Boolean),
    129 -> ("gwEB" -> math.Ordering.Char),
    130 -> ("hAEB" -> math.Ordering.String),
    131 -> ("hQEBAA==" -> Set[Any]()),
    132 -> ("hgEBAA==" -> ListSet[Any]()),
    133 -> ("hwEBAUgBgmE=" -> ListSet[Any]('a)),
    134 -> ("iAEBAA==" -> HashSet[Any]()),
    135 -> ("iQEBAUgBgmE=" -> HashSet[Any]('a)),
    136 -> ("igEBAA==" -> Map[Any, Any]()),
    137 -> ("iwEBAA==" -> HashMap()),
    138 -> ("jAEBAScBSAGCYUgE" -> HashMap('a -> 'a)),
    139 -> ("jQEBAA==" -> ListMap()),
    140 -> ("jgEBAScBSAGCYUgE" -> ListMap('a -> 'a)),
    141 -> ("jwEBdwEBAgI=" -> Stream(1)),
    142 -> ("kAEB" -> Stream()),
    143 -> ("kQEBCg==" -> new VolatileByteRef(10)),
    144 -> ("kgEBAQBqYXZhLm1hdGguQmlnRGVjaW1h7AECAgA=" -> math.BigDecimal(2)),
    145 -> ("kwEBAA==" -> (Queue.empty[Any], true)),
    146 -> ("lAEBAScBAgICBA==" -> (Map(1 -> 2).filterKeys(_ != 2), true)),
    147 -> ("lQEBAScBAgICBg==" -> (Map(1 -> 2).mapValues(_ + 1), true)),
    148 -> ("lgEBAQIC" -> (Map(1 -> 2).keySet, true)))

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
    scalaInstance: AnyRef,
    useObjectEquality: Boolean): Unit = {
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

    // Some objects like arrays can't be deserialized and compared with "==", so the roundtrip serialization is used
    // for checking. Other objects like Queue.empty can't be roundtrip compared, so object equality is checked.
    if (useObjectEquality) {
      assert(
        deserialized == scalaInstance,
        s"deserializing $serializedExample yields $deserialized (serialization id $idForScalaInstance), " +
          s"but expected $scalaInstance which does not equal to the deserialized example and which in turn " +
          s"serializes to $serializedScalaInstance")

    } else {
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
}
