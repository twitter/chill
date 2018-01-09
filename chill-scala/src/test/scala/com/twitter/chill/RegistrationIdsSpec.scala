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
            |18 -> class scala.collection.immutable.Queue
            |19 -> class scala.collection.immutable.Nil$
            |20 -> class scala.collection.immutable.$colon$colon
            |21 -> class scala.collection.immutable.Vector
            |22 -> class scala.collection.immutable.Set$EmptySet$
            |23 -> class scala.collection.immutable.Set$Set1
            |24 -> class scala.collection.immutable.Set$Set2
            |25 -> class scala.collection.immutable.Set$Set3
            |26 -> class scala.collection.immutable.Set$Set4
            |27 -> class scala.collection.immutable.ListSet$EmptyListSet$
            |28 -> class scala.collection.immutable.ListSet$Node
            |29 -> class scala.collection.immutable.HashSet$EmptyHashSet$
            |30 -> class scala.collection.immutable.HashSet$HashSet1
            |31 -> class scala.collection.immutable.HashSet$HashTrieSet
            |32 -> class scala.collection.immutable.Map$Map1
            |33 -> class scala.collection.immutable.Map$Map2
            |34 -> class scala.collection.immutable.Map$Map3
            |35 -> class scala.collection.immutable.Map$Map4
            |36 -> class scala.collection.immutable.HashMap$HashTrieMap
            |37 -> class scala.collection.immutable.Range
            |38 -> class scala.collection.immutable.Range$Inclusive
            |39 -> class scala.collection.immutable.NumericRange$Inclusive
            |40 -> class scala.collection.immutable.NumericRange$Exclusive
            |41 -> class scala.collection.mutable.BitSet
            |42 -> class scala.collection.mutable.HashMap
            |43 -> class scala.collection.mutable.HashSet
            |44 -> class scala.collection.convert.Wrappers$IterableWrapper
            |45 -> class scala.Tuple1
            |46 -> class scala.Tuple2
            |47 -> class scala.Tuple3
            |48 -> class scala.Tuple4
            |49 -> class scala.Tuple5
            |50 -> class scala.Tuple6
            |51 -> class scala.Tuple7
            |52 -> class scala.Tuple8
            |53 -> class scala.Tuple9
            |54 -> class scala.Tuple10
            |55 -> class scala.Tuple11
            |56 -> class scala.Tuple12
            |57 -> class scala.Tuple13
            |58 -> class scala.Tuple14
            |59 -> class scala.Tuple15
            |60 -> class scala.Tuple16
            |61 -> class scala.Tuple17
            |62 -> class scala.Tuple18
            |63 -> class scala.Tuple19
            |64 -> class scala.Tuple20
            |65 -> class scala.Tuple21
            |66 -> class scala.Tuple22
            |67 -> class scala.Tuple1$mcJ$sp
            |68 -> class scala.Tuple1$mcI$sp
            |69 -> class scala.Tuple1$mcD$sp
            |70 -> class scala.Tuple2$mcJJ$sp
            |71 -> class scala.Tuple2$mcJI$sp
            |72 -> class scala.Tuple2$mcJD$sp
            |73 -> class scala.Tuple2$mcIJ$sp
            |74 -> class scala.Tuple2$mcII$sp
            |75 -> class scala.Tuple2$mcID$sp
            |76 -> class scala.Tuple2$mcDJ$sp
            |77 -> class scala.Tuple2$mcDI$sp
            |78 -> class scala.Tuple2$mcDD$sp
            |79 -> class scala.Symbol
            |80 -> interface scala.reflect.ClassTag
            |81 -> class scala.runtime.BoxedUnit
            |82 -> class java.util.Arrays$ArrayList
            |83 -> class java.util.BitSet
            |84 -> class java.util.PriorityQueue
            |85 -> class java.util.regex.Pattern
            |86 -> class java.sql.Date
            |87 -> class java.sql.Time
            |88 -> class java.sql.Timestamp
            |89 -> class java.net.URI
            |90 -> class java.net.InetSocketAddress
            |91 -> class java.util.UUID
            |92 -> class java.util.Locale
            |93 -> class java.text.SimpleDateFormat
            |94 -> class java.util.Collections$UnmodifiableCollection
            |95 -> class java.util.Collections$UnmodifiableRandomAccessList
            |96 -> class java.util.Collections$UnmodifiableList
            |97 -> class java.util.Collections$UnmodifiableMap
            |98 -> class java.util.Collections$UnmodifiableSet
            |99 -> class java.util.Collections$UnmodifiableSortedMap
            |100 -> class java.util.Collections$UnmodifiableSortedSet
            |101 -> class com.esotericsoftware.kryo.serializers.ClosureSerializer$Closure"""
            .stripMargin.lines.mkString("\n")
      }
    }
}
