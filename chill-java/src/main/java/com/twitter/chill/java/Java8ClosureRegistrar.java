package com.twitter.chill.java;

import com.esotericsoftware.kryo.Kryo;
import com.twitter.chill.IKryoRegistrar;

/**
 * Enables Java 8 lambda serialization if running on Java 8; no-op otherwise.
 */
public class Java8ClosureRegistrar implements IKryoRegistrar {

    private static boolean checkJava8() {
        try {
            Class.forName("java.lang.invoke.SerializedLambda");
            return true;
        } catch (ClassNotFoundException e) {
            // Not running on Java 8.
            return false;
        }
    }
    private static boolean onJava8Field = checkJava8();
    public static boolean areOnJava8() {
      return onJava8Field;
    }

    @Override
    public void apply(Kryo k) {
        if (areOnJava8()) {
          k.register(ClosureSerializer.Closure.class, new ClosureSerializer());
        }
    }
}
