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

package com.twitter.chill.java;

import com.esotericsoftware.kryo.kryo5.serializers.JavaSerializer;

import com.twitter.chill.IKryoRegistrar;
import com.twitter.chill.SingleRegistrar;

import java.text.SimpleDateFormat;

/** This class fails with the Fields serializer.
 * If it is a perf bottleneck, we could write a Kryo serializer
 */
public class SimpleDateFormatSerializer extends JavaSerializer {
   static public IKryoRegistrar registrar() {
      return new SingleRegistrar(SimpleDateFormat.class, new SimpleDateFormatSerializer());
    }
}
