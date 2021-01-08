package com.twitter.chill.java;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.serializers.ClosureSerializer;
import com.twitter.chill.IKryoRegistrar;

/**
 * Enables Java 8 lambda serialization if running on Java 8; no-op otherwise.
 */
public class Java8ClosureRegistrar implements IKryoRegistrar {

    @Override
    public void apply(Kryo k) {
        try {
            Class.forName("java.lang.invoke.SerializedLambda");
        } catch (ClassNotFoundException e) {
            // Not running on Java 8.
            return;
        }
        k.register(ClosureSerializer.Closure.class, new ClosureSerializer());
    }
}
