/*
 Copyright 2013 Twitter, Inc.

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

import com.twitter.bijection.{ Base64String, Bijection, CastInjection, Injection }
import com.twitter.bijection.Conversion.asMethod

import _root_.java.util.{ Map => JMap }

/**
  * Helpful methods for registering Injections as serializers by way
  * of a Configuration map.
  *
  * @author Oscar Boykin
  * @author Sam Ritchie
  * @author Ashu Singhal
  */

// unsafe. remove when commons-codec issue gets resolved.
//
// TODO: change this to Injection[Base64String, String] and just use
// allCatch on inverting the decode.

object UnsafeBase64StringUnwrap extends Bijection[Base64String, String] {
  override def apply(bs: Base64String) = bs.str
  override def invert(str: String) = Base64String(str)
}

/**
  * Prefix is the namespace used for all registrations within the
  * configuration supplied to each registration method.
  */
case class KryoRegistrationHelper(prefix: String) {
  val REGISTRARS = prefix + ".registrars"
  val CLASS_REGISTRATIONS = prefix + ".class.registrations"
  val SEPARATOR = ":"

  implicit def toBase64String[T: Manifest]: Injection[T, String] =
    CastInjection.of[T, Any]
      .andThen(KryoInjection)
      .andThen(Bijection.connect[Array[Byte], Base64String])
      .andThen(UnsafeBase64StringUnwrap)

  // TODO: can we store type params in here too and prevent the cast?
  def getConfValue[T](conf: JMap[_, _], key: String): Option[T] =
    Option(conf.get(key).asInstanceOf[T])

  /**
    * Accepts a config map and a key that may contain a list of base64
    * encoded, kryo-serialized entries and returns an option of an
    * Iterable of the deserialized items.
    */
  def getAll[T: Manifest](conf: JMap[_, _], key: String): Option[Iterable[T]] =
    getConfValue[String](conf, key)
      .map { _.split(SEPARATOR) }
      .map { _.flatMap(_.as[Option[T]]) }

  /**
    * Appends the supplied string entry to the current value for "key"
    * in the supplied conf map.
    */
  def append(conf: JMap[String, AnyRef], key: String, entry: String) {
    val newV = List(getConfValue[String](conf, key).getOrElse(""), entry)
      .mkString(SEPARATOR)
    conf.put(key, newV)
  }

  /**
    * Accepts a list of items to store under the supplied k,
    * serializes then all into base-64 encoded strings with Kryo and
    * appends each entry onto the existing list of serialized entries
    * for the supplied key.
    */
  def appendAll(conf: JMap[String, AnyRef], k: String, items: TraversableOnce[AnyRef]) {
    items.foreach { item =>
      append(conf, k, item.as[String])
    }
  }

  /**
    * Serialize these instances or registrars
    */
  def resetRegistrars(conf: JMap[String, AnyRef]) { conf.remove(REGISTRARS) }
  def addRegistrars(conf: JMap[String, AnyRef], pairs: TraversableOnce[IKryoRegistrar]) {
    appendAll(conf, REGISTRARS, pairs)
  }
  def getRegistrars(conf: JMap[_,_]): Option[Iterable[IKryoRegistrar]] =
    getAll(conf, REGISTRARS)

  /**
    * Registration for classes.
    */
  def resetClasses(conf: JMap[_, _]) { conf.remove(CLASS_REGISTRATIONS) }
  def registerClasses(conf: JMap[String, AnyRef], klasses: TraversableOnce[Class[_]]) {
    appendAll(conf, CLASS_REGISTRATIONS, klasses)
  }
  def getRegisteredClasses(conf: JMap[_,_]): Option[Iterable[Class[_]]] =
    getAll(conf, CLASS_REGISTRATIONS)

  /**
    * Actual Kryo registration.
    */
  def asRegistrar(conf: JMap[_,_]): IKryoRegistrar = new IKryoRegistrar {
    def apply(k: Kryo) {
      getRegistrars(conf)
        .foreach { krs => krs.foreach { _.apply(k) } }

      getRegisteredClasses(conf)
        .foreach { k.registerClasses(_) }
    }
  }
}
