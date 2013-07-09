package com.twitter.chill.java;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import com.twitter.chill.IKryoRegistrar;
import com.twitter.chill.SingleRegistrar;

import java.util.BitSet;

public class BitSetSerializer extends Serializer<BitSet> {

    static public IKryoRegistrar registrar() {
      return new SingleRegistrar(BitSet.class, new BitSetSerializer());
    }

    @Override
    public void write(Kryo kryo, Output output, BitSet bitSet) {
        int len = bitSet.length();

        output.writeInt(len, true);

        for(int i = 0; i < len; i++) {
            output.writeBoolean(bitSet.get(i));
        }
    }

    @Override
    public BitSet read(Kryo kryo, Input input, Class<BitSet> bitSetClass) {
        int len = input.readInt(true);
        BitSet ret = new BitSet(len);

        for(int i = 0; i < len; i++) {
            ret.set(i, input.readBoolean());
        }

        return ret;
    }
}
