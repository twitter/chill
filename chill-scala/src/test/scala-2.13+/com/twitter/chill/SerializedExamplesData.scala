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
import scala.collection.immutable.{ArraySeq, HashMap, HashSet, ListMap, ListSet, NumericRange, Queue}
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
    9 -> ("CwF3AQECBA==" ->
      JavaConverters.seqAsJavaListConverter(Seq(2)).asJava), // Wrappers$SeqWrapper
    10 -> ("DAEBAHNjYWxhLmNvbGxlY3Rpb24uQXJyYXlPcHMkQXJyYXlJdGVyYXRvciRtY0kkc/ABAgBhAQIEBA==" ->
      JavaConverters.asJavaIteratorConverter(Iterator(2)).asJava), // Wrappers$IteratorWrapper
    11 -> ("DQEZAQEmAQIEAgQ=" ->
      JavaConverters.mapAsJavaMapConverter(Map(2 -> 2)).asJava), // Wrappers$MapWrapper
    12 -> ("DgEBAGphdmEudXRpbC5Db2xsZWN0aW9ucyRTaW5nbGV0b25MaXP0AQIE" ->
      JavaConverters
        .asScalaBufferConverter(_root_.java.util.Collections.singletonList(2))
        .asScala), // Wrappers$JListWrapper
    13 -> ("DwEBAGphdmEudXRpbC5Db2xsZWN0aW9ucyRTaW5nbGV0b25NYfABAgQCBA==" ->
      JavaConverters
        .mapAsScalaMapConverter(_root_.java.util.Collections.singletonMap(2, 2))
        .asScala), // Wrappers$JMapWrapper
    14 -> ("EAECBA==" -> Some(2)),
    15 -> ("EQECBA==" -> Left(2)),
    16 -> ("EgECBA==" -> Right(2)),
    // 17 -> ("FAEBAgQ=" -> Vector(2)),
    // new vector classes in 2.13 see 144, 145
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
    29 -> ("HwECAgoAAAEAAQBzY2FsYS5tYXRoLk51bWVyaWMkSW50SXNJbnRlZ3JhbKQBAQADAgQCAg==" ->
      new NumericRange.Inclusive[Int](2, 5, 1)),
    30 -> ("IAECAgoAAAAAAQBzY2FsYS5tYXRoLk51bWVyaWMkSW50SXNJbnRlZ3JhbKQBAQADAgQCAg==" ->
      new NumericRange.Exclusive[Int](2, 5, 1)),
    31 -> ("IQECAgYCCg==" -> scala.collection.mutable.BitSet(3, 5)),
    32 -> ("IgEBJgECBgIK" -> scala.collection.mutable.HashMap(3 -> 5)),
    33 -> ("IwEBAgY=" -> scala.collection.mutable.HashSet(3)),
    34 -> ("JAF3AQECBA==" -> Seq(2).asJavaCollection), // Wrappers$IterableWrapper
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
    72 -> ("SgEDagICAgQCBg==" -> _root_.java.util.Arrays.asList(1, 2, 3)),
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
    // 91 -> com.esotericsoftware.kryo.kryo5.serializers.ClosureSerializer$Closure
    92 -> ("XgEGAAQEAgI=" -> collection.immutable.Range(1, 3)),
    93 -> ("XwECgA==" -> Array(Byte.MinValue)),
    94 -> ("YAEC/38=" -> Array(Short.MaxValue)),
    95 -> ("YQEC/////w8=" -> Array(Int.MinValue)),
    96 -> ("YgEC/v//////////" -> Array(Long.MaxValue)),
    97 -> ("YwECAQAAAA==" -> Array(Float.MinPositiveValue)),
    98 -> ("ZAEC////////7/8=" -> Array(Double.MinValue)),
    99 -> ("ZQECAQ==" -> Array(true)),
    100 -> ("ZgECeAA=" -> Array('x')),
    101 -> ("ZwECAWNh9A==" -> Array("cat")),
    102 -> ("aAEDAgQDAW1vdXPl" -> Array(2, "mouse")),
    103 -> ("aQECAQ==" -> classOf[Int]),
    104 -> ("agE=" -> new Object()),
    105 -> ("awEBBgFfAQKA" -> wrapByteArray(Array(Byte.MinValue))),
    106 -> ("bAEBCAFgAQL/fw==" -> wrapShortArray(Array(Short.MaxValue))),
    107 -> ("bQEBAgFhAQL/////Dw==" -> wrapIntArray(Array(Int.MinValue))),
    108 -> ("bgEBCQFiAQL+//////////8=" -> wrapLongArray(Array(Long.MaxValue))),
    109 -> ("bwEBBAFjAQIBAAAA" -> wrapFloatArray(Array(Float.MinPositiveValue))),
    110 -> ("cAEBCgFkAQL////////v/w==" -> wrapDoubleArray(Array(Double.MinValue))),
    111 -> ("cQEBBQFlAQIB" -> wrapBooleanArray(Array(true))),
    112 -> ("cgEBBwFmAQJ4AA==" -> wrapCharArray(Array('x'))),
    113 -> ("cwEBA2cBAgFjYfQ=" -> wrapRefArray(Array("cat"))),
    114 -> ("dAE=" -> None),
    115 -> ("dQEA" -> collection.immutable.Queue()),
    116 -> ("dgEA" -> Nil),
    117 -> ("dwEBAgQ=" -> (2 :: Nil)),
    // 118 -> ("dwEGAAQEAgIG" -> collection.immutable.Range(1, 3)),
    119 -> ("eQEBdGHj" -> wrapString("tac")),
    120 -> ("egECfgECBAIG" -> collection.immutable.TreeSet(3, 2)),
    121 -> ("ewEBfgEmAQIGAgQ=" -> collection.immutable.TreeMap(3 -> 2)),
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
    133 -> ("hwEBAUcBgmE=" -> ListSet[Any]('a)),
    134 -> ("iAEBAA==" -> Map[Any, Any]()),
    135 -> ("iQEBAA==" -> ListMap[Any, Any]()),
    136 -> ("igEBASYBRwGCYUcE" -> ListMap('a -> 'a)),
    137 -> ("iwEBdwEBAgI=" -> Stream(1)),
    138 -> ("jAEB" -> Stream()),
    139 -> ("jQEBCg==" -> new VolatileByteRef(10)),
    140 -> ("jgEBAQBqYXZhLm1hdGguQmlnRGVjaW1h7AECAgA=" -> math.BigDecimal(2)),
    141 -> ("jwEBAA==" -> (Queue.empty[Any], true)),
    142 -> ("kAEBAQIC" -> (Map(1 -> 2).keySet, true)),
    143 -> ("kQEBAA==" -> Vector.empty[Int]),
    144 -> ("kgEBAQIC" -> Vector(1)),
    145 -> ("kwEBQA" + "ICAg" * 42 + "IC" -> Vector.fill(1 << 5 + 1)(1)),
    // Skip BigVectors. too slow
    // 146 -> ("" -> Vector.fill(1 << 10 + 1)(1)),
    // 147 -> ("" -> Vector.fill(1 << 15 + 1)(1)),
    // 148 -> ("" -> Vector.fill(1 << 20 + 1)(1)),
    // 149 -> ("" -> Vector.fill(1 << 25 + 1)(1)),
    150 -> ("mAEBAQKA" -> ArraySeq(Byte.MinValue)),
    151 -> ("mQEBAQL/fw==" -> ArraySeq(Short.MaxValue)),
    152 -> ("mgEBAQL/////Dw==" -> ArraySeq(Int.MinValue)),
    153 -> ("mwEBAQL+//////////8=" -> ArraySeq(Long.MaxValue)),
    154 -> ("nAEBAQIBAAAA" -> ArraySeq(Float.MinPositiveValue)),
    155 -> ("nQEBAQL////////v/w==" -> ArraySeq(Double.MinValue)),
    156 -> ("ngEBAQIB" -> ArraySeq(true)),
    157 -> ("nwEBAQJ4AA==" -> ArraySeq('x')),
    158 -> ("oAEBZwECAWNh9A==" -> ArraySeq("cat"))
  )

  val SpecialCasesNotInExamplesMap: Seq[Int] = Seq(17, 22, 27, 70, 83, 90, 91, 118, 146, 147, 148, 149)

  val OmitExamplesInScalaVersion: Map[String, Seq[Int]] = Map.empty
}
