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
        if (registeredEntries != expectedEntries)
          println(
            s"""\n\n
               |This test ($getClass) will fail, most probably
               |because the order of registration IDs has changed or a registration was added
               |or removed. If that was intended, here is the new list of entries that can be
               |set as expected value in the test:
               |
               |$registeredEntries\n\n\n""".stripMargin)
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
            |25 -> class scala.collection.mutable.WrappedArray$ofByte
            |26 -> class scala.collection.mutable.WrappedArray$ofShort
            |27 -> class scala.collection.mutable.WrappedArray$ofInt
            |28 -> class scala.collection.mutable.WrappedArray$ofLong
            |29 -> class scala.collection.mutable.WrappedArray$ofFloat
            |30 -> class scala.collection.mutable.WrappedArray$ofDouble
            |31 -> class scala.collection.mutable.WrappedArray$ofBoolean
            |32 -> class scala.collection.mutable.WrappedArray$ofChar
            |33 -> class scala.collection.mutable.WrappedArray$ofRef
            |34 -> class scala.None$
            |35 -> class scala.Some
            |36 -> class scala.util.Left
            |37 -> class scala.util.Right
            |38 -> class scala.collection.immutable.Queue
            |39 -> class scala.collection.immutable.Nil$
            |40 -> class scala.collection.immutable.$colon$colon
            |41 -> class scala.collection.immutable.Vector
            |42 -> class scala.collection.immutable.Set$EmptySet$
            |43 -> class scala.collection.immutable.Set$Set1
            |44 -> class scala.collection.immutable.Set$Set2
            |45 -> class scala.collection.immutable.Set$Set3
            |46 -> class scala.collection.immutable.Set$Set4
            |47 -> class scala.collection.immutable.ListSet$EmptyListSet$
            |48 -> class scala.collection.immutable.ListSet$Node
            |49 -> class scala.collection.immutable.HashSet$EmptyHashSet$
            |50 -> class scala.collection.immutable.HashSet$HashSet1
            |51 -> class scala.collection.immutable.HashSet$HashTrieSet
            |52 -> class scala.collection.immutable.Map$Map1
            |53 -> class scala.collection.immutable.Map$Map2
            |54 -> class scala.collection.immutable.Map$Map3
            |55 -> class scala.collection.immutable.Map$Map4
            |56 -> class scala.collection.immutable.HashMap$EmptyHashMap$
            |57 -> class scala.collection.immutable.HashMap$HashMap1
            |58 -> class scala.collection.immutable.HashMap$HashTrieMap
            |59 -> class scala.collection.immutable.ListMap$EmptyListMap$
            |60 -> class scala.collection.immutable.ListMap$Node
            |61 -> class scala.collection.immutable.Range
            |62 -> class scala.collection.immutable.Range$Inclusive
            |63 -> class scala.collection.immutable.NumericRange$Inclusive
            |64 -> class scala.collection.immutable.NumericRange$Exclusive
            |65 -> class scala.collection.mutable.BitSet
            |66 -> class scala.collection.mutable.HashMap
            |67 -> class scala.collection.mutable.HashSet
            |68 -> class scala.collection.immutable.Stream$Cons
            |69 -> class scala.collection.immutable.Stream$Empty$
            |70 -> class scala.collection.convert.Wrappers$IterableWrapper
            |71 -> class scala.Tuple1
            |72 -> class scala.Tuple2
            |73 -> class scala.Tuple3
            |74 -> class scala.Tuple4
            |75 -> class scala.Tuple5
            |76 -> class scala.Tuple6
            |77 -> class scala.Tuple7
            |78 -> class scala.Tuple8
            |79 -> class scala.Tuple9
            |80 -> class scala.Tuple10
            |81 -> class scala.Tuple11
            |82 -> class scala.Tuple12
            |83 -> class scala.Tuple13
            |84 -> class scala.Tuple14
            |85 -> class scala.Tuple15
            |86 -> class scala.Tuple16
            |87 -> class scala.Tuple17
            |88 -> class scala.Tuple18
            |89 -> class scala.Tuple19
            |90 -> class scala.Tuple20
            |91 -> class scala.Tuple21
            |92 -> class scala.Tuple22
            |93 -> class scala.Tuple1$mcJ$sp
            |94 -> class scala.Tuple1$mcI$sp
            |95 -> class scala.Tuple1$mcD$sp
            |96 -> class scala.Tuple2$mcJJ$sp
            |97 -> class scala.Tuple2$mcJI$sp
            |98 -> class scala.Tuple2$mcJD$sp
            |99 -> class scala.Tuple2$mcIJ$sp
            |100 -> class scala.Tuple2$mcII$sp
            |101 -> class scala.Tuple2$mcID$sp
            |102 -> class scala.Tuple2$mcDJ$sp
            |103 -> class scala.Tuple2$mcDI$sp
            |104 -> class scala.Tuple2$mcDD$sp
            |105 -> class scala.Symbol
            |106 -> interface scala.reflect.ClassTag
            |107 -> class scala.runtime.BoxedUnit
            |108 -> class java.util.Arrays$ArrayList
            |109 -> class java.util.BitSet
            |110 -> class java.util.PriorityQueue
            |111 -> class java.util.regex.Pattern
            |112 -> class java.sql.Date
            |113 -> class java.sql.Time
            |114 -> class java.sql.Timestamp
            |115 -> class java.net.URI
            |116 -> class java.net.InetSocketAddress
            |117 -> class java.util.UUID
            |118 -> class java.util.Locale
            |119 -> class java.text.SimpleDateFormat
            |120 -> class java.util.Collections$UnmodifiableCollection
            |121 -> class java.util.Collections$UnmodifiableRandomAccessList
            |122 -> class java.util.Collections$UnmodifiableList
            |123 -> class java.util.Collections$UnmodifiableMap
            |124 -> class java.util.Collections$UnmodifiableSet
            |125 -> class java.util.Collections$UnmodifiableSortedMap
            |126 -> class java.util.Collections$UnmodifiableSortedSet
            |127 -> class com.esotericsoftware.kryo.serializers.ClosureSerializer$Closure
            |128 -> class scala.collection.immutable.WrappedString"""
            .stripMargin.lines.mkString("\n")
      }
    }
}
