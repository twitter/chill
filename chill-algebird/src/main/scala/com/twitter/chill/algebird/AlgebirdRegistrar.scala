/*
Copyright 2014 Twitter, Inc.

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
package com.twitter.chill.algebird

import com.esotericsoftware.kryo.Kryo
import com.twitter.chill.{ RichKryo, IKryoRegistrar }

import com.twitter.algebird.{AveragedValue, DecayedValue, HLL, HyperLogLog,
  HyperLogLogMonoid, Moments, SpaceSaver}

object AlgebirdRegistrar {
  implicit def toRich(k: Kryo): RichKryo = new RichKryo(k)
}

class AlgebirdRegistrar extends IKryoRegistrar {
  import AlgebirdRegistrar._

  def apply(k: Kryo) {
    k
      .forClass[AveragedValue](new AveragedValueSerializer)
      .forClass[Moments](new MomentsSerializer)
      .forClass[DecayedValue](new DecayedValueSerializer)
      .forSubclass[HLL](new HLLSerializer)
      .forClass[HyperLogLogMonoid](new HLLMonoidSerializer())
      //.forSubclass[SpaceSaver[Any]](new SpaceSaverSerializer[Any])
  }
}
