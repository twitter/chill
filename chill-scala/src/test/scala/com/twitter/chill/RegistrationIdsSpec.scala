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
            |24 -> class scala.collection.immutable.ListSet$EmptyListSet$
            |25 -> class scala.collection.immutable.ListSet$Node
            |26 -> class scala.collection.immutable.HashSet$EmptyHashSet$
            |27 -> class scala.collection.immutable.HashSet$HashSet1
            |28 -> class scala.collection.immutable.HashSet$HashTrieSet
            |29 -> class scala.collection.immutable.Map$Map1
            |30 -> class scala.collection.immutable.Map$Map2
            |31 -> class scala.collection.immutable.Map$Map3
            |32 -> class scala.collection.immutable.Map$Map4
            |33 -> class scala.collection.immutable.HashMap$HashTrieMap
            |34 -> class scala.collection.immutable.Range$Inclusive
            |35 -> class scala.collection.immutable.NumericRange$Inclusive
            |36 -> class scala.collection.immutable.NumericRange$Exclusive
            |37 -> class scala.collection.mutable.BitSet
            |38 -> class scala.collection.mutable.HashMap
            |39 -> class scala.collection.mutable.HashSet
            |40 -> class scala.collection.convert.Wrappers$IterableWrapper
            |41 -> class scala.Tuple1
            |42 -> class scala.Tuple2
            |43 -> class scala.Tuple3
            |44 -> class scala.Tuple4
            |45 -> class scala.Tuple5
            |46 -> class scala.Tuple6
            |47 -> class scala.Tuple7
            |48 -> class scala.Tuple8
            |49 -> class scala.Tuple9
            |50 -> class scala.Tuple10
            |51 -> class scala.Tuple11
            |52 -> class scala.Tuple12
            |53 -> class scala.Tuple13
            |54 -> class scala.Tuple14
            |55 -> class scala.Tuple15
            |56 -> class scala.Tuple16
            |57 -> class scala.Tuple17
            |58 -> class scala.Tuple18
            |59 -> class scala.Tuple19
            |60 -> class scala.Tuple20
            |61 -> class scala.Tuple21
            |62 -> class scala.Tuple22
            |63 -> class scala.Tuple1$mcJ$sp
            |64 -> class scala.Tuple1$mcI$sp
            |65 -> class scala.Tuple1$mcD$sp
            |66 -> class scala.Tuple2$mcJJ$sp
            |67 -> class scala.Tuple2$mcJI$sp
            |68 -> class scala.Tuple2$mcJD$sp
            |69 -> class scala.Tuple2$mcIJ$sp
            |70 -> class scala.Tuple2$mcII$sp
            |71 -> class scala.Tuple2$mcID$sp
            |72 -> class scala.Tuple2$mcDJ$sp
            |73 -> class scala.Tuple2$mcDI$sp
            |74 -> class scala.Tuple2$mcDD$sp
            |75 -> class scala.Symbol
            |76 -> interface scala.reflect.ClassTag
            |77 -> class scala.runtime.BoxedUnit
            |78 -> class java.util.Arrays$ArrayList
            |79 -> class java.util.BitSet
            |80 -> class java.util.PriorityQueue
            |81 -> class java.util.regex.Pattern
            |82 -> class java.sql.Date
            |83 -> class java.sql.Time
            |84 -> class java.sql.Timestamp
            |85 -> class java.net.URI
            |86 -> class java.net.InetSocketAddress
            |87 -> class java.util.UUID
            |88 -> class java.util.Locale
            |89 -> class java.text.SimpleDateFormat
            |90 -> class java.util.Collections$UnmodifiableCollection
            |91 -> class java.util.Collections$UnmodifiableRandomAccessList
            |92 -> class java.util.Collections$UnmodifiableList
            |93 -> class java.util.Collections$UnmodifiableMap
            |94 -> class java.util.Collections$UnmodifiableSet
            |95 -> class java.util.Collections$UnmodifiableSortedMap
            |96 -> class java.util.Collections$UnmodifiableSortedSet
            |97 -> class com.esotericsoftware.kryo.serializers.ClosureSerializer$Closure"""
            .stripMargin.lines.mkString("\n")
      }
    }
}
