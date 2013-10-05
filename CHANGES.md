# chill #

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
