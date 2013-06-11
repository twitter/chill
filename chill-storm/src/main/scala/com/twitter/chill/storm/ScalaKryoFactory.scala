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

package com.twitter.chill.storm

import backtype.storm.serialization.IKryoFactory
import com.esotericsoftware.kryo.Kryo
import com.twitter.chill.{ KryoBase, KryoSerializer }
import org.objenesis.strategy.StdInstantiatorStrategy
import java.util.{ HashMap, Map => JMap }

/**
 *  @author Oscar Boykin
 *  @author Sam Ritchie
 */

class ScalaKryoFactory extends IKryoFactory {
  override def getKryo(conf: JMap[_,_]): Kryo = {
    val k = new KryoBase
    k.setRegistrationRequired(false)
    k.setInstantiatorStrategy(new StdInstantiatorStrategy)
    k
  }
  override def preRegister(k: Kryo, conf: JMap[_,_]) { populate(k) }
  override def postRegister(k: Kryo, conf: JMap[_,_]) { }
  override def postDecorate(k: Kryo, conf: JMap[_,_]) { }

  def populate(k: Kryo) {
    // Register all the chill serializers:
    KryoSerializer.registerAll(k)

    //Add commonly used types with Fields serializer:
    registeredTypes.foreach { cls => k.register(cls) }
  }

  // TODO: this was cargo-culted from
  // [Scalding](https://github.com/twitter/scalding/blob/develop/src/main/scala/com/twitter/scalding/serialization/KryoHadoop.scala),
  // which in turn cargo-culted from Spark.
  //
  // Types to pre-register.

  def registeredTypes: List[Class[_]] = {
    List(
      // Arrays
      Array(1), Array(1.0), Array(1.0f), Array(1L), Array(""), Array(("", "")),
      Array(new java.lang.Object), Array(1.toByte), Array(true), Array('c'),
      // Options and Either
      Some(1), Left(1), Right(1)
    ).map { _.getClass }
  }
}
