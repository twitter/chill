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

package com.twitter.chill.config

import org.specs._

import com.twitter.chill._

class TestInst extends KryoInstantiator { override def newKryo = sys.error("blow up") }

class ReflectingInstantiatorTest extends Specification {
  "A ConfiguredInstantiator" should {
    "work with a reflected instantiator" in {
      val conf = new JavaMapConfig
      ConfiguredInstantiator.setReflect(conf, classOf[TestInst])
      conf.get(ConfiguredInstantiator.KEY) must be_==(classOf[TestInst].getName)
      val cci = new ConfiguredInstantiator(conf)
      cci.getDelegate.getClass == classOf[TestInst] must beTrue
    }
    "work with a serialized instantiator" in {
      val conf = new JavaMapConfig
      ConfiguredInstantiator.setSerialized(conf, new TestInst)
      val cci = new ConfiguredInstantiator(conf)
      // Here is the only assert:
      cci.getDelegate.getClass == classOf[TestInst] must beTrue
    }
  }
}
