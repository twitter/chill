## Chill [![Build Status](https://secure.travis-ci.org/twitter/chill.png)](http://travis-ci.org/twitter/chill)

Scala extensions for the [Kryo serialization library](http://code.google.com/p/kryo/).

Chill provides a a number of Kryo serializers and an Option-like type called the MeatLocker. The MeatLocker allows you to box Kryo-serializable objects and deserialize them lazily on the first call to `get`:

```scala
import com.twitter.chill.MeatLocker

val boxedItem = MeatLocker(someItem)

// boxedItem is java.io.Serializable no matter what it contains.
val box = roundTripThroughJava(boxedItem)
box.get == boxedItem.get // true!
```

To retrieve the boxed item without caching the deserialized value, use `meatlockerInstance.copy`.

To serialize to bytes and deserialize from bytes:

```scala
import com.twitter.chill.KryoInjection

val bytes:  Array[Byte]    = KryoInjection(someItem)
val option: Option[AnyRef] = KryoInjection.invert(bytes)  // None is returned on failure
```

To deserialize from java.io.InputStream, or java.nio.ByteBuffer:

```scala
val kryo = KryoBijection.getKryo
val rich = new RichKryo(kryo)

val option1 = rich.fromInputStream(myInputStream)
val option2 = rich.fromByteBuffer(myByteBuffer)
```

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

Chill modules are available on Maven Central. The current groupid and version for all modules is, respectively, `"com.twitter"` and  `0.2.3`.

Current published artifacts are

* `chill-_2.9.2`
* `chill_2.10`
* `chill-java_2.9.2`
* `chill-java_2.10`
* `chill-storm_2.9.2`
* `chill-storm_2.10`
* `chill-hadoop_2.9.2`
* `chill-hadoop_2.10`
* `chill-akka_2.9.2`
* `chill-akka_2.10`

The suffix denotes the scala version.

## Authors

* Oscar Boykin <https://twitter.com/posco>
* Mike Gagnon <https://twitter.com/MichaelNGagnon>
* Sam Ritchie <https://twitter.com/sritchie>

## License

Copyright 2012 Twitter, Inc.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
