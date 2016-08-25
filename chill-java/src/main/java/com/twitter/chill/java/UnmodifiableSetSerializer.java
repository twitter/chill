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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
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
public class UnmodifiableSetSerializer extends Serializer<Set<?>> {

    @SuppressWarnings("unchecked")
    static public IKryoRegistrar registrar() {
        return new SingleRegistrar(Collections.unmodifiableSet(Collections.EMPTY_SET).getClass(),
                new UnmodifiableSetSerializer());
    }

    final private Field collectionField;

    public UnmodifiableSetSerializer() {
        try {
            collectionField =
                    Class.forName("java.util.Collections$UnmodifiableSet").getSuperclass().getDeclaredField("c");
            collectionField.setAccessible(true);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<?> read(Kryo kryo, Input input, Class<Set<?>> type) {
        try {
            Set<?> c = (Set<?>) kryo.readClassAndObject(input);
            return Collections.unmodifiableSet(c);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(Kryo kryo, Output output, Set<?> object) {
        try {
            Set<?> c = (Set<?>) collectionField.get(object);
            kryo.writeClassAndObject(output, c);
        } catch (RuntimeException e) {
            // Don't eat and wrap RuntimeExceptions because the ObjectBuffer.write...
            // handles SerializationException specifically (resizing the buffer)...
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
