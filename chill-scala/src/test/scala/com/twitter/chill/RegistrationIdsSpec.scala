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
            |15 -> class [B
            |16 -> class [S
            |17 -> class [I
            |18 -> class [J
            |19 -> class [F
            |20 -> class [D
            |21 -> class [Z
            |22 -> class [C
            |23 -> class [Ljava.lang.String;
            |24 -> class [Ljava.lang.Object;
            |25 -> class scala.None$
            |26 -> class scala.Some
            |27 -> class scala.util.Left
            |28 -> class scala.util.Right
            |29 -> class scala.collection.immutable.Queue
            |30 -> class scala.collection.immutable.Nil$
            |31 -> class scala.collection.immutable.$colon$colon
            |32 -> class scala.collection.immutable.Vector
            |33 -> class scala.collection.immutable.Set$EmptySet$
            |34 -> class scala.collection.immutable.Set$Set1
            |35 -> class scala.collection.immutable.Set$Set2
            |36 -> class scala.collection.immutable.Set$Set3
            |37 -> class scala.collection.immutable.Set$Set4
            |38 -> class scala.collection.immutable.ListSet$EmptyListSet$
            |39 -> class scala.collection.immutable.ListSet$Node
            |40 -> class scala.collection.immutable.HashSet$EmptyHashSet$
            |41 -> class scala.collection.immutable.HashSet$HashSet1
            |42 -> class scala.collection.immutable.HashSet$HashTrieSet
            |43 -> class scala.collection.immutable.Map$Map1
            |44 -> class scala.collection.immutable.Map$Map2
            |45 -> class scala.collection.immutable.Map$Map3
            |46 -> class scala.collection.immutable.Map$Map4
            |47 -> class scala.collection.immutable.HashMap$EmptyHashMap$
            |48 -> class scala.collection.immutable.HashMap$HashMap1
            |49 -> class scala.collection.immutable.HashMap$HashTrieMap
            |50 -> class scala.collection.immutable.ListMap$EmptyListMap$
            |51 -> class scala.collection.immutable.ListMap$Node
            |52 -> class scala.collection.immutable.Range
            |53 -> class scala.collection.immutable.Range$Inclusive
            |54 -> class scala.collection.immutable.NumericRange$Inclusive
            |55 -> class scala.collection.immutable.NumericRange$Exclusive
            |56 -> class scala.collection.mutable.BitSet
            |57 -> class scala.collection.mutable.HashMap
            |58 -> class scala.collection.mutable.HashSet
            |59 -> class scala.collection.convert.Wrappers$IterableWrapper
            |60 -> class scala.Tuple1
            |61 -> class scala.Tuple2
            |62 -> class scala.Tuple3
            |63 -> class scala.Tuple4
            |64 -> class scala.Tuple5
            |65 -> class scala.Tuple6
            |66 -> class scala.Tuple7
            |67 -> class scala.Tuple8
            |68 -> class scala.Tuple9
            |69 -> class scala.Tuple10
            |70 -> class scala.Tuple11
            |71 -> class scala.Tuple12
            |72 -> class scala.Tuple13
            |73 -> class scala.Tuple14
            |74 -> class scala.Tuple15
            |75 -> class scala.Tuple16
            |76 -> class scala.Tuple17
            |77 -> class scala.Tuple18
            |78 -> class scala.Tuple19
            |79 -> class scala.Tuple20
            |80 -> class scala.Tuple21
            |81 -> class scala.Tuple22
            |82 -> class scala.Tuple1$mcJ$sp
            |83 -> class scala.Tuple1$mcI$sp
            |84 -> class scala.Tuple1$mcD$sp
            |85 -> class scala.Tuple2$mcJJ$sp
            |86 -> class scala.Tuple2$mcJI$sp
            |87 -> class scala.Tuple2$mcJD$sp
            |88 -> class scala.Tuple2$mcIJ$sp
            |89 -> class scala.Tuple2$mcII$sp
            |90 -> class scala.Tuple2$mcID$sp
            |91 -> class scala.Tuple2$mcDJ$sp
            |92 -> class scala.Tuple2$mcDI$sp
            |93 -> class scala.Tuple2$mcDD$sp
            |94 -> class scala.Symbol
            |95 -> interface scala.reflect.ClassTag
            |96 -> class scala.runtime.BoxedUnit
            |97 -> class java.util.Arrays$ArrayList
            |98 -> class java.util.BitSet
            |99 -> class java.util.PriorityQueue
            |100 -> class java.util.regex.Pattern
            |101 -> class java.sql.Date
            |102 -> class java.sql.Time
            |103 -> class java.sql.Timestamp
            |104 -> class java.net.URI
            |105 -> class java.net.InetSocketAddress
            |106 -> class java.util.UUID
            |107 -> class java.util.Locale
            |108 -> class java.text.SimpleDateFormat
            |109 -> class java.util.Collections$UnmodifiableCollection
            |110 -> class java.util.Collections$UnmodifiableRandomAccessList
            |111 -> class java.util.Collections$UnmodifiableList
            |112 -> class java.util.Collections$UnmodifiableMap
            |113 -> class java.util.Collections$UnmodifiableSet
            |114 -> class java.util.Collections$UnmodifiableSortedMap
            |115 -> class java.util.Collections$UnmodifiableSortedSet
            |116 -> class com.esotericsoftware.kryo.serializers.ClosureSerializer$Closure"""
            .stripMargin.lines.mkString("\n")
      }
    }
}
