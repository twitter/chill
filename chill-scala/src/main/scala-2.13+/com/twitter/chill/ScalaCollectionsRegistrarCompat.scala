package com.twitter.chill

import scala.collection.immutable.Range

class ScalaCollectionsRegistrarCompat extends IKryoRegistrar {
  override def apply(newK: Kryo): Unit =
    newK.register(classOf[Range.Exclusive])
}
