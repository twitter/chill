/*
 * Copyright 2016 Alex Chermenin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.twitter.chill.java;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.twitter.chill.IKryoRegistrar;
import com.twitter.chill.SingleRegistrar;

import java.lang.reflect.Field;
import java.util.*;

/**
 * A kryo {@link Serializer} for unmodifiable sets created via {@link Collections#unmodifiableSet(Set)}.
 * <p>
 * Note: This serializer does not support cyclic references, so if one of the objects
 * gets set the list as attribute this might cause an error during deserialization.
 * </p>
 *
 * @author <a href="mailto:alex@chermenin.ru">Alex Chermenin</a>
 */
public class UnmodifiableSetSerializer extends UnmodifiableJavaCollectionSerializer<Set<?>> {
  
  @SuppressWarnings("unchecked")
  static public IKryoRegistrar registrar() {
    return new SingleRegistrar(Collections.unmodifiableSet(Collections.EMPTY_SET).getClass(),
        new UnmodifiableSetSerializer());
  }
  
  @Override
  protected Field getInnerField() throws Exception {
    return Class.forName("java.util.Collections$UnmodifiableSet").getSuperclass().getDeclaredField("c");
  }
  
  @Override
  protected Set<?> newInstance(Set<?> s) {
    return Collections.unmodifiableSet(s);
  }
}
