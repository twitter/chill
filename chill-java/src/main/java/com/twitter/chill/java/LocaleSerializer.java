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

package com.twitter.chill.java;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import com.twitter.chill.IKryoRegistrar;
import com.twitter.chill.SingleRegistrar;

import java.util.Locale;

public class LocaleSerializer extends Serializer<Locale> {

   static public IKryoRegistrar registrar() {
      return new SingleRegistrar(Locale.class, new LocaleSerializer());
    }

    @Override
    public void write(Kryo kryo, Output output, Locale loc) {
        output.writeString(loc.getLanguage());
        output.writeString(loc.getCountry());
        output.writeString(loc.getVariant());
    }

    @Override public Locale read(Kryo kryo, Input input, Class<Locale> locClass) {
        return new Locale(input.readString(), input.readString(), input.readString());
    }
}
