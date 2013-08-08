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
import com.twitter.chill.Base64;

import java.lang.reflect.InvocationTargetException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * This is the standard Config based KryoInstantiator.
 * It delegates to another KryoInstantiator that is described a Config
 * object. This is either done via reflection or reflection AND serialization.
 *
 * If the KEY is not set, the delegate is the default: new KryoInstantiator()
 *
 * In the case of reflection, the class name of the delegate instantiator is given.
 *
 * In the case of serialization, we first reflect to create the KryoInstatiator
 * we use to get the Kryo we need to deserialize.
 */
public class ConfiguredInstantiator extends KryoInstantiator {
  protected final KryoInstantiator delegate;

  /** Key we use to configure this class.
   * Format: <class of KryoInstantiator>(:<base64 serialized instantiator>)
   * if there is no serialized instantiator, we use the reflected instance
   * as the delegate
   */
  public static final String KEY = "com.twitter.chill.config.configuredinstantiator";

  public ConfiguredInstantiator(Config conf) throws ConfigurationException {
    String key = conf.get(KEY);
    if (null == key) {
      delegate = new KryoInstantiator();
    }
    else {
      String[] parts = key.split(":");
      if(parts.length != 1 && parts.length != 2) {
        throw new ConfigurationException("Invalid Config Key: " + conf.get(KEY));
      }
      KryoInstantiator reflected = null;
      try { reflected = reflect((Class<? extends KryoInstantiator>)Class.forName(parts[0]), conf); }
      catch(ClassNotFoundException x) {
        throw new ConfigurationException("Could not find class for: " + parts[0], x);
      }

      if(parts.length == 2) {
        delegate = deserialize(reflected.newKryo(), parts[1]);
        if(null == delegate) {
          throw new ConfigurationException("Null delegate from: " + parts[1]);
        }
      }
      else {
        delegate = reflected;
      }
    }
  }

  /** Calls through to the delegate */
  public Kryo newKryo() { return delegate.newKryo(); }

  /** Return the delegated KryoInstantiator */
  public KryoInstantiator getDelegate() {
    return delegate;
  }

  /** In this mode, we are just refecting to another delegated class. This is preferred
   * if you don't have any configuration to do at runtime (i.e. you can make a named class
   * that has all the logic for your KryoInstantiator).
   */
  public static void setReflect(Config conf, Class<? extends KryoInstantiator> instClass) {
    conf.set(KEY, instClass.getName());
  }

  /** This instantiates a KryoInstantiator by:
   * 1) checking if it has a constructor that takes Config
   * 2) checking for a no-arg constructor
   */
  static KryoInstantiator reflect(Class<? extends KryoInstantiator> instClass, Config optConf)
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

  /** Use the default KryoInstantiator to serialize the KryoInstantiator ki
   * same as: setSerialized(conf, KryoInstantiator.class, ki)
   */
  public static void setSerialized(Config conf, KryoInstantiator ki)
    throws ConfigurationException {
    setSerialized(conf, KryoInstantiator.class, ki);
  }

  /** If this reflector needs config to be set, that should be done PRIOR to making this call.
   * This mode serializes an instance (ki) to be used as the delegate.
   * Only use this mode if reflection alone will not work.
   */
  public static void setSerialized(Config conf, Class<? extends KryoInstantiator> reflector, KryoInstantiator ki)
    throws ConfigurationException {
    KryoInstantiator refki = reflect(reflector, conf);
    String kistr = serialize(refki.newKryo(), ki);
    // Verify, that deserialization works:
    deserialize(refki.newKryo(), kistr); // ignore the result, just see if it throws
    conf.set(KEY, reflector.getName() + ":" + kistr);
  }

  protected static KryoInstantiator deserialize(Kryo k, String base64Value) throws ConfigurationException {
    try {
      return (KryoInstantiator)k.readClassAndObject(new Input(Base64.decode(base64Value)));
    }
    catch(java.io.IOException iox) {
      throw new ConfigurationException("could not deserialize: " + base64Value, iox);
    }
  }
  protected static String serialize(Kryo k, KryoInstantiator ki) {
    Output out = new Output(1 << 10, 1 << 19); // 1 MB in config is too much
    k.writeClassAndObject(out, ki);
    return Base64.encodeBytes(out.toBytes());
  }
}
