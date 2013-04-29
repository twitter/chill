# chill #

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
