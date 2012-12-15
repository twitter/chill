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

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.{ Serializer => KSerializer }
import com.esotericsoftware.kryo.io.{ Input, Output }

class EnumerationSerializer extends KSerializer[Enumeration$Value] {

  private def enumOf(v: AnyRef): Enumeration =
    //TODO: hacky, but not clear how to fix:
    v.getClass
      .getMethod("scala$Enumeration$$outerEnum")
      .invoke(v)
      .asInstanceOf[scala.Enumeration]

  def write(kser: Kryo, out: Output, obj: Enumeration$Value) {
    val enum = enumOf(obj)
    kser.writeClassAndObject(out, enum)
    // Now, we just write the ID:
    out.writeInt(obj.id)
  }

  def read(kser: Kryo, in: Input, cls: Class[Enumeration$Value]): Enumeration$Value = {
    val enum = kser.readClassAndObject(in).asInstanceOf[Enumeration]
    enum(in.readInt).asInstanceOf[Enumeration$Value]
  }
}
