package com.twitter.chill

import org.specs._

import scala.collection.immutable.ListMap
import scala.collection.immutable.HashMap

/*
* This is just a test case for Kryo to deal with. It should
* be outside KryoTest, otherwise the enclosing class, KryoTest
* will also need to be serialized
*/
case class TestCaseClassForSerialization(x : String, y : Int)

case class TestValMap(val map : Map[String,Double])
case class TestValHashMap(val map : HashMap[String,Double])
case class TestVarArgs(vargs: String*)

class KryoTest extends Specification with KryoSerializer {

  noDetailedDiffs() //Fixes issue for scala 2.9

  def rt[T](t : T): T = deserialize[T](serialize(t.asInstanceOf[AnyRef]))

  "KryoSerializers and KryoDeserializers" should {
    "round trip any non-array object" in {
      val test = List(1,2,"hey",(1,2),
                      ("hey","you"),
                      ("slightly", 1L, "longer", 42, "tuple"),
                      Map(1->2,4->5),
                      0 to 100,
                      (0 to 42).toList, Seq(1,100,1000),
                      Map("good" -> 0.5, "bad" -> -1.0),
                      Set(1,2,3,4,10),
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
                      'hai)
        .asInstanceOf[List[AnyRef]]

      val rtTest = test map { serialize(_) } map { deserialize[AnyRef](_) }
      rtTest must be_==(test)
    }
    "handle manifests" in {
      rt(manifest[Int]) must be_==(manifest[Int])
      rt(manifest[(Int,Int)]) must be_==(manifest[(Int,Int)])
      rt(manifest[Array[Int]]) must be_==(manifest[Array[Int]])
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
      arrayRT(Array(None, Nil, None, Nil))
    }
    "handle scala singletons" in {
      val test = List(Nil, None)
      //Serialize each:
      rt(test) must be_==(test)
    }
    "Serialize a giant list" in {
      val bigList = (1 to 100000).toList
      val list2 = rt(bigList)
      list2.size must be_==(bigList.size)
      //Specs, it turns out, also doesn't deal with giant lists well:
      list2.zip(bigList).foreach { tup => tup._1 must be_==(tup._2) }
    }
  }
}
