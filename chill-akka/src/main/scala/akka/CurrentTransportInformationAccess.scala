package akka

import akka.serialization.Serialization

object CurrentTransportInformationAccess {
  def get = Serialization.currentTransportInformation
}
