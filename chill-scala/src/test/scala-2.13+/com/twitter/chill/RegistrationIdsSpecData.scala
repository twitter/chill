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

object RegistrationIdsSpecData {

  val Entries_0_9_5 =
    """0 -> int
      |1 -> class java.lang.String
      |2 -> float
      |3 -> boolean
      |4 -> byte
      |5 -> char
      |6 -> short
      |7 -> long
      |8 -> double
      |9 -> class scala.collection.convert.JavaCollectionWrappers$SeqWrapper
      |10 -> class scala.collection.convert.JavaCollectionWrappers$IteratorWrapper
      |11 -> class scala.collection.convert.JavaCollectionWrappers$MapWrapper
      |12 -> class scala.collection.convert.JavaCollectionWrappers$JListWrapper
      |13 -> class scala.collection.convert.JavaCollectionWrappers$JMapWrapper
      |14 -> class scala.Some
      |15 -> class scala.util.Left
      |16 -> class scala.util.Right
      |17 -> class scala.collection.immutable.Vector
      |18 -> class scala.collection.immutable.Set$Set1
      |19 -> class scala.collection.immutable.Set$Set2
      |20 -> class scala.collection.immutable.Set$Set3
      |21 -> class scala.collection.immutable.Set$Set4
      |22 -> class scala.collection.immutable.HashSet
      |23 -> class scala.collection.immutable.Map$Map1
      |24 -> class scala.collection.immutable.Map$Map2
      |25 -> class scala.collection.immutable.Map$Map3
      |26 -> class scala.collection.immutable.Map$Map4
      |27 -> class scala.collection.immutable.HashMap
      |28 -> class scala.collection.immutable.Range$Inclusive
      |29 -> class scala.collection.immutable.NumericRange$Inclusive
      |30 -> class scala.collection.immutable.NumericRange$Exclusive
      |31 -> class scala.collection.mutable.BitSet
      |32 -> class scala.collection.mutable.HashMap
      |33 -> class scala.collection.mutable.HashSet
      |34 -> class scala.collection.convert.JavaCollectionWrappers$IterableWrapper
      |35 -> class scala.Tuple1
      |36 -> class scala.Tuple2
      |37 -> class scala.Tuple3
      |38 -> class scala.Tuple4
      |39 -> class scala.Tuple5
      |40 -> class scala.Tuple6
      |41 -> class scala.Tuple7
      |42 -> class scala.Tuple8
      |43 -> class scala.Tuple9
      |44 -> class scala.Tuple10
      |45 -> class scala.Tuple11
      |46 -> class scala.Tuple12
      |47 -> class scala.Tuple13
      |48 -> class scala.Tuple14
      |49 -> class scala.Tuple15
      |50 -> class scala.Tuple16
      |51 -> class scala.Tuple17
      |52 -> class scala.Tuple18
      |53 -> class scala.Tuple19
      |54 -> class scala.Tuple20
      |55 -> class scala.Tuple21
      |56 -> class scala.Tuple22
      |57 -> class scala.Tuple1$mcJ$sp
      |58 -> class scala.Tuple1$mcI$sp
      |59 -> class scala.Tuple1$mcD$sp
      |60 -> class scala.Tuple2$mcJJ$sp
      |61 -> class scala.Tuple2$mcJI$sp
      |62 -> class scala.Tuple2$mcJD$sp
      |63 -> class scala.Tuple2$mcIJ$sp
      |64 -> class scala.Tuple2$mcII$sp
      |65 -> class scala.Tuple2$mcID$sp
      |66 -> class scala.Tuple2$mcDJ$sp
      |67 -> class scala.Tuple2$mcDI$sp
      |68 -> class scala.Tuple2$mcDD$sp
      |69 -> class scala.Symbol
      |70 -> interface scala.reflect.ClassTag
      |71 -> class scala.runtime.BoxedUnit
      |72 -> class java.util.Arrays$ArrayList
      |73 -> class java.util.BitSet
      |74 -> class java.util.PriorityQueue
      |75 -> class java.util.regex.Pattern
      |76 -> class java.sql.Date
      |77 -> class java.sql.Time
      |78 -> class java.sql.Timestamp
      |79 -> class java.net.URI
      |80 -> class java.net.InetSocketAddress
      |81 -> class java.util.UUID
      |82 -> class java.util.Locale
      |83 -> class java.text.SimpleDateFormat
      |84 -> class java.util.Collections$UnmodifiableCollection
      |85 -> class java.util.Collections$UnmodifiableRandomAccessList
      |86 -> class java.util.Collections$UnmodifiableList
      |87 -> class java.util.Collections$UnmodifiableMap
      |88 -> class java.util.Collections$UnmodifiableSet
      |89 -> class java.util.Collections$UnmodifiableSortedMap
      |90 -> class java.util.Collections$UnmodifiableSortedSet
      |91 -> class com.esotericsoftware.kryo.kryo5.serializers.ClosureSerializer$Closure
      |92 -> class scala.collection.immutable.Range$Exclusive
      |93 -> class [B
      |94 -> class [S
      |95 -> class [I
      |96 -> class [J
      |97 -> class [F
      |98 -> class [D
      |99 -> class [Z
      |100 -> class [C
      |101 -> class [Ljava.lang.String;
      |102 -> class [Ljava.lang.Object;
      |103 -> class java.lang.Class
      |104 -> class java.lang.Object
      |105 -> class scala.collection.mutable.ArraySeq$ofByte
      |106 -> class scala.collection.mutable.ArraySeq$ofShort
      |107 -> class scala.collection.mutable.ArraySeq$ofInt
      |108 -> class scala.collection.mutable.ArraySeq$ofLong
      |109 -> class scala.collection.mutable.ArraySeq$ofFloat
      |110 -> class scala.collection.mutable.ArraySeq$ofDouble
      |111 -> class scala.collection.mutable.ArraySeq$ofBoolean
      |112 -> class scala.collection.mutable.ArraySeq$ofChar
      |113 -> class scala.collection.mutable.ArraySeq$ofRef
      |114 -> class scala.None$
      |115 -> class scala.collection.immutable.Queue
      |116 -> class scala.collection.immutable.Nil$
      |117 -> class scala.collection.immutable.$colon$colon
      |118 -> class scala.collection.immutable.Range
      |119 -> class scala.collection.immutable.WrappedString
      |120 -> class scala.collection.immutable.TreeSet
      |121 -> class scala.collection.immutable.TreeMap
      |122 -> class scala.math.Ordering$Byte$
      |123 -> class scala.math.Ordering$Short$
      |124 -> class scala.math.Ordering$Int$
      |125 -> class scala.math.Ordering$Long$
      |126 -> class scala.math.Ordering$Float$
      |127 -> class scala.math.Ordering$Double$
      |128 -> class scala.math.Ordering$Boolean$
      |129 -> class scala.math.Ordering$Char$
      |130 -> class scala.math.Ordering$String$
      |131 -> class scala.collection.immutable.Set$EmptySet$
      |132 -> class scala.collection.immutable.ListSet$EmptyListSet$
      |133 -> class scala.collection.immutable.ListSet$Node
      |134 -> class scala.collection.immutable.Map$EmptyMap$
      |135 -> class scala.collection.immutable.ListMap$EmptyListMap$
      |136 -> class scala.collection.immutable.ListMap$Node
      |137 -> class scala.collection.immutable.Stream$Cons
      |138 -> class scala.collection.immutable.Stream$Empty$
      |139 -> class scala.runtime.VolatileByteRef
      |140 -> class scala.math.BigDecimal
      |141 -> class scala.collection.immutable.Queue$EmptyQueue$
      |142 -> class scala.collection.immutable.MapOps$ImmutableKeySet""".stripMargin.linesIterator.toStream

  val RecentEntries =
    """143 -> class scala.collection.immutable.Vector0$
      |144 -> class scala.collection.immutable.Vector1
      |145 -> class scala.collection.immutable.Vector2
      |146 -> class scala.collection.immutable.Vector3
      |147 -> class scala.collection.immutable.Vector4
      |148 -> class scala.collection.immutable.Vector5
      |149 -> class scala.collection.immutable.Vector6
      |150 -> class scala.collection.immutable.ArraySeq$ofByte
      |151 -> class scala.collection.immutable.ArraySeq$ofShort
      |152 -> class scala.collection.immutable.ArraySeq$ofInt
      |153 -> class scala.collection.immutable.ArraySeq$ofLong
      |154 -> class scala.collection.immutable.ArraySeq$ofFloat
      |155 -> class scala.collection.immutable.ArraySeq$ofDouble
      |156 -> class scala.collection.immutable.ArraySeq$ofBoolean
      |157 -> class scala.collection.immutable.ArraySeq$ofChar
      |158 -> class scala.collection.immutable.ArraySeq$ofRef""".stripMargin.linesIterator.toStream

  val CurrentEntries = Entries_0_9_5 #::: RecentEntries
}
