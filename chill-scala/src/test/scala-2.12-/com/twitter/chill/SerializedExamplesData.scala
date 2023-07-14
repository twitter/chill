/*
Copyright 2019 Twitter, Inc.

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

import scala.collection.JavaConverters
import scala.collection.JavaConverters._
import scala.collection.immutable.{HashMap, HashSet, ListMap, ListSet, NumericRange, Queue}
import scala.runtime.VolatileByteRef

object SerializedExamplesData {
  val Examples = Seq(
    0 -> ("AgI=" -> Int.box(1)),
    1 -> ("AwFhYuM=" -> "abc"),
    2 -> ("BAAAgD8=" -> Float.box(1)),
    3 -> ("BQE=" -> Boolean.box(true)),
    4 -> ("BgE=" -> Byte.box(1)),
    5 -> ("B2EA" -> Char.box('a')),
    6 -> ("CAEA" -> Short.box(1)),
    7 -> ("CQI=" -> Long.box(1)),
    8 -> ("CgAAAAAAAPA/" -> Double.box(1)),
    // 9 -> void is a special case
    // Note: Instead of JavaConverters.***Converter(***).as***, in Scala 2.12
    // methods JavaConverters.*** can be used directly. For backwards compatibility,
    // the legacy methods to convert are used here.
    9 -> ("CwEBAHNjYWxhLmNvbGxlY3Rpb24uY29udmVydC5XcmFwcGVyc6QBdgEBAgQ=" ->
      JavaConverters.seqAsJavaListConverter(Seq(2)).asJava), // Wrappers$SeqWrapper
    10 -> ("DAEBAHNjYWxhLmNvbGxlY3Rpb24uY29udmVydC5XcmFwcGVyc6QBAQFzY2FsYS5jb2xsZWN0aW9uLkluZGV4ZWRTZXFMaWtlJEVsZW1lbnTzAWwBAQIBYAECBAIA" ->
      JavaConverters.asJavaIteratorConverter(Iterator(2)).asJava), // Wrappers$IteratorWrapper
    11 -> ("DQEBAHNjYWxhLmNvbGxlY3Rpb24uY29udmVydC5XcmFwcGVyc6QBGQEBJgECBAIE" ->
      JavaConverters.mapAsJavaMapConverter(Map(2 -> 2)).asJava), // Wrappers$MapWrapper
    12 -> ("DgEBAHNjYWxhLmNvbGxlY3Rpb24uY29udmVydC5XcmFwcGVyc6QBAQFqYXZhLnV0aWwuQ29sbGVjdGlvbnMkU2luZ2xldG9uTGlz9AECBA==" ->
      JavaConverters
        .asScalaBufferConverter(_root_.java.util.Collections.singletonList(2))
        .asScala), // Wrappers$JListWrapper
    13 -> ("DwEBAHNjYWxhLmNvbGxlY3Rpb24uY29udmVydC5XcmFwcGVyc6QBAQFqYXZhLnV0aWwuQ29sbGVjdGlvbnMkU2luZ2xldG9uTWHwAQIEAgQ=" ->
      JavaConverters
        .mapAsScalaMapConverter(_root_.java.util.Collections.singletonMap(2, 2))
        .asScala), // Wrappers$JMapWrapper
    14 -> ("EAECBA==" -> Some(2)),
    15 -> ("EQECBA==" -> Left(2)),
    16 -> ("EgECBA==" -> Right(2)),
    17 -> ("EwEBAgQ=" -> Vector(2)),
    18 -> ("FAEBAgQ=" -> Set(2)),
    19 -> ("FQECAgQCBg==" -> Set(2, 3)),
    20 -> ("FgEDAgQCBgII" -> Set(2, 3, 4)),
    21 -> ("FwEEAgQCBgIIAgo=" -> Set(2, 3, 4, 5)),
    // 22 -> class HashSet$HashTrieSet
    23 -> ("GQEBJgECBAIG" -> Map(2 -> 3)),
    24 -> ("GgECJgECBAIGJgECCAIK" -> Map(2 -> 3, 4 -> 5)),
    25 -> ("GwEDJgECBAIGJgECCAIKJgECDAIO" -> Map(2 -> 3, 4 -> 5, 6 -> 7)),
    26 -> ("HAEEJgECBAIGJgECCAIKJgECDAIOJgECEAIS" -> Map(2 -> 3, 4 -> 5, 6 -> 7, 8 -> 9)),
    // 27 -> class HashMap$HashTrieMap
    28 -> ("HgEMAAwIBgI=" -> new Range.Inclusive(3, 6, 1)),
    29 -> ("HwEBAgoAAQABAHNjYWxhLm1hdGguTnVtZXJpYyRJbnRJc0ludGVncmFspAEBAAMIAgQCAg==" ->
      new NumericRange.Inclusive[Int](2, 5, 1)),
    30 -> ("IAEBAgoAAAABAHNjYWxhLm1hdGguTnVtZXJpYyRJbnRJc0ludGVncmFspAEBAAMGAgQCAg==" ->
      new NumericRange.Exclusive[Int](2, 5, 1)),
    31 -> ("IQECAgYCCg==" -> scala.collection.mutable.BitSet(3, 5)),
    32 -> ("IgEBJgECBgIK" -> scala.collection.mutable.HashMap(3 -> 5)),
    33 -> ("IwEBAgY=" -> scala.collection.mutable.HashSet(3)),
    34 -> ("JAF2AQECBA==" -> Seq(2).asJavaCollection), // Wrappers$IterableWrapper
    35 -> ("JQEDAYJh" -> Tuple1("a")),
    36 -> ("JgEDAYJhAwGCYg==" -> ("a", "b")),
    37 -> ("JwECAgIEAgY=" -> (1, 2, 3)),
    38 -> ("KAECAgIEAgYCCA==" -> (1, 2, 3, 4)),
    39 -> ("KQECAgIEAgYCCAIK" -> (1, 2, 3, 4, 5)),
    40 -> ("KgECAgIEAgYCCAIKAgw=" -> (1, 2, 3, 4, 5, 6)),
    41 -> ("KwECAgIEAgYCCAIKAgwCDg==" -> (1, 2, 3, 4, 5, 6, 7)),
    42 -> ("LAECAgIEAgYCCAIKAgwCDgIQ" -> (1, 2, 3, 4, 5, 6, 7, 8)),
    43 -> ("LQECAgIEAgYCCAIKAgwCDgIQAhI=" -> (1, 2, 3, 4, 5, 6, 7, 8, 9)),
    44 -> ("LgECAgIEAgYCCAIKAgwCDgIQAhICAA==" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0)),
    45 -> ("LwECAgIEAgYCCAIKAgwCDgIQAhICAAIC" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1)),
    46 -> ("MAECAgIEAgYCCAIKAgwCDgIQAhICAAICAgQ=" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2)),
    47 -> ("MQECAgIEAgYCCAIKAgwCDgIQAhICAAICAgQCBg==" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3)),
    48 -> ("MgECAgIEAgYCCAIKAgwCDgIQAhICAAICAgQCBgII" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4)),
    49 -> ("MwECAgIEAgYCCAIKAgwCDgIQAhICAAICAgQCBgIIAgo=" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5)),
    50 -> ("NAECAgIEAgYCCAIKAgwCDgIQAhICAAICAgQCBgIIAgoCDA==" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6)),
    51 -> ("NQECAgIEAgYCCAIKAgwCDgIQAhICAAICAgQCBgIIAgoCDAIO" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7)),
    52 -> ("NgECAgIEAgYCCAIKAgwCDgIQAhICAAICAgQCBgIIAgoCDAIOAhA=" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8)),
    53 -> ("NwECAgIEAgYCCAIKAgwCDgIQAhICAAICAgQCBgIIAgoCDAIOAhACEg==" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9)),
    54 -> ("OAECAgIEAgYCCAIKAgwCDgIQAhICAAICAgQCBgIIAgoCDAIOAhACEgIA" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0)),
    55 -> ("OQECAgIEAgYCCAIKAgwCDgIQAhICAAICAgQCBgIIAgoCDAIOAhACEgIAAgI=" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1)),
    56 -> ("OgECAgIEAgYCCAIKAgwCDgIQAhICAAICAgQCBgIIAgoCDAIOAhACEgIAAgICBA==" -> (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2)),
    57 -> ("OwEBAAAAAAAAAA==" -> Tuple1(1L)),
    58 -> ("PAEBAAAA" -> Tuple1(1)),
    59 -> ("PQEAAAAAAADwPw==" -> Tuple1(1.0)),
    60 -> ("PgEBAAAAAAAAAAIAAAAAAAAA" -> (1L, 2L)),
    61 -> ("PwEBAAAAAAAAAAIAAAA=" -> (1L, 2)),
    62 -> ("QAEBAAAAAAAAAAAAAAAAAABA" -> (1L, 2.0)),
    63 -> ("QQEBAAAAAgAAAAAAAAA=" -> (1, 2L)),
    64 -> ("QgEBAAAAAgAAAA==" -> (1, 2)),
    65 -> ("QwEBAAAAAAAAAAAAAEA=" -> (1, 2.0)),
    66 -> ("RAEAAAAAAADwPwIAAAAAAAAA" -> (1.0, 2L)),
    67 -> ("RQEAAAAAAADwPwIAAAA=" -> (1.0, 2)),
    68 -> ("RgEAAAAAAADwPwAAAAAAAABA" -> (1.0, 2.0)),
    69 -> ("RwGCYQ==" -> Symbol("a")),
    // 70 -> interface scala.reflect.ClassTag
    71 -> ("SQE=" -> runtime.BoxedUnit.UNIT),
    72 -> ("SgEDaQICAgQCBg==" -> _root_.java.util.Arrays.asList(1, 2, 3)),
    73 -> ("SwECAAAAAAAAAAAAAAAAAAAAAA==" -> new _root_.java.util.BitSet(65)),
    74 -> ("TAEAAA==" -> new _root_.java.util.PriorityQueue[Int](7)),
    75 -> ("TQFhKuI=" -> _root_.java.util.regex.Pattern.compile("a*b")),
    76 -> ("TgEA" -> new _root_.java.sql.Date(0)),
    77 -> ("TwEH" -> new _root_.java.sql.Time(7)),
    78 -> ("UAEDwI23AQ==" -> new _root_.java.sql.Timestamp(3)),
    79 -> ("UQGB" -> new _root_.java.net.URI("")),
    80 -> ("UgEwLjAuMC6wAg==" -> new _root_.java.net.InetSocketAddress(2)),
    81 -> ("UwECBA==" -> new _root_.java.util.UUID(1, 2)),
    82 -> ("VAGs7QAFc3IAEGphdmEudXRpbC5Mb2NhbGV++BFgnDD57AMABkkACGhhc2hjb2RlTAAHY291bnRyeXQAEkxqYXZhL2xhbmcvU3RyaW5nO0wACmV4dGVuc2lvbnNxAH4AAUwACGxhbmd1YWdlcQB+AAFMAAZzY3JpcHRxAH4AAUwAB3ZhcmlhbnRxAH4AAXhw/////3QAAHEAfgADdAACZW5xAH4AA3EAfgADeA==" ->
      _root_.java.util.Locale.ENGLISH),
    // 83 -> class java.text.SimpleDateFormat - this case has two very special aspects:
    // a) SimpleDateFormat("") serializes to about 40.000 bytes
    // b) each time you serialize SimpleDateFormat(""), you get a slightly different binary representation.
    // Probably, one should write a custom serializer for this class...
    84 -> ("VgEBAGphdmEudXRpbC5Db2xsZWN0aW9ucyRFbXB0eUxpc/QB" ->
      _root_.java.util.Collections.unmodifiableCollection(_root_.java.util.Collections.EMPTY_LIST)),
    85 -> ("VwEBAGphdmEudXRpbC5Db2xsZWN0aW9ucyRFbXB0eUxpc/QB" ->
      _root_.java.util.Collections.unmodifiableList(_root_.java.util.Collections.EMPTY_LIST)),
    86 -> ("WAEBAGphdmEudXRpbC5MaW5rZWRMaXP0AQE=" ->
      _root_.java.util.Collections.unmodifiableList(new _root_.java.util.LinkedList[Int]())),
    87 -> ("WQEBAGphdmEudXRpbC5Db2xsZWN0aW9ucyRTaW5nbGV0b25NYfABAgICBA==" ->
      _root_.java.util.Collections
        .unmodifiableMap[Int, Int](_root_.java.util.Collections.singletonMap(1, 2))),
    88 -> ("WgEBAGphdmEudXRpbC5Db2xsZWN0aW9ucyRFbXB0eVNl9AE=" ->
      _root_.java.util.Collections.unmodifiableSet(_root_.java.util.Collections.EMPTY_SET)),
    89 -> ("WwEBAGphdmEudXRpbC5Db2xsZWN0aW9ucyRVbm1vZGlmaWFibGVOYXZpZ2FibGVNYXAkRW1wdHlOYXZpZ2FibGVNYfABAQ==" ->
      _root_.java.util.Collections
        .unmodifiableSortedMap[Int, Int](_root_.java.util.Collections.emptySortedMap())),
    // 90 -> class java.util.Collections$UnmodifiableSortedSet
    // With the following implementation, we have a problem
    // 90 -> ("XQEBAMEBamF2YS51dGlsLkNvbGxlY3Rpb25zJFVubW9kaWZpYWJsZU5hdmlnYWJsZVNldCRFbXB0eU5hdmlnYWJsZVNldAEA" ->
    //   _root_.java.util.Collections.unmodifiableSortedSet[Int](_root_.java.util.Collections.emptySortedSet())),
    // because we get an exception in the test with the root cause:
    // com.twitter.chill.Instantiators$ can not access a member of class java.util.Collections$UnmodifiableNavigableSet$EmptyNavigableSet with modifiers "public"
    // 91 -> class com.esotericsoftware.kryo.serializers.ClosureSerializer$Closure"""
    92 -> ("XgECgA==" -> Array(Byte.MinValue)),
    93 -> ("XwEC/38=" -> Array(Short.MaxValue)),
    94 -> ("YAEC/////w8=" -> Array(Int.MinValue)),
    95 -> ("YQEC/v//////////" -> Array(Long.MaxValue)),
    96 -> ("YgECAQAAAA==" -> Array(Float.MinPositiveValue)),
    97 -> ("YwEC////////7/8=" -> Array(Double.MinValue)),
    98 -> ("ZAECAQ==" -> Array(true)),
    99 -> ("ZQECeAA=" -> Array('x')),
    100 -> ("ZgECAWNh9A==" -> Array("cat")),
    101 -> ("ZwEDAgQDAW1vdXPl" -> Array(2, "mouse")),
    102 -> ("aAECAQ==" -> classOf[Int]),
    103 -> ("aQE=" -> new Object()),
    104 -> ("agEBBgFeAQKA" -> wrapByteArray(Array(Byte.MinValue))),
    105 -> ("awEBCAFfAQL/fw==" -> wrapShortArray(Array(Short.MaxValue))),
    106 -> ("bAEBAgFgAQL/////Dw==" -> wrapIntArray(Array(Int.MinValue))),
    107 -> ("bQEBCQFhAQL+//////////8=" -> wrapLongArray(Array(Long.MaxValue))),
    108 -> ("bgEBBAFiAQIBAAAA" -> wrapFloatArray(Array(Float.MinPositiveValue))),
    109 -> ("bwEBCgFjAQL////////v/w==" -> wrapDoubleArray(Array(Double.MinValue))),
    110 -> ("cAEBBQFkAQIB" -> wrapBooleanArray(Array(true))),
    111 -> ("cQEBBwFlAQJ4AA==" -> wrapCharArray(Array('x'))),
    112 -> ("cgEBA2YBAgFjYfQ=" -> wrapRefArray(Array("cat"))),
    113 -> ("cwE=" -> None),
    114 -> ("dAEA" -> collection.immutable.Queue()),
    115 -> ("dQEA" -> Nil),
    116 -> ("dgEBAgQ=" -> (2 :: Nil)),
    117 -> ("dwEGAAQEAgI=" -> collection.immutable.Range(1, 3)),
    118 -> ("eAEBdGHj" -> wrapString("tac")),
    119 -> ("eQECfQECBAIG" -> collection.immutable.TreeSet(3, 2)),
    120 -> ("egEBfQEmAQIGAgQ=" -> collection.immutable.TreeMap(3 -> 2)),
    121 -> ("ewE=" -> math.Ordering.Byte),
    122 -> ("fAE=" -> math.Ordering.Short),
    123 -> ("fQE=" -> math.Ordering.Int),
    124 -> ("fgE=" -> math.Ordering.Long),
    125 -> ("fwE=" -> math.Ordering.Float),
    126 -> ("gAEB" -> math.Ordering.Double),
    127 -> ("gQEB" -> math.Ordering.Boolean),
    128 -> ("ggEB" -> math.Ordering.Char),
    129 -> ("gwEB" -> math.Ordering.String),
    130 -> ("hAEBAA==" -> Set[Any]()),
    131 -> ("hQEBAA==" -> ListSet[Any]()),
    132 -> ("hgEBAUcBgmE=" -> ListSet[Any]('a)),
    133 -> ("hwEBAA==" -> HashSet[Any]()),
    134 -> ("iAEBAUcBgmE=" -> HashSet[Any]('a)),
    135 -> ("iQEBAA==" -> Map[Any, Any]()),
    136 -> ("igEBAA==" -> HashMap[Any, Any]()),
    137 -> ("iwEBASYBRwGCYUcE" -> HashMap('a -> 'a)),
    138 -> ("jAEBAA==" -> ListMap[Any, Any]()),
    139 -> ("jQEBASYBRwGCYUcE" -> ListMap('a -> 'a)),
    140 -> ("jgEBdgEBAgI=" -> Stream(1)),
    141 -> ("jwEB" -> Stream()),
    142 -> ("kAEBCg==" -> new VolatileByteRef(10)),
    143 -> ("kQEBAQBqYXZhLm1hdGguQmlnRGVjaW1h7AECAgA=" -> math.BigDecimal(2)),
    144 -> ("kgEBAA==" -> (Queue.empty[Any], true)),
    145 -> ("kwEBASYBAgICBA==" -> (Map(1 -> 2).filterKeys(_ != 2).toMap, true)),
    146 -> ("lAEBASYBAgICBg==" -> (Map(1 -> 2).mapValues(_ + 1).toMap, true)),
    147 -> ("lQEBAQIC" -> (Map(1 -> 2).keySet, true))
  )

  val SpecialCasesNotInExamplesMap: Seq[Int] = Seq(22, 27, 70, 83, 90, 91)

  // In older Scala versions, instances of the following classes have a serialized representation that differs from
  // the current Scala version 2.12.17:
  // 10 -> scala.collection.convert.Wrappers.IteratorWrapper
  // 28 -> scala.collection.immutable.Range$Inclusive
  // 117 -> scala.collection.immutable.Range

  val OmitExamplesInScalaVersion: Map[String, Seq[Int]] =
    Map("2.10." -> Seq(10, 28, 117), "2.11." -> Seq(28, 117))
}
