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

package com.twitter.chill

import com.twitter.chill.java.PackageRegistrar

import com.twitter.bijection.{ Bufferable, ImplicitBijection }

object KryoSerializer {

  /** Return an instantiator that is configured to work well with scala
   * objects/classes, but has no serializers registered
   */
  def empty: KryoInstantiator = new EmptyScalaKryoInstantiator

  /** Return an instantiator that is configured to work well with scala
   * objects/classes, but has no serializers registered
   */
  def registered: KryoInstantiator = new ScalaKryoInstantiator

  def registerCollectionSerializers: IKryoRegistrar = new ScalaCollectionsRegistrar

  def registerAll: IKryoRegistrar = new AllScalaRegistrar

  /** Use a bijection[A,B] then the KSerializer on B
   */
  def viaBijection[A,B](kser: KSerializer[B])(implicit bij: ImplicitBijection[A,B], cmf: ClassManifest[B]): KSerializer[A] =
    new KSerializer[A] {
      def write(k: Kryo, out: Output, obj: A) { kser.write(k, out, bij(obj)) }
      def read(k: Kryo, in: Input, cls: Class[A]) =
        bij.invert(kser.read(k, in, cmf.erasure.asInstanceOf[Class[B]]))
    }

  def viaBufferable[T](implicit b: Bufferable[T]): KSerializer[T] =
    InjectiveSerializer.asKryo[T](Bufferable.injectionOf[T])
}
