package com.twitter.chill.java

import java.io.ByteArrayOutputStream

import com.twitter.chill.{ KryoInstantiator, KryoPool }
import org.scalatest.{ Matchers, WordSpec }

class SerDeStateTest extends WordSpec with Matchers {
  "SerDeState" should {
    "Properly write out to a Stream" in {
      val pool = KryoPool.withBuffer(1, new KryoInstantiator, 1000, -1)
      val st = pool.borrow()

      st.writeClassAndObject("Hello World")
      val baos = new ByteArrayOutputStream
      st.writeOutputTo(baos)

      st.clear()
      st.setInput(baos.toByteArray)
      st.readClassAndObject() should equal("Hello World")
    }
  }
}