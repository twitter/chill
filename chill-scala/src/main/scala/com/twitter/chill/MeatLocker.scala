/**
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

object MeatLocker {
  def apply[T](t: T) = new MeatLocker(t)
}

// TODO: Use Injection and return an Option[T]. Or upgrade to scala
// 2.10 fully and return a Try[T].
class MeatLocker[T](@transient t: T) extends java.io.Serializable {
  protected val tBytes = KryoBijection(t.asInstanceOf[AnyRef])
  lazy val get: T = copy
  def copy: T = KryoBijection.invert(tBytes).asInstanceOf[T]
}
