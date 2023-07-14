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

import java.util.regex.Pattern;

public class RegexSerializer extends Serializer<Pattern> {

    static public IKryoRegistrar registrar() {
      return new SingleRegistrar(Pattern.class, new RegexSerializer());
    }

    @Override
    public void write(Kryo kryo, Output output, Pattern pattern) {
        output.writeString(pattern.pattern());
    }

    @Override
    public Pattern read(Kryo kryo, Input input, Class<? extends Pattern> patternClass) {
        return Pattern.compile(input.readString());
    }
}
