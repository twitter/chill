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

import com.twitter.bijection.Injection
import com.twitter.bijection.{ Bufferable, Bijection, ImplicitBijection, Injection }

object BijectionEnrichedKryo {
  implicit def enrich(k: Kryo): BijectionEnrichedKryo = new BijectionEnrichedKryo(k)

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

class BijectionEnrichedKryo(k: Kryo) {

  def injectionForClass[T](implicit inj: Injection[T, Array[Byte]], cmf: ClassManifest[T]): Kryo = {
    k.register(cmf.erasure, InjectiveSerializer.asKryo[T])
    k
  }

  def injectionForSubclass[T](implicit inj: Injection[T, Array[Byte]], cmf: ClassManifest[T]): Kryo = {
    k.addDefaultSerializer(cmf.erasure, InjectiveSerializer.asKryo[T])
    k
  }

  def bufferableForClass[T](implicit b: Bufferable[T], cmf: ClassManifest[T]): Kryo = {
    k.register(cmf.erasure, BijectionEnrichedKryo.viaBufferable[T])
    k
  }

  /** B has to already be registered, then use the KSerializer[B] to create KSerialzer[A]
   */
  def forClassViaBijection[A,B](implicit bij: ImplicitBijection[A,B], acmf: ClassManifest[A], bcmf: ClassManifest[B]): Kryo = {
    val kserb = k.getSerializer(bcmf.erasure).asInstanceOf[KSerializer[B]]
    k.register(acmf.erasure, BijectionEnrichedKryo.viaBijection[A,B](kserb))
    k
  }

  /** Helpful override to alleviate rewriting types. */
  def forClassViaBijection[A,B](bij: Bijection[A,B])(implicit acmf: ClassManifest[A], bcmf: ClassManifest[B]): Kryo = {
    implicit def implicitBij = bij
    this.forClassViaBijection[A, B]
  }

}
