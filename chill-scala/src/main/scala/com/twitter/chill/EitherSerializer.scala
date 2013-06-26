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

class EitherSerializer[A, B] extends KSerializer[Either[A, B]] {
  def write(kser: Kryo, out: Output, either: Either[A, B]) {
    val (item, isLeft) = either match {
      case Left(l) => (l, true)
      case Right(r) => (r, false)
    }
    out.writeBoolean(isLeft)
    kser.writeClassAndObject(out, item)
  }

  def read(kser: Kryo, in: Input, cls: Class[Either[A, B]]): Either[A, B] = {
    if (in.readBoolean)
      Left(kser.readClassAndObject(in).asInstanceOf[A])
    else
      Right(kser.readClassAndObject(in).asInstanceOf[B])
  }
}
