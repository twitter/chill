package com.twitter.chill.java;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.ClosureSerializer;
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
      return;
    }
    k.register(ClosureSerializer.class, new ClosureSerializer());
  }
}
