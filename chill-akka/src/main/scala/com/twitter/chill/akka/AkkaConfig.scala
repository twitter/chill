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

package com.twitter.chill.akka

import com.twitter.chill.config.{ Config => ChillConfig }
import com.typesafe.config.{ Config => TypesafeConfig }
import com.typesafe.config.ConfigFactory

import scala.util.Try

/**
 * Wraps the immutable typesafe.config.Config in a wrapper that
 * keeps track of the state and follows the semantics of ChillConfig
 */
class AkkaConfig(var typesafeConfig: TypesafeConfig) extends ChillConfig {
  /* This is implementing a Java API so that has an assy format */
  def get(key: String) =
    Try(typesafeConfig.getString(key)).toOption.orNull

  def set(key: String, value: String) {
    typesafeConfig = Option(value).map { v =>
      ConfigFactory.parseString("%s = \"%s\"".format(key, v))
        .withFallback(typesafeConfig)
    }
      .getOrElse(typesafeConfig.withoutPath(key))
  }
}
