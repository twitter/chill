package com.twitter.chill

import scala.collection.immutable.{ HashMap, HashSet, ListMap, ListSet, Queue }

import org.scalatest.{ Matchers, WordSpec }

class StandardDataRegistrationsSpec extends WordSpec with Matchers {
  """
    |For projects using chill to persist serialized data (for example in event
    |sourcing scenarios), it can be beneficial or even required to turn on the
    |Kryo.setRegistrationRequired setting. For such projects, chill should provide
    |registrations for the most common data structures that are likely to be
    |persisted.
    |
    |Note that for sorted sets and maps, only the natural orderings for Byte, Short,
    |Int, Long, Float, Double, Boolean, Char, and String are registered (and not for
    |example the reverse orderings).
    |
    |The ScalaKryoInstantiator with setRegistrationRequired(true)""".stripMargin
    .should {
      def registrationRequiredInstantiator = new ScalaKryoInstantiator() {
        override def newKryo: KryoBase = {
          val k = super.newKryo
          k.setRegistrationRequired(true)
          k
        }
      }
      val kryo = KryoPool.withByteArrayOutputStream(4, registrationRequiredInstantiator)
      def roundtrip(original: AnyRef): Unit = {
        try {
          val serde = kryo.fromBytes(kryo.toBytesWithClass(original))
          (original, serde) match {
            case (originalArray: Array[_], serdeArray: Array[_]) =>
              assert(originalArray.toSeq == serdeArray.toSeq)
            case _ =>
              assert(serde == original)
          }
        } catch {
          case e: Throwable =>
            val message = s"exception during serialization round trip for $original of class ${original.getClass}:\n" +
              e.toString.lines.next
            assert(false, message)
        }
      }
      "serialize the empty hash map" in { roundtrip(HashMap()) }
      "serialize the one-element hash map" in { roundtrip(HashMap(1 -> 2)) }
      // TODO larger hash maps
      "serialize the empty list map" in { roundtrip(ListMap()) }
      "serialize the one-element list map" in { roundtrip(ListMap(1 -> 2)) }
      // TODO larger list maps
      "serialize the empty set" in { roundtrip(Set()) }
      "serialize the empty hash set" in { roundtrip(HashSet()) }
      "serialize the one-element hash set" in { roundtrip(HashSet(1)) }
      "serialize the empty list set" in { roundtrip(ListSet()) }
      "serialize the one-element list set" in { roundtrip(ListSet(1)) }
      // TODO TreeSet, and more examples like Set(1)
      "serialize the empty list" in { roundtrip(Nil) }
      "serialize the one-element list" in { roundtrip(List(1)) }
      // TODO more examples like List.empty[Int], 1 :: Nil, List(1, 2), List(1, 2, 3, 4)
      "serialize the empty queue" in { roundtrip(Queue()) }
      // TODO more examples like Queue(1)
      "serialize a range" in { roundtrip(Range(2, 10, 3)) }
      // TODO more examples like Vector(), Vector(1)
      "serialize the empty option" in { roundtrip(None) }
      // TODO more examples like Some(1), Left(2), Right(3), Option.empty, Option(5)
      "serialize the empty array" in { roundtrip(Array()) }
      "serialize Int arrays" in { roundtrip(Array.empty[Int]) }
      "serialize Byte arrays" in { roundtrip(Array.empty[Byte]) }
      "serialize Short arrays" in { roundtrip(Array.empty[Short]) }
      "serialize Long arrays" in { roundtrip(Array.empty[Long]) }
      "serialize Float arrays" in { roundtrip(Array.empty[Float]) }
      "serialize Double arrays" in { roundtrip(Array.empty[Double]) }
      "serialize Boolean arrays" in { roundtrip(Array.empty[Boolean]) }
      "serialize Char arrays" in { roundtrip(Array.empty[Char]) }
      "serialize String arrays" in { roundtrip(Array.empty[String]) }
      // TODO more examples like filled arrays and Array.empty[Object]
    }
}
