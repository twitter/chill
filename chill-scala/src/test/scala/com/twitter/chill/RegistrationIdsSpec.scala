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
        if (registeredEntries != expectedEntries) println(s"\n\n\n$registeredEntries\n\n\n") // FIXME remove this line
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
            |24 -> class scala.collection.immutable.HashSet$EmptyHashSet$
            |25 -> class scala.collection.immutable.HashSet$HashSet1
            |26 -> class scala.collection.immutable.HashSet$HashTrieSet
            |27 -> class scala.collection.immutable.Map$Map1
            |28 -> class scala.collection.immutable.Map$Map2
            |29 -> class scala.collection.immutable.Map$Map3
            |30 -> class scala.collection.immutable.Map$Map4
            |31 -> class scala.collection.immutable.HashMap$HashTrieMap
            |32 -> class scala.collection.immutable.Range$Inclusive
            |33 -> class scala.collection.immutable.NumericRange$Inclusive
            |34 -> class scala.collection.immutable.NumericRange$Exclusive
            |35 -> class scala.collection.mutable.BitSet
            |36 -> class scala.collection.mutable.HashMap
            |37 -> class scala.collection.mutable.HashSet
            |38 -> class scala.collection.convert.Wrappers$IterableWrapper
            |39 -> class scala.Tuple1
            |40 -> class scala.Tuple2
            |41 -> class scala.Tuple3
            |42 -> class scala.Tuple4
            |43 -> class scala.Tuple5
            |44 -> class scala.Tuple6
            |45 -> class scala.Tuple7
            |46 -> class scala.Tuple8
            |47 -> class scala.Tuple9
            |48 -> class scala.Tuple10
            |49 -> class scala.Tuple11
            |50 -> class scala.Tuple12
            |51 -> class scala.Tuple13
            |52 -> class scala.Tuple14
            |53 -> class scala.Tuple15
            |54 -> class scala.Tuple16
            |55 -> class scala.Tuple17
            |56 -> class scala.Tuple18
            |57 -> class scala.Tuple19
            |58 -> class scala.Tuple20
            |59 -> class scala.Tuple21
            |60 -> class scala.Tuple22
            |61 -> class scala.Tuple1$mcJ$sp
            |62 -> class scala.Tuple1$mcI$sp
            |63 -> class scala.Tuple1$mcD$sp
            |64 -> class scala.Tuple2$mcJJ$sp
            |65 -> class scala.Tuple2$mcJI$sp
            |66 -> class scala.Tuple2$mcJD$sp
            |67 -> class scala.Tuple2$mcIJ$sp
            |68 -> class scala.Tuple2$mcII$sp
            |69 -> class scala.Tuple2$mcID$sp
            |70 -> class scala.Tuple2$mcDJ$sp
            |71 -> class scala.Tuple2$mcDI$sp
            |72 -> class scala.Tuple2$mcDD$sp
            |73 -> class scala.Symbol
            |74 -> interface scala.reflect.ClassTag
            |75 -> class scala.runtime.BoxedUnit
            |76 -> class java.util.Arrays$ArrayList
            |77 -> class java.util.BitSet
            |78 -> class java.util.PriorityQueue
            |79 -> class java.util.regex.Pattern
            |80 -> class java.sql.Date
            |81 -> class java.sql.Time
            |82 -> class java.sql.Timestamp
            |83 -> class java.net.URI
            |84 -> class java.net.InetSocketAddress
            |85 -> class java.util.UUID
            |86 -> class java.util.Locale
            |87 -> class java.text.SimpleDateFormat
            |88 -> class java.util.Collections$UnmodifiableCollection
            |89 -> class java.util.Collections$UnmodifiableRandomAccessList
            |90 -> class java.util.Collections$UnmodifiableList
            |91 -> class java.util.Collections$UnmodifiableMap
            |92 -> class java.util.Collections$UnmodifiableSet
            |93 -> class java.util.Collections$UnmodifiableSortedMap
            |94 -> class java.util.Collections$UnmodifiableSortedSet
            |95 -> class com.esotericsoftware.kryo.serializers.ClosureSerializer$Closure"""
            .stripMargin.lines.mkString("\n")
      }
    }
}
