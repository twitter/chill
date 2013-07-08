package com.twitter.chill.java;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import com.twitter.chill.IKryoRegistrar;

import java.sql.Date;

public class SqlDateSerializer extends Serializer<Date> {

    static public IKryoRegistrar registrar() {
      return new IKryoRegistrar() {
        public void apply(Kryo k) {
          k.register(Date.class, new SqlDateSerializer());
        }
      };
    }

    @Override
    public void write(Kryo kryo, Output output, Date date) {
        output.writeLong(date.getTime(), true);
    }

    @Override
    public Date read(Kryo kryo, Input input, Class<Date> dateClass) {
        return new Date(input.readLong(true));
    }
}
