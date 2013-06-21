package com.twitter.chill.hadoop;

import com.esotericsoftware.kryo.Kryo;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.serializer.Deserializer;
import org.apache.hadoop.io.serializer.Serialization;
import org.apache.hadoop.io.serializer.Serializer;
import org.objenesis.strategy.StdInstantiatorStrategy;

public class KryoSerialization extends Configured implements Serialization<Object> {

    Kryo kryo;
    KryoFactory factory;

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
