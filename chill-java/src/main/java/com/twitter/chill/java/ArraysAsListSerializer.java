/*
 * Copyright 2010 Martin Grotzke
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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * A kryo {@link Serializer} for lists created via {@link Arrays#asList(Object...)}.
 * <p>
 * Note: This serializer does not support cyclic references, so if one of the objects
 * gets set the list as attribute this might cause an error during deserialization.
 * </p>
 *
 * @author <a href="mailto:martin.grotzke@javakaffee.de">Martin Grotzke</a>
 */
public class ArraysAsListSerializer extends Serializer<List<?>> {

    static public void register(Kryo k) {
      k.register(Arrays.asList("").getClass(), new ArraysAsListSerializer());
    }

    private Field _arrayField;

    public ArraysAsListSerializer() {
        try {
            _arrayField = Class.forName( "java.util.Arrays$ArrayList" ).getDeclaredField( "a" );
            _arrayField.setAccessible( true );
        } catch ( final Exception e ) {
            throw new RuntimeException( e );
        }
    }

    @Override
    public List<?> read(final Kryo kryo, final Input input, final Class<List<?>> type) {
        final int length = input.readInt(true);
        final Class<?> componentType = kryo.readClass( input ).getType();
        try {
            final Object[] items = (Object[]) Array.newInstance( componentType, length );
            for( int i = 0; i < length; i++ ) {
                items[i] = kryo.readClassAndObject( input );
            }
            return Arrays.asList( items );
        } catch ( final Exception e ) {
            throw new RuntimeException( e );
        }
    }

    @Override
    public void write(final Kryo kryo, final Output output, final List<?> obj) {
        try {
            final Object[] array = (Object[]) _arrayField.get( obj );
            output.writeInt(array.length, true);
            final Class<?> componentType = array.getClass().getComponentType();
            kryo.writeClass( output, componentType );
            for( final Object item : array ) {
                kryo.writeClassAndObject( output, item );
            }
        } catch ( final RuntimeException e ) {
             // Don't eat and wrap RuntimeExceptions because the ObjectBuffer.write...
             // handles SerializationException specifically (resizing the buffer)...
             throw e;
        } catch ( final Exception e ) {
             throw new RuntimeException( e );
        }
    }
}
