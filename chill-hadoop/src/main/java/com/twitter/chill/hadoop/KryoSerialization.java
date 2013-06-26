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

package com.twitter.chill.hadoop;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.serializer.Deserializer;
import org.apache.hadoop.io.serializer.Serialization;
import org.apache.hadoop.io.serializer.Serializer;
import org.objenesis.strategy.StdInstantiatorStrategy;

import com.twitter.chill.java.ResourcePool;

import java.io.ByteArrayOutputStream;

public class KryoSerialization extends Configured implements Serialization<Object> {

    Kryo kryo;
    KryoFactory factory;

    final ResourcePool<Kryo> kryoPool;
    final ResourcePool<Output> outputPool;

    public KryoSerialization() {
        this(new Configuration());
    }

    /**
     * Constructor KryoSerialization creates a new KryoSerialization instance.
     *
     * @param conf of type Configuration
     */
    public KryoSerialization( Configuration conf ) {
        super( conf );
        int MAX_CACHED_RESOURCE = 100;
        kryoPool = new ResourcePool<Kryo>(MAX_CACHED_RESOURCE) {
          protected Kryo newInstance() {
            return populatedKryo();
          }
        };

        outputPool = new ResourcePool<Output>(MAX_CACHED_RESOURCE) {
          protected Output newInstance() {
            return new Output(new ByteArrayOutputStream());
          }
        };
    }

    /**
     * Mutate the given instance (add custom serializers)
     * This is called BEFORE the factory adds serializers
     */
    public void decorateKryo(Kryo k) { }

    /**
     * override this to implement your own subclass of Kryo
     * default is new Kryo with StdInstantiatorStrategy.
     */
    public Kryo newKryo() {
      Kryo k = new Kryo();
      k.setInstantiatorStrategy(new StdInstantiatorStrategy());
      return k;
    }

    public final Kryo populatedKryo() {
        if (factory == null)
            factory = new KryoFactory(getConf());

        Kryo k = newKryo();
        decorateKryo(k);
        factory.populateKryo(k);
        return k;
    }

    public final Kryo borrowKryo() { return kryoPool.borrow(); }

    public final void releaseKryo(Kryo k) { kryoPool.release(k); }

    public final Output borrowOutput() { return outputPool.borrow(); }

    public final void releaseOutput(Output o) {
      // Clear buffer.
      o.clear();
      ByteArrayOutputStream byteStream = (ByteArrayOutputStream)o.getOutputStream();
      byteStream.reset();

      outputPool.release(o);
    }

    /**
     * Initializes Kryo instance from the JobConf on the first run. If the ACCEPT_ALL key in
     * the JobConf has been set to true, Kryo will return yes for everything; else, Kryo will only
     * return true for classes with explicitly registered serializations.
     * @param aClass
     * @return
     */
    public boolean accept(Class<?> aClass) {
        if (kryo == null)
            kryo = populatedKryo();
        try {
            return (kryo.getRegistration(aClass) != null);
        } catch (IllegalArgumentException e) {
            return factory.getAcceptAll();
        }
    }

    public Serializer<Object> getSerializer(Class<Object> aClass) {
        return new KryoSerializer(this);
    }

    public Deserializer<Object> getDeserializer(Class<Object> aClass) {
        return new KryoDeserializer(this, aClass);
    }
}
