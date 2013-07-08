package com.twitter.chill.java;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import com.twitter.chill.IKryoRegistrar;

import java.util.regex.Pattern;

public class RegexSerializer extends Serializer<Pattern> {

    static public IKryoRegistrar registrar() {
      return new IKryoRegistrar() {
        public void apply(Kryo k) {
          k.register(Pattern.class, new RegexSerializer());
        }
      };
    }

    @Override
    public void write(Kryo kryo, Output output, Pattern pattern) {
        output.writeString(pattern.pattern());
    }

    @Override
    public Pattern read(Kryo kryo, Input input, Class<Pattern> patternClass) {
        return Pattern.compile(input.readString());
    }
}
