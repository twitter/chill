package com.twitter.chill

import org.scalatest.{ Matchers, WordSpec }

class RegistrationIdsSpec extends WordSpec with Matchers {
  """
    |Projects using chill to long term persist serialized data (for example in event
    |sourcing scenarios) depend on the registration IDs of the pre-registered
    |classes being stable. Therefore, it is important that updates to chill avoid
    |changing registration IDs of the pre-registered classes as far as possible.
    |When changing registration IDs becomes necessary, details of the changes should
    |be mentioned in the release notes.
    |
    |For the ScalaKryoInstantiator, the registered classes""".stripMargin
    .should {
      "be as expected, i.e. contain the list of registrations defined in this test.".in {
        val k = new ScalaKryoInstantiator().newKryo
        if (registeredEntries != expectedEntries) println(s"\n\n\n$registeredEntries\n\n\n")
        assert(registeredEntries == expectedEntries)
        def registeredEntries =
          Stream.from(0)
            .map(k.getRegistration)
            .takeWhile(_ != null)
            .map(r => s"${r.getId} -> ${r.getType}")
            .mkString("\n")
        def expectedEntries =
          """0 -> int
            |1 -> class java.lang.String
            |2 -> float
            |3 -> boolean
            |4 -> byte
            |5 -> char
            |6 -> short
            |7 -> long
            |8 -> double
            |9 -> void
            |10 -> class scala.collection.convert.Wrappers$SeqWrapper
            |11 -> class scala.collection.convert.Wrappers$IteratorWrapper
            |12 -> class scala.collection.convert.Wrappers$MapWrapper
            |13 -> class scala.collection.convert.Wrappers$JListWrapper
            |14 -> class scala.collection.convert.Wrappers$JMapWrapper
            |15 -> class scala.Some
            |16 -> class scala.util.Left
            |17 -> class scala.util.Right
            |18 -> class scala.collection.immutable.Vector
            |19 -> class scala.collection.immutable.Set$EmptySet$
            |20 -> class scala.collection.immutable.Set$Set1
            |21 -> class scala.collection.immutable.Set$Set2
            |22 -> class scala.collection.immutable.Set$Set3
            |23 -> class scala.collection.immutable.Set$Set4
            |24 -> class scala.collection.immutable.HashSet$HashTrieSet
            |25 -> class scala.collection.immutable.Map$Map1
            |26 -> class scala.collection.immutable.Map$Map2
            |27 -> class scala.collection.immutable.Map$Map3
            |28 -> class scala.collection.immutable.Map$Map4
            |29 -> class scala.collection.immutable.HashMap$HashTrieMap
            |30 -> class scala.collection.immutable.Range$Inclusive
            |31 -> class scala.collection.immutable.NumericRange$Inclusive
            |32 -> class scala.collection.immutable.NumericRange$Exclusive
            |33 -> class scala.collection.mutable.BitSet
            |34 -> class scala.collection.mutable.HashMap
            |35 -> class scala.collection.mutable.HashSet
            |36 -> class scala.collection.convert.Wrappers$IterableWrapper
            |37 -> class scala.Tuple1
            |38 -> class scala.Tuple2
            |39 -> class scala.Tuple3
            |40 -> class scala.Tuple4
            |41 -> class scala.Tuple5
            |42 -> class scala.Tuple6
            |43 -> class scala.Tuple7
            |44 -> class scala.Tuple8
            |45 -> class scala.Tuple9
            |46 -> class scala.Tuple10
            |47 -> class scala.Tuple11
            |48 -> class scala.Tuple12
            |49 -> class scala.Tuple13
            |50 -> class scala.Tuple14
            |51 -> class scala.Tuple15
            |52 -> class scala.Tuple16
            |53 -> class scala.Tuple17
            |54 -> class scala.Tuple18
            |55 -> class scala.Tuple19
            |56 -> class scala.Tuple20
            |57 -> class scala.Tuple21
            |58 -> class scala.Tuple22
            |59 -> class scala.Tuple1$mcJ$sp
            |60 -> class scala.Tuple1$mcI$sp
            |61 -> class scala.Tuple1$mcD$sp
            |62 -> class scala.Tuple2$mcJJ$sp
            |63 -> class scala.Tuple2$mcJI$sp
            |64 -> class scala.Tuple2$mcJD$sp
            |65 -> class scala.Tuple2$mcIJ$sp
            |66 -> class scala.Tuple2$mcII$sp
            |67 -> class scala.Tuple2$mcID$sp
            |68 -> class scala.Tuple2$mcDJ$sp
            |69 -> class scala.Tuple2$mcDI$sp
            |70 -> class scala.Tuple2$mcDD$sp
            |71 -> class scala.Symbol
            |72 -> interface scala.reflect.ClassTag
            |73 -> class scala.runtime.BoxedUnit
            |74 -> class java.util.Arrays$ArrayList
            |75 -> class java.util.BitSet
            |76 -> class java.util.PriorityQueue
            |77 -> class java.util.regex.Pattern
            |78 -> class java.sql.Date
            |79 -> class java.sql.Time
            |80 -> class java.sql.Timestamp
            |81 -> class java.net.URI
            |82 -> class java.net.InetSocketAddress
            |83 -> class java.util.UUID
            |84 -> class java.util.Locale
            |85 -> class java.text.SimpleDateFormat
            |86 -> class java.util.Collections$UnmodifiableCollection
            |87 -> class java.util.Collections$UnmodifiableRandomAccessList
            |88 -> class java.util.Collections$UnmodifiableList
            |89 -> class java.util.Collections$UnmodifiableMap
            |90 -> class java.util.Collections$UnmodifiableSet
            |91 -> class java.util.Collections$UnmodifiableSortedMap
            |92 -> class java.util.Collections$UnmodifiableSortedSet
            |93 -> class com.esotericsoftware.kryo.serializers.ClosureSerializer$Closure"""
            .stripMargin.lines.mkString("\n")
      }
    }
}
