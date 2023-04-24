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

package com.twitter.chill.thrift;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import org.apache.thrift.TBase;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.transport.TTransportException;

/**
 * Kryo serializer for Thrift instances.
 *
 * Note that this class is not thread-safe. (Kryo itself is not thread safe, so
 * this shouldn't be a concern.)
 *
 * Use this with addDefaultSerializer(TBase.class, TBaseSerializer.class) It
 * still helps to .register your instances so the full class name does not need
 * to be written.
 */
public class TBaseSerializer extends Serializer<TBase> {
  private final TSerializer serializer = new TSerializer();
  private final TDeserializer deserializer = new TDeserializer();

  public TBaseSerializer() throws TTransportException {
  }

  @Override
  public void write(Kryo kryo, Output output, TBase tBase) {
    try {
      byte[] serThrift = serializer.serialize(tBase);
      output.writeInt(serThrift.length, true);
      output.writeBytes(serThrift);
    } catch (TException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public TBase read(Kryo kryo, Input input, Class<? extends TBase> tBaseClass) {
    try {
      TBase prototype = tBaseClass.newInstance();
      int tSize = input.readInt(true);
      byte[] barr = new byte[tSize];
      input.readBytes(barr);
      deserializer.deserialize(prototype, barr);
      return prototype;
    } catch (Exception e) {
      throw new RuntimeException("Could not create " + tBaseClass, e);
    }
  }
}
