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

package com.twitter.chill.backwardcomp

import com.esotericsoftware.kryo.serializers.FieldSerializer
import com.twitter.chill.{Input, Kryo, ObjectSerializer, Output}

class BackwardCompatibleJavaWrappersSerializer[T](kryo: Kryo, typ: Class[_]) extends FieldSerializer[T](kryo, typ) {
  import BackwardCompatibleJavaWrappersSerializer._

  override def read(kryo: Kryo, input: Input, typ: Class[T]): T = {
    // Skipping the first serialized field which is assumed to be an $outer object reference
    kryo.getClassResolver.asInstanceOf[BackwardCompatibleClassResolver].readIgnoredClass(input)
    val refId = input.readVarInt(true)
    // Assuming ObjectSerializer which reads no data, so it does not mean if this is the first occurrence or not
    super.read(kryo, input, typ)
  }

  override def write(kryo: Kryo, output: Output, `object`: T): Unit = {
    kryo.getClassResolver.asInstanceOf[BackwardCompatibleClassResolver].writeFakeName(output, "scala.collection.convert.Wrappers$", OuterWrapper.getClass)
    kryo.writeObjectOrNull(output, OuterWrapper, new ObjectSerializer)
    super.write(kryo, output, `object`)
  }
}

object BackwardCompatibleJavaWrappersSerializer {
  object OuterWrapper
}
