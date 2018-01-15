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
            |70 -> class scala.collection.immutable.TreeSet
            |71 -> class scala.collection.immutable.TreeMap
            |72 -> class scala.math.Ordering$Byte$
            |73 -> class scala.math.Ordering$Short$
            |74 -> class scala.math.Ordering$Int$
            |75 -> class scala.math.Ordering$Long$
            |76 -> class scala.math.Ordering$Float$
            |77 -> class scala.math.Ordering$Double$
            |78 -> class scala.math.Ordering$Boolean$
            |79 -> class scala.math.Ordering$Char$
            |80 -> class scala.math.Ordering$String$
            |81 -> class scala.collection.convert.Wrappers$IterableWrapper
            |82 -> class scala.Tuple1
            |83 -> class scala.Tuple2
            |84 -> class scala.Tuple3
            |85 -> class scala.Tuple4
            |86 -> class scala.Tuple5
            |87 -> class scala.Tuple6
            |88 -> class scala.Tuple7
            |89 -> class scala.Tuple8
            |90 -> class scala.Tuple9
            |91 -> class scala.Tuple10
            |92 -> class scala.Tuple11
            |93 -> class scala.Tuple12
            |94 -> class scala.Tuple13
            |95 -> class scala.Tuple14
            |96 -> class scala.Tuple15
            |97 -> class scala.Tuple16
            |98 -> class scala.Tuple17
            |99 -> class scala.Tuple18
            |100 -> class scala.Tuple19
            |101 -> class scala.Tuple20
            |102 -> class scala.Tuple21
            |103 -> class scala.Tuple22
            |104 -> class scala.Tuple1$mcJ$sp
            |105 -> class scala.Tuple1$mcI$sp
            |106 -> class scala.Tuple1$mcD$sp
            |107 -> class scala.Tuple2$mcJJ$sp
            |108 -> class scala.Tuple2$mcJI$sp
            |109 -> class scala.Tuple2$mcJD$sp
            |110 -> class scala.Tuple2$mcIJ$sp
            |111 -> class scala.Tuple2$mcII$sp
            |112 -> class scala.Tuple2$mcID$sp
            |113 -> class scala.Tuple2$mcDJ$sp
            |114 -> class scala.Tuple2$mcDI$sp
            |115 -> class scala.Tuple2$mcDD$sp
            |116 -> class scala.Symbol
            |117 -> interface scala.reflect.ClassTag
            |118 -> class scala.runtime.BoxedUnit
            |119 -> class java.util.Arrays$ArrayList
            |120 -> class java.util.BitSet
            |121 -> class java.util.PriorityQueue
            |122 -> class java.util.regex.Pattern
            |123 -> class java.sql.Date
            |124 -> class java.sql.Time
            |125 -> class java.sql.Timestamp
            |126 -> class java.net.URI
            |127 -> class java.net.InetSocketAddress
            |128 -> class java.util.UUID
            |129 -> class java.util.Locale
            |130 -> class java.text.SimpleDateFormat
            |131 -> class java.util.Collections$UnmodifiableCollection
            |132 -> class java.util.Collections$UnmodifiableRandomAccessList
            |133 -> class java.util.Collections$UnmodifiableList
            |134 -> class java.util.Collections$UnmodifiableMap
            |135 -> class java.util.Collections$UnmodifiableSet
            |136 -> class java.util.Collections$UnmodifiableSortedMap
            |137 -> class java.util.Collections$UnmodifiableSortedSet
            |138 -> class com.esotericsoftware.kryo.serializers.ClosureSerializer$Closure
            |139 -> class scala.collection.immutable.WrappedString"""
            .stripMargin.lines.mkString("\n")
      }
    }
}
