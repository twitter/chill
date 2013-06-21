package com.twitter.chill.java;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.regex.Pattern;

public class RegexSerializer extends Serializer<Pattern> {

    @Override
    public void write(Kryo kryo, Output output, Pattern pattern) {
        output.writeString(pattern.pattern());
    }

    @Override
    public Pattern read(Kryo kryo, Input input, Class<Pattern> patternClass) {
        return Pattern.compile(input.readString());
    }
}
