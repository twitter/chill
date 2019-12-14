package com.twitter.chill

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

object RegistrationIdsSpec {
  private def registeredEntries(k: KryoBase) =
    Stream
      .from(0)
      .map(k.getRegistration)
      .takeWhile(_ != null)
      .map(r => s"${r.getId} -> ${r.getType}")
      .mkString("\n")

  private def printMessageFor(k: KryoBase, scope: String): Unit =
    System.err.println(s"""\n\n
         |This test ($getClass)
         |will fail for $scope, most probably because the order of
         |registration IDs has changed or a registration was added or
         |removed. If that was intended, here is the list of entries
         |that are currently found, so you can update the test's
         |expected values:
         |
         |${registeredEntries(k)}\n\n\n""".stripMargin)
}

class RegistrationIdsSpec extends AnyWordSpec with Matchers {
  import RegistrationIdsSpec._
  import RegistrationIdsSpecData._

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
      val compatibility = classOf[AllScalaRegistrar_0_10_0].getSimpleName
      (s"be as expected for the backward compatibility layer $compatibility,\n" +
        "  i.e. contain the list of registrations defined in this test.").in {
        val k = new KryoBase
        new AllScalaRegistrar_0_10_0().apply(k)
        if (registeredEntries(k) != Entries_0_10_0) printMessageFor(k, compatibility)
        assert(registeredEntries(k) == Entries_0_10_0)
      }

      val current = classOf[AllScalaRegistrar].getSimpleName
      (s"be as expected for the current $current,\n" +
        "  i.e. contain the list of registrations defined in this test.").in {
        val k = new KryoBase
        new AllScalaRegistrar().apply(k)
        if (registeredEntries(k) != CurrentEntries) printMessageFor(k, current)
        assert(registeredEntries(k) == CurrentEntries)
      }
    }
}
