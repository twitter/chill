## Chill [![Build Status](https://secure.travis-ci.org/twitter/chill.png)](http://travis-ci.org/twitter/chill)

Scala extensions for the [Kryo serialization library](http://code.google.com/p/kryo/).

Chill provides a a number of Kryo serializers and an Option-like type called the MeatLocker. The MeatLocker allows you to box Kryo-serializable objects and deserialize them lazily on the first call to `get`:

```scala
val boxedCodec = new MeatLocker(new LongCodec)

// boxedCodec is java.io.Serializable no matter what it contains.
val codec = roundTripThroughJava(boxedCodec)
val encoded = codec.get.encode(1L)
val decoded = codec.get.decode(encoded)
decoded == 1L // true
```

### Handled classes

Chill provides support for singletons, scala Objects and the following types:

* Scala primitives
  * scala.Symbol
* Collections and sequences
  * scala.collection.immutable.Map
  * scala.collection.immutable.List
  * scala.collection.immutable.Vector
  * scala.collection.immutable.Set

## Maven

Current version is 0.0.2. groupid="com.twitter" artifact="chill_2.9.2".

## Authors

* Oscar Boykin <http://twitter.com/posco>
* Sam Ritchie <http://twitter.com/sritchie>

## License

Copyright 2012 Twitter, Inc.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
