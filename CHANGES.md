# chill #

### 0.7.2 ###
* Setting the class loader by default: https://github.com/twitter/chill/pull/242

### 0.7.1 ###
* Add testing support for Java 6, 7 and 8.
* Adds two Injection's to lift T into an Externalizer of T.
* Protobuf module is Java only.

### 0.7.0 ###
* Adds Scrooge 2.11 support now that the artifact is out, sets up for c…: https://github.com/twitter/chill/pull/236
* make chill 1.6 compatible: https://github.com/twitter/chill/pull/231
* Update README.md: https://github.com/twitter/chill/pull/232
* Alter scala collection default serializer registration order: https://github.com/twitter/chill/pull/229
* Build chill-scrooge for scala 2.11, too: https://github.com/twitter/chill/pull/219

### 0.6.0 ###
* Add build instructions to readme and make InjectiveSerializer serializable #216
* Build chill-scrooge for scala 2.11, too #219
* Rewrite Java BitSet serializer to make it more efficient #220
* Bijection 0.8.0, algebird 0.10.0, scala 2.10.5 #228

### 0.5.2 ###
* Use new Travis CI infrastructure #210
* Optimizations for ConfiguredInstantiator. #213
* Upgrade to algebird 0.9.0, bijection 0.7.2, scala 2.11.5 #214

### 0.5.1
* reverse Config contains logic: https://github.com/twitter/chill/pull/205
* fix setConf: https://github.com/twitter/chill/pull/204
* fix default constructor for kryo serialization: https://github.com/twitter/chill/pull/203
* Switched Chill Avro to use ClassTags, added Java unit tests: https://github.com/twitter/chill/pull/200
* Enable cross compilation for chill-avro: https://github.com/twitter/chill/pull/202

### 0.5.0
* Move to211: https://github.com/twitter/chill/pull/197
* Make 2.10.4 the default, move to scalatest: https://github.com/twitter/chill/pull/196

### 0.4.0
* Support serializing asJavaIterable.: https://github.com/twitter/chill/pull/192
* Remove a deprecation, that really should never have been there: https://github.com/twitter/chill/pull/189
* Use scalariform: https://github.com/twitter/chill/pull/188
* README: Update Kryo project URL.: https://github.com/twitter/chill/pull/187
* support mutable BitSet: https://github.com/twitter/chill/pull/185
* Make chill-avro work with generic records: https://github.com/twitter/chill/pull/184
* updating akka dependency to 2.3.2: https://github.com/twitter/chill/pull/182
* add chill-algebird project by copying AlgebirdSerializers from of scaldi...: https://github.com/twitter/chill/pull/177
* Scrooge serializer: https://github.com/twitter/chill/pull/178
* Use shaded asm classes provided by Kryo.: https://github.com/twitter/chill/pull/175

### 0.3.6
* Add ScalaAnyRefMapConfig, deals with non-string keys in cascading: https://github.com/twitter/chill/pull/174
* added AvroSerializer: https://github.com/twitter/chill/pull/172
* Add sbt script for homogeneity with SB, Scalding, etc: https://github.com/twitter/chill/pull/170
* Support for akka 2.2.1: https://github.com/twitter/chill/pull/169
* Protobuf should be in the all target: https://github.com/twitter/chill/pull/168
* Enable getClass equality checking in tests: https://github.com/twitter/chill/pull/165
* Add chill-protobuf, with tests: https://github.com/twitter/chill/pull/163
* support serialization of scala sortedmap: https://github.com/twitter/chill/pull/162

### 0.3.5
* Add Serializers for scala SortedList and ListSet: https://github.com/twitter/chill/pull/152
* Fix Range serialization (and remove broken subclass-serialization of Iterable, Seq, etc): https://github.com/twitter/chill/pull/154
* Build and test chill-akka for 2.10: https://github.com/twitter/chill/pull/155
* Add chill-thrift: https://github.com/twitter/chill/pull/156
* Support JavaConverter-built classes: https://github.com/twitter/chill/pull/159
* Back to 2.21: https://github.com/twitter/chill/pull/157
* Adds a test from issue #8: https://github.com/twitter/chill/pull/158

### 0.3.4
* Bugfixes for Externalizer with looped object graphs https://github.com/twitter/chill/pull/143

### 0.3.3
* Serialize synthetic fields by default: https://github.com/twitter/chill/pull/135
* Prefer Java to Kryo, but check both in Externalizer: https://github.com/twitter/chill/pull/138

### 0.3.2

* Add a LocaleSerializer to chill-java: https://github.com/twitter/chill/pull/128
* chill-akka module: https://github.com/twitter/chill/pull/116
* Community section to README (with a mailing list!) https://github.com/twitter/chill/pull/127
* SingletonSerializer for (): https://github.com/twitter/chill/pull/130
* Add Externalizer (fault-tolerant meatlocker): https://github.com/twitter/chill/pull/126
* Use References in Externalizer: https://github.com/twitter/chill/pull/131

### 0.3.1

* Issue 115: fix for wrapped Arrays with non-primitives

### 0.3.0
* Lots of refactoring around configuration (see chill.config in chill-java)
* Issue #94 KryoPool for pooling Kryo instances and Output buffers
* Issue #85 upgrade to bijection 0.5.2 and scala 2.9.3
* Issue #78 Some, Left, Right serializers in chill(-scala)
* Issue #70 create chill-java for Java only code
* Issue #67 add chill-storm
* Issue #65, #71 import cascading.kryo as chill-hadoop
* Issue #64 fix KryoInjection to be from Any to Array[Byte]
* Issues #61, #63 handle the case of small Map/Set in scala
* Issue #57 use CanBuildFrom in Traversable serialization

Contributors:

94 commits
* P. Oscar Boykin: 46 commits
* Sam Ritchie: 38 commits
* ryanlecompte: 5 commits
* Ngoc Dao: 2 commits
* Dao Ngoc: 1 commits
* Michael Schmitz: 1 commits
* Julien Le Dem: 1 commits

### 0.2.3
* Update to Kryo 2.21
* Update to Bijection 0.4.0
* Improve Traversable Serializer

### 0.2.2

* Custom RegexSerializer
* ArrayBuffer serialization fix (#48)

### 0.2.1
* Improve MeatLocker (safe to call get before serialization)
* Fix a bug with serialization of mutable collections
* Reorganize source for multiple subprojects

### 0.2.0

* Upgrade chill to work with scala 2.10.
* Upgrade to Bijection 0.3.0. This release gained the Injection type, so the BijectiveSerializer is now an InjectiveSerializer.
* Remove KryoSerializer trait in favor of `KryoBijection` and `KryoInjection`.

### 0.1.4
* Serious bugfix with serialization of recursive types.

### 0.1.3
* Improved serialization of scala BitSet

### 0.1.2
* Fixed a long standing bug with serializing some anonymous functions as vals in traits

### 0.1.1

* adds RichKryo enrichment with many methods simplifying adding Serializers.
* adds ability to serialize any Traversable with a Builder.
* support for mutable scala collections.
* adds support for bijection.Bufferable as a serializer.

### 0.1.0

* adds `BijectiveSerializer` and `BijectivePair` with accompanying registrations in `KryoSerializer`
* adds Enumeration support

### 0.0.5

* ClosureCleaner and Specs (Thanks to the Spark project!)

### 0.0.4

* Added "copy" method to the MeatLocker

### 0.0.3

* Added Manifest serializations
* Added Traversable serializer
* Added KryoSerializer object for easy registration

### 0.0.2

* Added TupleSerializers for all tuple types.

### 0.0.1

* Added serializers for collection types and MeatLocker.
