package com.twitter.chill.java;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.net.URI;

public class URISerializer extends Serializer<java.net.URI> {

    @Override
    public void write(Kryo kryo, Output output, URI uri) {
        output.writeString(uri.toString());
    }

    @Override
    public URI read(Kryo kryo, Input input, Class<URI> uriClass) {
        return URI.create(input.readString());
    }
}
