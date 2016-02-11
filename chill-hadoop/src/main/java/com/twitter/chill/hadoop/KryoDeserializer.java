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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.io.serializer.Deserializer;

import com.twitter.chill.KryoPool;
import com.twitter.chill.SerDeState;

public class KryoDeserializer implements Deserializer<Object> {

    private final KryoPool kryoPool;
    private final Class<Object> klass;

    private DataInputStream inputStream;

    public KryoDeserializer(KryoPool kp, Class<Object> klass) {
        this.kryoPool = kp;
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
        byte[] bytes = new byte[Varint.readUnsignedVarInt(inputStream)];
        inputStream.readFully( bytes );
        return kryoPool.fromBytes(bytes, klass);
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
