## Chill [![Build Status](https://secure.travis-ci.org/twitter/chill.png)](http://travis-ci.org/twitter/chill)

Scala extensions for the [Kryo serialization library](http://code.google.com/p/kryo/).

Chill provides a a number of Kryo serializers and an Option-like type called the MeatLocker. The MeatLocker allows you to box Kryo-serializable objects and deserialize them lazily on the first call to `get`:

```scala
val boxedItem = MeatLocker(someItem)

// boxedItem is java.io.Serializable no matter what it contains.
val box = roundTripThroughJava(boxedItem)
box.get == boxedItem.get // true!
```

To retrieve the boxed item without caching the deserialized value, use `meatlockerInstance.copy`.

### Handled classes

Chill provides support for singletons, scala Objects and the following types:

* Scala primitives
  * scala.Enumeration values
  * scala.Symbol
  * scala.reflect.Manifest
  * scala.reflect.ClassManifest
* Collections and sequences
  * scala.collection.immutable.Map
  * scala.collection.immutable.List
  * scala.collection.immutable.Vector
  * scala.collection.immutable.Set
  * scala.collection.mutable.{Map, Set, Buffer, WrappedArray}
  * all 22 scala tuples

## Maven

Current version is `0.1.2`. groupid=`"com.twitter"` artifact=`"chill_2.9.2"`.

## Authors

* Oscar Boykin <https://twitter.com/posco>
* Mike Gagnon <https://twitter.com/MichaelNGagnon>
* Sam Ritchie <https://twitter.com/sritchie>

## License

Copyright 2012 Twitter, Inc.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
