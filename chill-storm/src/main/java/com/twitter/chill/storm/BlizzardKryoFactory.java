/*
Copyright 2012 Twitter, Inc.

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

package com.twitter.chill.storm;

import org.apache.storm.serialization.IKryoFactory;
import com.esotericsoftware.kryo.kryo5.Kryo;
import java.util.Map;

import com.twitter.chill.KryoInstantiator;
import com.twitter.chill.config.JavaMapConfig;
import com.twitter.chill.config.ConfiguredInstantiator;
import com.twitter.chill.config.ConfigurationException;

/** BlizzardKryoFactory. get it? chilly storm.
 * This is a thin wrapper for using the ConfiguredInstantiator. It
 * creates the entire kryo object in the getKryo method and then
 * doesn't do anything else.
 *
 * To set this up, you probably want to use ConfiguredInstantitator with
 * the JavaMapConfig.
 */
public class BlizzardKryoFactory implements IKryoFactory {
  public Kryo getKryo(Map conf) {
    KryoInstantiator kryoInst;
    try {
      kryoInst = new ConfiguredInstantiator(new JavaMapConfig(conf));
      return kryoInst.newKryo();
    }
    catch(ConfigurationException cx) { throw new RuntimeException(cx); }
  }
  public void preRegister(Kryo k, Map conf) { }
  public void postRegister(Kryo k, Map conf) { }
  public void postDecorate(Kryo k, Map conf) { }
}
