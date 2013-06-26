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
import com.esotericsoftware.kryo.io.Output;
import org.apache.hadoop.io.serializer.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class KryoSerializer implements Serializer<Object> {
    private final KryoSerialization kryoSerialization;
    private DataOutputStream outputStream;

    public KryoSerializer(KryoSerialization kryoSerialization) {
        this.kryoSerialization = kryoSerialization;
    }

    public void open(OutputStream out) throws IOException {
        if(out instanceof DataOutputStream)
            outputStream = (DataOutputStream)out;
        else
            outputStream = new DataOutputStream(out);
    }

    public void serialize(Object o) throws IOException {
        Output ko =  kryoSerialization.borrowOutput();
        // Clear buffer.
        ko.clear();
        // kryoSerialization ALWAYS puts these types into the buffer
        ByteArrayOutputStream byteStream = (ByteArrayOutputStream)ko.getOutputStream();
        byteStream.reset();
        // Get a Kryo:
        Kryo kryo = kryoSerialization.borrowKryo();
        try {
          kryo.writeObject(ko, o);
          ko.flush();
          // Copy from buffer to output stream.
          outputStream.writeInt(byteStream.size());
          byteStream.writeTo(outputStream);
        }
        finally {
          kryoSerialization.releaseKryo(kryo);
          kryoSerialization.releaseOutput(ko);
        }
        outputStream.flush();
    }

    public void close() throws IOException {
        try {
            if(outputStream != null) {
                outputStream.close();
            }
        } finally {
            outputStream = null;
        }
    }
}
