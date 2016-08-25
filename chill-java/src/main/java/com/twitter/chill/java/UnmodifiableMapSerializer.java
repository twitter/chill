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
 * A kryo {@link Serializer} for unmodifiable maps created via {@link Collections#unmodifiableMap(Map)}.
 * <p>
 * Note: This serializer does not support cyclic references, so if one of the objects
 * gets set the list as attribute this might cause an error during deserialization.
 * </p>
 *
 * @author <a href="mailto:alex@chermenin.ru">Alex Chermenin</a>
 */
public class UnmodifiableMapSerializer extends Serializer<Map<?, ?>> {

    @SuppressWarnings("unchecked")
    static public IKryoRegistrar registrar() {
        return new SingleRegistrar(Collections.unmodifiableMap(Collections.EMPTY_MAP).getClass(),
                new UnmodifiableMapSerializer());
    }

    final private Field mapField;

    public UnmodifiableMapSerializer() {
        try {
            mapField = Class.forName("java.util.Collections$UnmodifiableMap").getDeclaredField("m");
            mapField.setAccessible(true);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<?, ?> read(Kryo kryo, Input input, Class<Map<?, ?>> type) {
        try {
            Map<?, ?> map = (Map<?, ?>) kryo.readClassAndObject(input);
            return Collections.unmodifiableMap(map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(Kryo kryo, Output output, Map<?, ?> object) {
        try {
            Map<?, ?> map = (Map<?, ?>) mapField.get(object);
            kryo.writeClassAndObject(output, map);
        } catch (RuntimeException e) {
            // Don't eat and wrap RuntimeExceptions because the ObjectBuffer.write...
            // handles SerializationException specifically (resizing the buffer)...
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
