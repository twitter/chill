package com.twitter.chill.java;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import com.twitter.chill.IKryoRegistrar;
import com.twitter.chill.SingleRegistrar;

import java.util.UUID;

public class UUIDSerializer extends Serializer<UUID> {

   static public IKryoRegistrar registrar() {
      return new SingleRegistrar(UUID.class, new UUIDSerializer());
    }

    @Override
    public void write(Kryo kryo, Output output, UUID uuid) {
        output.writeLong(uuid.getMostSignificantBits(), false);
        output.writeLong(uuid.getLeastSignificantBits(), false);
    }

    @Override public UUID read(Kryo kryo, Input input, Class<UUID> uuidClass) {
        return new UUID(input.readLong(false), input.readLong(false));
    }
}
