package com.twitter.chill

class ScalaCollectionsRegistrarCompat extends IKryoRegistrar {
  override def apply(newK: Kryo): Unit = ()
}
