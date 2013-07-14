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

package com.twitter.chill.config;
import com.twitter.chill.KryoInstantiator;

import java.lang.reflect.InvocationTargetException;

import com.esotericsoftware.kryo.Kryo;
/**
 * This is the standard Config based KryoInstantiator.
 * It delegates to another KryoInstantiator that is described a Config
 * object. This is either done via reflection or serialization.
 */
public final class ConfiguredInstantiator extends KryoInstantiator {
  protected final KryoInstantiator delegate;

  /** Mode is one of three:
   * reflect: instantiate the named class via new instance. If this class has a constructor
   *   that accepts Config, that is used, else the no-arg constructor.
   *
   * serialized: use a default (new Kryo) to deserialize the instance in base64
   */
  public static final String MODE_KEY = "com.twitter.chill.config.configuredinstantiator.mode";
  public static final String REFLECT_MODE = "reflect";
  public static final String SERIALIZED_MODE = "serialized";

  public static final String VALUE_KEY = "com.twitter.chill.config.configuredinstantiator.value";

  public ConfiguredInstantiator(Config conf) throws ConfigurationException {
    String mode = conf.get(MODE_KEY);
    if(REFLECT_MODE == mode) {
      try { delegate = reflect((Class<? extends KryoInstantiator>)Class.forName(conf.get(VALUE_KEY)), conf); }
      catch(ClassNotFoundException x) {
        throw new ConfigurationException("Could not find class for: " + conf.get(VALUE_KEY), x);
      }
    }
    else if (SERIALIZED_MODE == mode) {
      //Kryo k = new Kryo();
      delegate = deserialize(conf.get(VALUE_KEY));
    }
    else {
      throw new ConfigurationException("Unknown mode: " + mode);
    }
  }

  public Kryo newKryo() { return delegate.newKryo(); }

  public static void setReflect(Config conf, Class<? extends KryoInstantiator> instClass) {
    conf.set(MODE_KEY, REFLECT_MODE);
    conf.set(VALUE_KEY, instClass.getName());
  }

  public static KryoInstantiator reflect(Class<? extends KryoInstantiator> instClass, Config optConf)
    throws ConfigurationException {
    try {
      try {
        return instClass.getConstructor(Config.class).newInstance(optConf);
      } catch (NoSuchMethodException ex3) {
        return instClass.newInstance();
      }
    }
    catch(InstantiationException x) {
      throw new ConfigurationException(x);
    }
    catch(IllegalAccessException x) {
      throw new ConfigurationException(x);
    }
    catch(InvocationTargetException x) {
      throw new ConfigurationException(x);
    }
  }

  public static void setSerialized(Config conf, KryoInstantiator ki) {
    conf.set(MODE_KEY, SERIALIZED_MODE);
    conf.set(VALUE_KEY, serialize(ki));
  }

  public static KryoInstantiator deserialize(String base64Value) {
    throw new RuntimeException("TODO");
  }
  public static String serialize(KryoInstantiator ki) {
    throw new RuntimeException("TODO");
  }
}
