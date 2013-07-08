package com.twitter.chill.java;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import com.twitter.chill.IKryoRegistrar;

import java.sql.Timestamp;

public class TimestampSerializer extends Serializer<Timestamp> {

    static public IKryoRegistrar registrar() {
      return new IKryoRegistrar() {
        public void apply(Kryo k) {
          k.register(Timestamp.class, new TimestampSerializer());
        }
      };
    }

    @Override
    public void write(Kryo kryo, Output output, Timestamp timestamp) {
        output.writeLong(timestamp.getTime(), true);
        output.writeInt(timestamp.getNanos(), true);
    }

    @Override
    public Timestamp read(Kryo kryo, Input input, Class<Timestamp> timestampClass) {
        Timestamp ts = new Timestamp(input.readLong(true));
        ts.setNanos(input.readInt(true));
        return ts;
    }
}
