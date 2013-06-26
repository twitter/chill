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

package com.twitter.chill.hadoop;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import org.apache.hadoop.io.serializer.Deserializer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class KryoDeserializer implements Deserializer<Object> {

    private final KryoSerialization kryoSerialization;
    private final Class<Object> klass;

    private DataInputStream inputStream;

    public KryoDeserializer(KryoSerialization kryoSerialization, Class<Object> klass) {
        this.kryoSerialization =  kryoSerialization;
        this.klass = klass;
    }

    public void open(InputStream in) throws IOException {
        if( in instanceof DataInputStream)
            inputStream = (DataInputStream) in;
        else
            inputStream = new DataInputStream( in );
    }

    public Object deserialize(Object o) throws IOException {
        // TODO, we could share these buffers if we see that alloc is bottlenecking
        byte[] bytes = new byte[inputStream.readInt()];
        inputStream.readFully( bytes );

        Kryo kryo = kryoSerialization.borrowKryo();
        try {
          return kryo.readObject(new Input(bytes), klass);
        }
        finally {
          kryoSerialization.releaseKryo(kryo);
        }
    }

    public void close() throws IOException {
        try {
            if( inputStream != null )
                inputStream.close();
        } finally {
            inputStream = null;
        }
    }
}
