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
import com.esotericsoftware.kryo.serializers.FieldSerializer

import com.twitter.chill.IKryoRegistrar

import com.twitter.algebird.{
  AveragedValue,
  DecayedValue,
  HLL,
  HyperLogLog,
  HyperLogLogMonoid,
  Moments,
  QTree,
  SpaceSaver,
  DenseVector,
  SparseVector,
  AdaptiveVector
}

class AlgebirdRegistrar extends IKryoRegistrar {

  def apply(k: Kryo) {
    // Some of the monoids from Algebird that we use:
    k.register(classOf[AveragedValue], new AveragedValueSerializer)
    k.register(classOf[DecayedValue], new DecayedValueSerializer)
    k.register(classOf[HyperLogLogMonoid], new HLLMonoidSerializer)
    k.register(classOf[Moments], new MomentsSerializer)
    k.register(classOf[QTree[Any]], new QTreeSerializer)
    k.addDefaultSerializer(classOf[HLL], new HLLSerializer)

    /**
     * AdaptiveVector is IndexedSeq, which picks up the chill IndexedSeq serializer
     * (which is its own bug), force using the fields serializer here
     */
    k.register(classOf[DenseVector[_]], new FieldSerializer[DenseVector[_]](k, classOf[DenseVector[_]]))

    k.register(classOf[SparseVector[_]], new FieldSerializer[SparseVector[_]](k, classOf[SparseVector[_]]))

    k.addDefaultSerializer(classOf[AdaptiveVector[_]], classOf[FieldSerializer[_]])
  }
}
