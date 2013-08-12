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

import com.twitter.chill.config.{Config => ChillConfig}
import com.typesafe.config.{Config => TypesafeConfig}
import com.typesafe.config.ConfigFactory

/** Wraps the immutable typesafe.config.Config in a wrapper that
 * keeps track of the state and follows the semantics of ChillConfig
 */
class AkkaConfig(var typesafeConfig: TypesafeConfig = ConfigFactory.empty) extends ChillConfig {
  def get(key: String) = try { typesafeConfig.getString(key) } catch { case _: Throwable => null }
  def set(key: String, value: String) {
    if(value != null) {
      typesafeConfig =
        ConfigFactory.parseString("%s = \"%s\"".format(key, value))
          .withFallback(typesafeConfig)
    }
    else {
      typesafeConfig = typesafeConfig.withoutPath(key)
    }
  }
}
