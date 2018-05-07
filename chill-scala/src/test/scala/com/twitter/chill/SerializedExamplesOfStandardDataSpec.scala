package com.twitter.chill

import org.scalatest.{ Matchers, WordSpec }

class SerializedExamplesOfStandardDataSpec extends WordSpec with Matchers {
  s"""
    |Projects using chill to persist serialized data (for example in event sourcing
    |scenarios) depend on the serialized representation of the pre-registered
    |classes being stable. Therefore, it is important that updates to chill avoid
    |changing the serialization of the pre-registered classes as far as possible.
    |When changing a serialization becomes necessary, details of the changes should
    |be mentioned in the release notes.
    |
    |For the ScalaKryoInstantiators, the registered classes""".stripMargin
    .should {

      "serialize to the provided examples".in {
        checkSerialization("AgI=", 0, Int.box(1))
        checkSerialization("AwFhYuM=", 1, "abc")
        checkSerialization("BD+AAAA=", 2, Float.box(1))
        checkSerialization("BQE=", 3, Boolean.box(true))
        checkSerialization("BgE=", 4, Byte.box(1))
        checkSerialization("BwBh", 5, Char.box('a'))
        checkSerialization("CAAB", 6, Short.box(1))
        checkSerialization("CQI=", 7, Long.box(1))
        checkSerialization("Cj/wAAAAAAAA", 8, Double.box(1))
        checkSerialization("EQECBA==", 15, Some(2))
        checkSerialization("dAE=", 114, None)
      }
    }

  val kryo: KryoBase = {
    val instantiator = new ScalaKryoInstantiator()
    instantiator.setRegistrationRequired(true)
    instantiator.newKryo
  }
  val pool: KryoPool =
    KryoPool.withByteArrayOutputStream(4, new ScalaKryoInstantiator())
  def err(message: String): Unit =
    System.err.println(s"\n##########\n$message\n##########\n")
  def err(message: String, serialized: String): Unit =
    System.err.println(
      s"\n##########\n$message\nThe example serialized is $serialized\n##########\n")

  println("\n\n\n#########")
  val short: _root_.java.lang.Short = Short.box(1)
  println(Base64.encodeBytes(pool.toBytesWithClass(short)))
  val long: _root_.java.lang.Long = Long.box(1)
  println(Base64.encodeBytes(pool.toBytesWithClass(long)))
  println("#########\n\n\n")

  def checkSerialization2[T](example: String, serId: Int, expected: T): Unit = {
    println("\n\n\n#########")
    println(expected)
    println(expected.getClass)
    println(Base64.encodeBytes(pool.toBytesWithClass(expected)))
    println(Short.box(1))
    println(Short.box(1).getClass)
    println(Base64.encodeBytes(pool.toBytesWithClass(Short.box(1))))
    println("#########\n\n\n")

    val serialized = try Base64.encodeBytes(pool.toBytesWithClass(expected))
    catch {
      case e: Throwable => err(s"can't kryo serialize $expected: $e"); throw e
    }
    val bytes = try Base64.decode(example)
    catch {
      case e: Throwable =>
        err(s"can't base64 decode $example: $e", serialized); throw e
    }
    val deserialized = try pool.fromBytes(bytes)
    catch {
      case e: Throwable =>
        err(s"can't kryo deserialize $example: $e", serialized); throw e
    }
    val idOfExpected = kryo.getRegistration(expected.getClass).getId
    assert(
      idOfExpected == serId,
      s"$expected is registered with ID $idOfExpected, but expected $serId")
    assert(
      deserialized == expected,
      s"deserializing $example yields $deserialized, but expected $expected which serializes to $serialized")
  }

  def checkSerialization(example: String, serId: Int, expected: AnyRef): Unit = {
    val serialized = try Base64.encodeBytes(pool.toBytesWithClass(expected))
    catch {
      case e: Throwable => err(s"can't kryo serialize $expected: $e"); throw e
    }
    val bytes = try Base64.decode(example)
    catch {
      case e: Throwable =>
        err(s"can't base64 decode $example: $e", serialized); throw e
    }
    val deserialized = try pool.fromBytes(bytes)
    catch {
      case e: Throwable =>
        err(s"can't kryo deserialize $example: $e", serialized); throw e
    }
    val idOfExpected = kryo.getRegistration(expected.getClass).getId
    assert(
      idOfExpected == serId,
      s"$expected is registered with ID $idOfExpected, but expected $serId")
    assert(
      deserialized == expected,
      s"deserializing $example yields $deserialized, but expected $expected which serializes to $serialized")
    assert(serialized == example, s"$expected serializes to $serialized, but the test example is $example")
  }
}
