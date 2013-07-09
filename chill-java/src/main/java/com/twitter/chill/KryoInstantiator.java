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

package com.twitter.chill;

import com.esotericsoftware.kryo.Kryo;
import org.objenesis.strategy.InstantiatorStrategy;

/** Class to create a new Kryo instance.
 * Used in initial configuration or pooling of Kryo objects.
 * These objects are immutable (and hopefully Kryo serializable)
 */
abstract public class KryoInstantiator {
  abstract public Kryo newKryo();

  /** If true, Kryo will error if it sees a class that has not been registered
   */
  public KryoInstantiator setInstantiatorStrategy(final InstantiatorStrategy inst) {
    return new KryoInstantiator() {
      public Kryo newKryo() {
        Kryo k = KryoInstantiator.this.newKryo();
        k.setInstantiatorStrategy(inst);
        return k;
      }
    };
  }

  /** If true, Kryo keeps a map of all the objects it has seen.
   * this can use a ton of memory on hadoop, but save serialization costs in
   * some cases
   */
  public KryoInstantiator setReferences(final boolean ref) {
    return new KryoInstantiator() {
      public Kryo newKryo() {
        Kryo k = KryoInstantiator.this.newKryo();
        k.setReferences(ref);
        return k;
      }
    };
  }

  /** If true, Kryo will error if it sees a class that has not been registered
   */
  public KryoInstantiator setRegistrationRequired(final boolean req) {
    return new KryoInstantiator() {
      public Kryo newKryo() {
        Kryo k = KryoInstantiator.this.newKryo();
        k.setRegistrationRequired(req);
        return k;
      }
    };
  }

  public KryoInstantiator withRegistrar(final IKryoRegistrar r) {
    return new KryoInstantiator() {
      public Kryo newKryo() {
        Kryo k = KryoInstantiator.this.newKryo();
        r.apply(k);
        return k;
      }
    };
  }
}
