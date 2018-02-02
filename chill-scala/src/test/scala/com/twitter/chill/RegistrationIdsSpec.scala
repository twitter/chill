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
    |For the ScalaKryoInstantiators, the registered classes""".stripMargin
    .should {
      val compatibility = classOf[AllScalaRegistrar_0_9_2].getSimpleName
      (s"be as expected for the backward compatibility layer $compatibility,\n" +
        "  i.e. contain the list of registrations defined in this test.").in {
          val k = new KryoBase
          new AllScalaRegistrar_0_9_2().apply(k)
          if (registeredEntries(k) != expectedEntries_0_9_2) printMessageFor(k, compatibility)
          assert(registeredEntries(k) == expectedEntries_0_9_2)
        }

      val current = classOf[AllScalaRegistrar].getSimpleName
      (s"be as expected for the current $current,\n" +
        "  i.e. contain the list of registrations defined in this test.").in {
          val k = new KryoBase
          new AllScalaRegistrar().apply(k)
          if (registeredEntries(k) != expectedEntries_current) printMessageFor(k, current)
          assert(registeredEntries(k) == expectedEntries_current)
        }
    }

  private def registeredEntries(k: KryoBase) =
    Stream.from(0)
      .map(k.getRegistration)
      .takeWhile(_ != null)
      .map(r => s"${r.getId} -> ${r.getType}")
      .mkString("\n")

  private def printMessageFor(k: KryoBase, scope: String): Unit =
    System.err.println(
      s"""\n\n
         |This test ($getClass) will fail for $scope, most probably because
         |the order of registration IDs has changed or a registration was
         |added or removed. If that was intended, here is the new list of
         |entries that can be set as expected value in the test:
         |
         |${registeredEntries(k)}\n\n\n""".stripMargin)

  private def expectedEntries_0_9_2 =
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
      |19 -> class scala.collection.immutable.Set$Set1
      |20 -> class scala.collection.immutable.Set$Set2
      |21 -> class scala.collection.immutable.Set$Set3
      |22 -> class scala.collection.immutable.Set$Set4
      |23 -> class scala.collection.immutable.HashSet$HashTrieSet
      |24 -> class scala.collection.immutable.Map$Map1
      |25 -> class scala.collection.immutable.Map$Map2
      |26 -> class scala.collection.immutable.Map$Map3
      |27 -> class scala.collection.immutable.Map$Map4
      |28 -> class scala.collection.immutable.HashMap$HashTrieMap
      |29 -> class scala.collection.immutable.Range$Inclusive
      |30 -> class scala.collection.immutable.NumericRange$Inclusive
      |31 -> class scala.collection.immutable.NumericRange$Exclusive
      |32 -> class scala.collection.mutable.BitSet
      |33 -> class scala.collection.mutable.HashMap
      |34 -> class scala.collection.mutable.HashSet
      |35 -> class scala.collection.convert.Wrappers$IterableWrapper
      |36 -> class scala.Tuple1
      |37 -> class scala.Tuple2
      |38 -> class scala.Tuple3
      |39 -> class scala.Tuple4
      |40 -> class scala.Tuple5
      |41 -> class scala.Tuple6
      |42 -> class scala.Tuple7
      |43 -> class scala.Tuple8
      |44 -> class scala.Tuple9
      |45 -> class scala.Tuple10
      |46 -> class scala.Tuple11
      |47 -> class scala.Tuple12
      |48 -> class scala.Tuple13
      |49 -> class scala.Tuple14
      |50 -> class scala.Tuple15
      |51 -> class scala.Tuple16
      |52 -> class scala.Tuple17
      |53 -> class scala.Tuple18
      |54 -> class scala.Tuple19
      |55 -> class scala.Tuple20
      |56 -> class scala.Tuple21
      |57 -> class scala.Tuple22
      |58 -> class scala.Tuple1$mcJ$sp
      |59 -> class scala.Tuple1$mcI$sp
      |60 -> class scala.Tuple1$mcD$sp
      |61 -> class scala.Tuple2$mcJJ$sp
      |62 -> class scala.Tuple2$mcJI$sp
      |63 -> class scala.Tuple2$mcJD$sp
      |64 -> class scala.Tuple2$mcIJ$sp
      |65 -> class scala.Tuple2$mcII$sp
      |66 -> class scala.Tuple2$mcID$sp
      |67 -> class scala.Tuple2$mcDJ$sp
      |68 -> class scala.Tuple2$mcDI$sp
      |69 -> class scala.Tuple2$mcDD$sp
      |70 -> class scala.Symbol
      |71 -> interface scala.reflect.ClassTag
      |72 -> class scala.runtime.BoxedUnit
      |73 -> class java.util.Arrays$ArrayList
      |74 -> class java.util.BitSet
      |75 -> class java.util.PriorityQueue
      |76 -> class java.util.regex.Pattern
      |77 -> class java.sql.Date
      |78 -> class java.sql.Time
      |79 -> class java.sql.Timestamp
      |80 -> class java.net.URI
      |81 -> class java.net.InetSocketAddress
      |82 -> class java.util.UUID
      |83 -> class java.util.Locale
      |84 -> class java.text.SimpleDateFormat
      |85 -> class java.util.Collections$UnmodifiableCollection
      |86 -> class java.util.Collections$UnmodifiableRandomAccessList
      |87 -> class java.util.Collections$UnmodifiableList
      |88 -> class java.util.Collections$UnmodifiableMap
      |89 -> class java.util.Collections$UnmodifiableSet
      |90 -> class java.util.Collections$UnmodifiableSortedMap
      |91 -> class java.util.Collections$UnmodifiableSortedSet
      |92 -> class com.esotericsoftware.kryo.serializers.ClosureSerializer$Closure"""
      .stripMargin.lines.mkString("\n")

  private def expectedEntries_recent =
    """"""
      .stripMargin.lines.mkString("\n")

  private def expectedEntries_current =
    (expectedEntries_0_9_2.lines ++ expectedEntries_recent.lines).mkString("\n")
}
