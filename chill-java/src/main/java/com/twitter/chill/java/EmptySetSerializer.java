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
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.twitter.chill.IKryoRegistrar;
import com.twitter.chill.SingleRegistrar;

import java.util.*;

/**
 * A kryo {@link Serializer} for empty set created via {@link Collections#emptySet()} or {@link Collections#EMPTY_SET}.
 * <p>
 * Note: This serializer does not support cyclic references, so if one of the objects
 * gets set the list as attribute this might cause an error during deserialization.
 * </p>
 *
 * @author <a href="mailto:alex@chermenin.ru">Alex Chermenin</a>
 */
public class EmptySetSerializer extends Serializer<Set<?>> {

    private static Class<?> EMPTY_SET_CLASS = Collections.EMPTY_SET.getClass();

    @SuppressWarnings("unchecked")
    static public IKryoRegistrar registrar() {
        return new SingleRegistrar(EMPTY_SET_CLASS, new EmptySetSerializer());
    }

    @Override
    public Set<?> read(Kryo kryo, Input input, Class<Set<?>> type) {
        try {
            Class clazz = kryo.readClass(input).getType();
            if (clazz.equals(EMPTY_SET_CLASS)) {
                return Collections.EMPTY_SET;
            } else {
                throw new KryoException("Unexpected class " + clazz.getCanonicalName() +
                        " (expect " + EMPTY_SET_CLASS.getCanonicalName());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(Kryo kryo, Output output, Set<?> object) {
        try {
            kryo.writeClass(output, object.getClass());
        } catch (RuntimeException e) {
            // Don't eat and wrap RuntimeExceptions because the ObjectBuffer.write...
            // handles SerializationException specifically (resizing the buffer)...
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
