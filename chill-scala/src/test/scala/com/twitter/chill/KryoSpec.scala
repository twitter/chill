/*
Copyright 2012 Twitter, Inc.

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

import org.specs._
import org.specs.matcher.Matcher

import scala.collection.immutable.{SortedSet, BitSet, ListSet, HashSet, SortedMap, ListMap, HashMap}
import scala.collection.mutable.{ArrayBuffer => MArrayBuffer, BitSet => MBitSet, HashMap => MHashMap}
import _root_.java.util.PriorityQueue
import _root_.java.util.Locale
import scala.collection.mutable
import scala.collection.JavaConverters._

/*
* This is just a test case for Kryo to deal with. It should
* be outside KryoSpec, otherwise the enclosing class, KryoSpec
* will also need to be serialized
*/
case class TestCaseClassForSerialization(x : String, y : Int)

case class TestValMap(map: Map[String,Double])
case class TestValHashMap(map: HashMap[String,Double])
case class TestVarArgs(vargs: String*)

class SomeRandom(val x: Int)

object WeekDay extends Enumeration {
 type WeekDay = Value
 val Mon, Tue, Wed, Thu, Fri, Sat, Sun = Value
}

trait ExampleUsingSelf { self =>
  def count = 0
  def addOne = new ExampleUsingSelf {override def count=self.count+1}
}

case class Foo(m1: Map[String, Int], m2: Map[String, Seq[String]])

class KryoSpec extends Specification with BaseProperties {

  noDetailedDiffs() //Fixes issue for scala 2.9

  def roundtrip[T] = new Matcher[T]{
    def apply(t: => T) = (rtEquiv(t), "successfull serialization roundtrip for " + t, "failed serialization roundtrip for " + t)
  }

  def getKryo = KryoSerializer.registered.newKryo

  "KryoSerializers and KryoDeserializers" should {
    "round trip any non-array object" in {
      val test = List(1,2,"hey",(1,2),
                      ("hey","you"),
                      ("slightly", 1L, "longer", 42, "tuple"),
                      Foo(Map("1" -> 1), Map("1" -> Seq("foo.com"))),
                      Map(1->2,4->5),
                      0 to 100,
                      (0 to 42).toList, Seq(1,100,1000),
                      Right(Map("hello" -> 100)),
                      Left(Map(1->"YO!")),
                      Some(Left(10)),
                      Map("good" -> 0.5, "bad" -> -1.0),
                      Map('a -> 'a, 'b -> 'b, 'c -> 'c, 'd -> 'd, 'e -> 'e),
                      MArrayBuffer(1,2,3,4,5),
                      List(Some(MHashMap(1->1, 2->2)), None, Some(MHashMap(3->4))),
                      Set(1,2,3,4,10),
                      HashSet(1,2),
                      SortedSet[Long](),
                      SortedSet(1L, 2L, 3L, 4L),
                      BitSet(),
                      BitSet((0 until 1000).map{ x : Int => x*x } : _*),
                      MBitSet(),
                      MBitSet((0 until 1000).map{ x : Int => x*x } : _*),
                      SortedMap[Long, String](),
                      SortedMap("b" -> 2, "a" -> 1),
                      ListMap("good" -> 0.5, "bad" -> -1.0),
                      HashMap("good" -> 0.5, "bad" -> -1.0),
                      TestCaseClassForSerialization("case classes are: ", 10),
                      TestValMap(Map("you" -> 1.0, "every" -> 2.0, "body" -> 3.0, "a" -> 1.0,
                        "b" -> 2.0, "c" -> 3.0, "d" -> 4.0)),
                      TestValHashMap(HashMap("you" -> 1.0)),
                      TestVarArgs("hey", "you", "guys"),
                      implicitly[ClassManifest[(Int,Int)]],
                      Vector(1,2,3,4,5),
                      TestValMap(null),
                      Some("junk"),
                      List(1, 2, 3).asJava,
                      Map("hey" -> 1, "you" -> 2).asJava,
                      new _root_.java.util.ArrayList(Seq(1, 2, 3).asJava).asScala,
                      new _root_.java.util.HashMap[Int,Int](Map(1 -> 2, 3 -> 4).asJava).asScala,
                      (),
                      'hai)
        .asInstanceOf[List[AnyRef]]

      test.foreach { _ must roundtrip }
    }
    "round trip a SortedSet" in {
      val a = SortedSet[Long]() // Test empty SortedSet
      val b = SortedSet[Int](1,2) // Test small SortedSet
      val c = SortedSet[Int](1,2,3,4,6,7,8,9,10)(Ordering.fromLessThan((x, y) => x > y)) // Test with different ordering
      a must roundtrip
      b must roundtrip
      c must roundtrip
      (rt(c) + 5) must be_==(c + 5)
    }
    "round trip a ListSet" in {
      val a = ListSet[Long]() // Test empty SortedSet
      val b = ListSet[Int](1,2) // Test small ListSet
      val c = ListSet[Int](1,2,3,4,6,7,8,9,10)
      a must roundtrip
      b must roundtrip
      c must roundtrip
    }
    "handle trait with reference of self" in {
      var a= new ExampleUsingSelf{}
      var b=rt(a.addOne)
      b.count must be_==(1)
    }
    "handle manifests" in {
      manifest[Int] must roundtrip
      manifest[(Int,Int)] must roundtrip
      manifest[Array[Int]] must roundtrip
    }
    "handle arrays" in {
      def arrayRT[T](arr : Array[T]) {
        // Array doesn't have a good equals
        rt(arr).toList must be_==(arr.toList)
      }
      arrayRT(Array(0))
      arrayRT(Array(0.1))
      arrayRT(Array("hey"))
      arrayRT(Array((0,1)))
      arrayRT(Array((0,1), (1,0)))
      arrayRT(Array(None, Nil, None, Nil))
    }
    "handle WrappedArray instances" in {
      val tests = Seq(
        Array((1,1), (2,2), (3,3)).toSeq,
        Array((1.0, 1.0), (2.0, 2.0)).toSeq,
        Array((1.0, "1.0"), (2.0, "2.0")).toSeq)
      tests.foreach { _ must roundtrip }
    }
    "handle lists of lists" in {
      List(("us", List(1)), ("jp", List(3, 2)), ("gb", List(3, 1))) must roundtrip
    }
    "handle scala singletons" in {
      List(Nil, None) must roundtrip
      None must roundtrip
      (rt(None) eq None) must beTrue
    }
    "serialize a giant list" in {
      val bigList = (1 to 100000).toList
      val list2 = rt(bigList)
      list2.size must be_==(bigList.size)
      //Specs, it turns out, also doesn't deal with giant lists well:
      list2.zip(bigList).foreach { tup => tup._1 must be_==(tup._2) }
    }
    "handle scala enums" in {
       WeekDay.values.foreach { _ must roundtrip }
    }
    "use java serialization" in {
      val kinst = { () => getKryo.javaForClass[TestCaseClassForSerialization] }
      rtEquiv(kinst, TestCaseClassForSerialization("hey", 42)) must beTrue
    }
    "work with Meatlocker" in {
      val l = List(1,2,3)
      val ml = MeatLocker(l)
      jrt(ml).get must_==(l)
    }
    "work with Externalizer" in {
      val l = List(1,2,3)
      val ext = Externalizer(l)
      ext.javaWorks must be_==(true)
      jrt(ext).get must_==(l)
    }
    "work with Externalizer with non-java-ser" in {
      val l = new SomeRandom(3)
      val ext = Externalizer(l)
      ext.javaWorks must be_==(false)
      jrt(ext).get.x must_==(l.x)
    }
    "Externalizer can RT with Kryo" in {
      val l = new SomeRandom(10)
      val ext = Externalizer(l)
      rt(ext).get.x must_==(l.x)
    }
    "handle Regex" in {
      val test = """\bhilarious""".r
      val roundtripped = rt(test)
      roundtripped.pattern.pattern must be_==(test.pattern.pattern)
      roundtripped.findFirstIn("hilarious").isDefined must beTrue
    }
    "handle small immutable maps when registration is required" in {
      val inst = { () =>
        val kryo = KryoSerializer.registered.newKryo
        kryo.setRegistrationRequired(true)
        kryo
      }
      val m1 = Map('a -> 'a)
      val m2 = Map('a -> 'a, 'b -> 'b)
      val m3 = Map('a -> 'a, 'b -> 'b, 'c -> 'c)
      val m4 = Map('a -> 'a, 'b -> 'b, 'c -> 'c, 'd -> 'd)
      val m5 = Map('a -> 'a, 'b -> 'b, 'c -> 'c, 'd -> 'd, 'e -> 'e)
      Seq(m1, m2, m3, m4, m5).foreach { rtEquiv(inst, _) must beTrue }
    }
    "handle small immutable sets when registration is required" in {
      val inst = { () =>
        val kryo = getKryo
        kryo.setRegistrationRequired(true)
        kryo
      }
      val s1 = Set('a)
      val s2 = Set('a, 'b)
      val s3 = Set('a, 'b, 'c)
      val s4 = Set('a, 'b, 'c, 'd)
      val s5 = Set('a, 'b, 'c, 'd, 'e)
      Seq(s1, s2, s3, s4, s5).foreach { rtEquiv(inst, _) must beTrue }
    }
    "handle nested mutable maps" in {
      val inst = { () =>
        val kryo = getKryo
        kryo.setRegistrationRequired(true)
        kryo
      }
      val obj0 = mutable.Map(4 -> mutable.Set("house1", "house2"),
                             1 -> mutable.Set("name3", "name4", "name1", "name2"),
                             0 -> mutable.Set(1, 2, 3, 4))

      // Make sure to make a totally separate map to check equality with
      val obj1 = mutable.Map(4 -> mutable.Set("house1", "house2"),
                             1 -> mutable.Set("name3", "name4", "name1", "name2"),
                             0 -> mutable.Set(1, 2, 3, 4))

      rtEquiv(inst, obj0) must beTrue
    }
    "deserialize InputStream" in {
      val obj   = Seq(1, 2, 3)
      val bytes = serialize(obj)

      val inputStream = new _root_.java.io.ByteArrayInputStream(bytes)

      val kryo = getKryo
      val rich = new RichKryo(kryo)

      val opt1 = rich.fromInputStream(inputStream)
      opt1 must be_==(Option(obj))

      // Test again to make sure it still works
      inputStream.reset()
      val opt2 = rich.fromInputStream(inputStream)
      opt2 must be_==(Option(obj))
    }
    "deserialize ByteBuffer" in {
      val obj   = Seq(1, 2, 3)
      val bytes = serialize(obj)

      val byteBuffer = _root_.java.nio.ByteBuffer.wrap(bytes)

      val kryo = getKryo
      val rich = new RichKryo(kryo)

      val opt1 = rich.fromByteBuffer(byteBuffer)
      opt1 must be_==(Option(obj))

      // Test again to make sure it still works
      byteBuffer.rewind()
      val opt2 = rich.fromByteBuffer(byteBuffer)
      opt2 must be_==(Option(obj))
    }
    "Handle Ordering.reverse" in {
      // This is exercising the synthetic field serialization in 2.10
      val ord = Ordering.fromLessThan[(Int,Int)] { (l, r) => l._1 < r._1 }
      // Now with a reverse ordering:
      val qr = new PriorityQueue[(Int,Int)](3, ord.reverse)
      qr.add((2,3))
      qr.add((4,5))
      def toList[A](q: PriorityQueue[A]): List[A] = {
        import scala.collection.JavaConverters._
        q.iterator.asScala.toList
      }
      val qrlist = toList(qr)
      toList(rt(qr)) must be_==(qrlist)
    }
    "Ranges should be fixed size" in {
      val MAX_RANGE_SIZE = 188 // what seems to be needed.
      serialize((1 to 10000)).size must be_<(MAX_RANGE_SIZE) // some fixed size
      serialize((1 to 10000 by 2)).size must be_<(MAX_RANGE_SIZE) // some fixed size
      serialize((1 until 10000)).size must be_<(MAX_RANGE_SIZE) // some fixed size
      serialize((1 until 10000 by 2)).size must be_<(MAX_RANGE_SIZE) // some fixed size
      serialize((1L to 10000L)).size must be_<(MAX_RANGE_SIZE) // some fixed size
      serialize((1L to 10000L by 2L)).size must be_<(MAX_RANGE_SIZE) // some fixed size
      serialize((1L until 10000L)).size must be_<(MAX_RANGE_SIZE) // some fixed size
      serialize((1L until 10000L by 2L)).size must be_<(MAX_RANGE_SIZE) // some fixed size
      serialize((1.0 to 10000.0)).size must be_<(MAX_RANGE_SIZE) // some fixed size
      serialize((1.0 to 10000.0 by 2.0)).size must be_<(MAX_RANGE_SIZE) // some fixed size
      serialize((1.0 until 10000.0)).size must be_<(MAX_RANGE_SIZE) // some fixed size
      serialize((1.0 until 10000.0 by 2.0)).size must be_<(MAX_RANGE_SIZE) // some fixed size
    }
  }
}
