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

import com.twitter.chill.KryoPool;
import com.twitter.chill.KryoInstantiator;
import com.twitter.chill.config.Config;
import com.twitter.chill.config.ConfiguredInstantiator;
import com.twitter.chill.config.ConfigurationException;

import java.io.ByteArrayOutputStream;

public class KryoSerialization extends Configured implements Serialization<Object> {

    KryoPool kryoPool;
    Kryo testKryo;
    protected static int MAX_CACHED_RESOURCE = 100;

    public KryoSerialization() throws ConfigurationException {
        this(new Configuration());
    }

    /**
     * Constructor KryoSerialization creates a new KryoSerialization instance.
     *
     * @param conf of type Configuration
     */
    public KryoSerialization( Configuration conf ) throws ConfigurationException {
        // Hadoop will then call setConf (yay! mutability!)
        super( conf );
    }

    @Override
    public void setConf(Configuration conf) {
      try {
        KryoInstantiator kryoInst = new ConfiguredInstantiator(new HadoopConfig(conf));
        testKryo = kryoInst.newKryo();
        kryoPool = KryoPool.withByteArrayOutputStream(MAX_CACHED_RESOURCE, kryoInst);
      }
      catch(ConfigurationException cx) {
        // This interface can't throw
        throw new RuntimeException(cx);
      }
    }

    /**
     * Uses the initialized Kryo instance from the JobConf to test if Kryo will accept the class
     * @param aClass
     * @return
     */
    public boolean accept(Class<?> aClass) {
        try {
            return (testKryo.getRegistration(aClass) != null);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public Serializer<Object> getSerializer(Class<Object> aClass) {
        return new KryoSerializer(kryoPool);
    }

    public Deserializer<Object> getDeserializer(Class<Object> aClass) {
        return new KryoDeserializer(kryoPool, aClass);
    }
}
