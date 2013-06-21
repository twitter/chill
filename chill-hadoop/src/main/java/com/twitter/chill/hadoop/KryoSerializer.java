package com.twitter.chill.hadoop;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import org.apache.hadoop.io.serializer.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class KryoSerializer implements Serializer<Object> {
    private Kryo kryo;
    private final KryoSerialization kryoSerialization;
    private DataOutputStream outputStream;
    private ByteArrayOutputStream byteStream;

    public KryoSerializer(KryoSerialization kryoSerialization) {
        this.kryoSerialization = kryoSerialization;
    }

    public void open(OutputStream out) throws IOException {
        kryo = kryoSerialization.populatedKryo();
        byteStream = new ByteArrayOutputStream();
        if(out instanceof DataOutputStream)
            outputStream = (DataOutputStream)out;
        else
            outputStream = new DataOutputStream(out);
    }

    public void serialize(Object o) throws IOException {
        // Clear buffer.
        byteStream.reset();
        // Write output to temorary buffer.
        Output ko = new Output(byteStream);
        kryo.writeObject(ko, o);
        ko.flush();
        // Copy from buffer to output stream.
        outputStream.writeInt(byteStream.size());
        byteStream.writeTo(outputStream);
        outputStream.flush();
    }

    // TODO: Bump the kryo version, add a kryo.reset();
    public void close() throws IOException {
        try {
            if(outputStream != null) {
                outputStream.close();
            }
        } finally {
            outputStream = null;
            byteStream = null;
            kryo = null;
        }
    }
}
