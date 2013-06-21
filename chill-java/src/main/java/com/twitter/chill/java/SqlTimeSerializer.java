package com.twitter.chill.java;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.sql.Time;

public class SqlTimeSerializer extends Serializer<Time> {

    @Override
    public void write(Kryo kryo, Output output, Time time) {
        output.writeLong(time.getTime(), true);
    }

    @Override
    public Time read(Kryo kryo, Input input, Class<Time> timeClass) {
        return new Time(input.readLong(true));
    }
}
