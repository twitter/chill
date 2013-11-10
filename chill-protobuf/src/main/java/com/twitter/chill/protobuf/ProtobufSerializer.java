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

package com.twitter.chill.protobuf;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import com.google.protobuf.Message;

import java.lang.reflect.Method;

/**
 * Kryo serializer for Protobuf instances.
 *
 * Note that this class is not thread-safe. (Kryo itself is not thread
 * safe, so this shouldn't be a concern.)
 *
 * Use this with
 * addDefaultSerializer(Message.class, ProtobufSerializer.class)
 * It still helps to .register your instances so the full class name
 * does not need to be written.
 */
public class ProtobufSerializer extends Serializer<Message> {

  protected Method getParse(Class cls) throws Exception {
    return cls.getMethod("parseFrom", new Class[]{ byte[].class });
  }

  @Override
  public void write(Kryo kryo, Output output, Message mes) {
    byte[] ser = mes.toByteArray();
    output.writeInt(ser.length, true);
    output.writeBytes(ser);
  }

  @Override
  public Message read(Kryo kryo, Input input, Class<Message> pbClass) {
    try {
      int size = input.readInt(true);
      byte[] barr = new byte[size];
      input.readBytes(barr);
      return (Message)getParse(pbClass).invoke(null, barr);
    } catch (Exception e) {
      throw new RuntimeException("Could not create " + pbClass, e);
    }
  }
}

