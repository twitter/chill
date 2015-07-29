package com.twitter.chill.java

import java.util

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{ Input, Output }
import org.objenesis.strategy.StdInstantiatorStrategy
import org.scalatest._

import scala.util.Random

class BitSetSpec extends WordSpec with MustMatchers {

  implicit val kryo = new Kryo()

  def rt[A](a: A)(implicit k: Kryo): A = {
    val out = new Output(1000, -1)
    k.writeClassAndObject(out, a.asInstanceOf[AnyRef])
    val in = new Input(out.toBytes)
    k.readClassAndObject(in).asInstanceOf[A]
  }

  "A BitSetSerializer serializer" should {
    "handle BitSet" in {
      kryo.setInstantiatorStrategy(new StdInstantiatorStrategy)
      BitSetSerializer.registrar()(kryo)
      var simple = new util.BitSet(2048)
      simple.size() must be(2048)
      for (i <- 0 to 1337) {
        simple.set(i, true)
      }
      // we assume everything after 1337 to be false
      simple.get(1338) must equal(false)
      simple.get(2000) must equal(false)
      var dolly = rt(simple)
      simple = null // avoid accidental calls
      dolly.size() must be(2048)
      for (i <- 0 to 1337) {
        dolly.get(i) must be(true)
      }
      dolly.get(1338) must equal(false)
      dolly.get(2000) must equal(false)
    }

    /**
     * My results:
     * The old serializer took 2886ms
     * The new serializer took 112ms
     * The old serializer needs 2051 bytes
     * The new serializer needs 258 bytes
     */
    "handle a BitSet efficiently" in {
      val oldKryo = new Kryo()
      OldBitSetSerializer.registrar()(oldKryo)

      val newKryo = new Kryo()
      BitSetSerializer.registrar()(newKryo)

      val element = new util.BitSet(2048)
      val rnd = new Random()
      for (i <- 0 to 2048) {
        element.set(i, rnd.nextBoolean())
      }

      // warmup In case anybody wants to see hotspot
      var lastBitSetFromOld: util.BitSet = null
      for (i <- 0 to 50000) {
        lastBitSetFromOld = rt(element)(oldKryo)
      }
      var start = System.currentTimeMillis()
      for (i <- 0 to 100000) {
        rt(element)(oldKryo)
      }
      println("The old serializer took " + (System.currentTimeMillis() - start) + "ms")

      var lastBitSetFromNew: util.BitSet = null
      // warmup for the new kryo
      for (i <- 0 to 50000) {
        lastBitSetFromNew = rt(element)(newKryo)
      }
      // check for the three bitsets to be equal
      for (i <- 0 to 2048) {
        // original bitset against old serializer output
        element.get(i) must be(lastBitSetFromOld.get(i))

        // original bitset against new serializer output
        element.get(i) must be(lastBitSetFromNew.get(i))
      }

      start = System.currentTimeMillis()
      for (i <- 0 to 100000) {
        rt(element)(newKryo)
      }
      println("The new serializer took " + (System.currentTimeMillis() - start) + "ms")

      var out = new Output(1, -1)
      oldKryo.writeObject(out, element)
      out.flush()
      var oldBytes = out.total()
      println("The old serializer needs " + oldBytes + " bytes")
      out = new Output(1, -1)
      newKryo.writeObject(out, element)
      out.flush()
      var newBytes = out.total()
      println("The new serializer needs " + newBytes + " bytes")

      oldBytes >= newBytes must be(true)
    }
  }
}
