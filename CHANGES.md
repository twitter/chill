# chill #

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
