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
      |9 -> class scala.collection.convert.Wrappers$SeqWrapper
      |10 -> class scala.collection.convert.Wrappers$IteratorWrapper
      |11 -> class scala.collection.convert.Wrappers$MapWrapper
      |12 -> class scala.collection.convert.Wrappers$JListWrapper
      |13 -> class scala.collection.convert.Wrappers$JMapWrapper
      |14 -> class scala.Some
      |15 -> class scala.util.Left
      |16 -> class scala.util.Right
      |17 -> class scala.collection.immutable.Vector
      |18 -> class scala.collection.immutable.Set$Set1
      |19 -> class scala.collection.immutable.Set$Set2
      |20 -> class scala.collection.immutable.Set$Set3
      |21 -> class scala.collection.immutable.Set$Set4
      |22 -> class scala.collection.immutable.HashSet$HashTrieSet
      |23 -> class scala.collection.immutable.Map$Map1
      |24 -> class scala.collection.immutable.Map$Map2
      |25 -> class scala.collection.immutable.Map$Map3
      |26 -> class scala.collection.immutable.Map$Map4
      |27 -> class scala.collection.immutable.HashMap$HashTrieMap
      |28 -> class scala.collection.immutable.Range$Inclusive
      |29 -> class scala.collection.immutable.NumericRange$Inclusive
      |30 -> class scala.collection.immutable.NumericRange$Exclusive
      |31 -> class scala.collection.mutable.BitSet
      |32 -> class scala.collection.mutable.HashMap
      |33 -> class scala.collection.mutable.HashSet
      |34 -> class scala.collection.convert.Wrappers$IterableWrapper
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
      |91 -> class com.esotericsoftware.kryo.kryo5.serializers.ClosureSerializer$Closure""".stripMargin.linesIterator.toStream

  val RecentEntries = Stream.empty

  val CurrentEntries = Entries_0_9_5 #::: RecentEntries
}
