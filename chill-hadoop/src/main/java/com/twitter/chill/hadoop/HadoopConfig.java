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

package com.twitter.chill.hadoop;
import com.twitter.chill.config.Config;

import org.apache.hadoop.conf.Configuration;

/**
 * Adapt Configuration to be used with Chill
 */
public class HadoopConfig extends Config {

  final Configuration conf;
  public HadoopConfig(Configuration conf) { this.conf = conf; }
  @Override
  public String get(String key) { return conf.get(key); }
  @Override
  public void set(String key, String value) { conf.set(key, value); }
}
