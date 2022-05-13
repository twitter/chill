## Chill

![build](https://github.com/twitter/chill/workflows/ci/badge.svg)
[![Codecov branch](https://img.shields.io/codecov/c/github/twitter/chill/develop.svg?maxAge=3600)](https://codecov.io/github/twitter/chill)
[![Latest version](https://index.scala-lang.org/twitter/chill/chill/latest.svg?color=orange)](https://index.scala-lang.org/twitter/chill/chill)
[![Chat](https://badges.gitter.im/twitter/chill.svg)](https://gitter.im/twitter/chill?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Extensions for the [Kryo serialization library](https://github.com/EsotericSoftware/kryo) including
serializers and a set of classes to ease configuration of Kryo in systems like Hadoop, Storm,
Akka, etc.

### Compatibility

Serialization compatibility is **NOT** guaranteed between releases, and for this reason, we don't recommend using it for long-term storage. Serialization is highly dependent on scala version compatibility and on the underlying Kryo serializers, which take different approaches to compatibility.

### Building Chill

```bash
> sbt
sbt:chill-all>  compile # to build chill
sbt:chill-all>  publishM2 # to publish chill to your local .m2 repo
sbt:chill-all>  publishLocal # publish to local ivy repo.
```

Chill has a set of subprojects: chill-java, chill-hadoop, chill-storm and chill-scala.  Other than
chill-scala, all these projects are written in Java so they are easy to use on any JVM platform.

## Chill-Java

The chill-java package includes the `KryoInstantiator` class (factory for Kryo instances)
and the `IKryoRegistrar` interface (adds Serializers to a given Kryo). These two are composable
to build instantiators that create instances of Kryo that have the options and serializers you
need. The benefit of this over a direct Kryo instance is that a Kryo instance is mutable and not
serializable, which limits the safety and reusability of code that works directly with them.

To deserialize or serialize easily, look at `KryoPool`:

```java
int POOL_SIZE = 10;
KryoPool kryo = KryoPool.withByteArrayOutputStream(POOL_SIZE, new KryoInstantiator());
byte[] ser = kryo.toBytesWithClass(myObj);
Object deserObj = kryo.fromBytes(myObj);
```

The KryoPool is a thread-safe way to share Kryo instances and temporary output buffers.

### Chill Config

Hadoop, Storm, and Akka all use a configuration that is basically equivalent to a `Map[String,
String]`. The `com.twitter.chill.config` package makes it easy to build up `KryoInstantiator`
instances given a Config instance, which is an abstract class acting as a thin wrapper over
whatever configuration data the system, such as Hadoop, Storm or Akka, might give.

To configure a KryoInstantiator use `ConfiguredInstantiator` with either reflection,
which takes a class name and instantiates that KryoInstantiator, or an instance of KryoInstantiator
and serializes that instance to use later:
```scala
class TestInst extends KryoInstantiator { override def newKryo = sys.error("blow up") }

// A new Config:
val conf = new JavaMapConfig
// Set-up class-based reflection of our instantiator:
ConfiguredInstantiator.setReflect(conf, classOf[TestInst])
val cci = new ConfiguredInstantiator(conf)
cci.newKryo // uses TestInst
//Or serialize a particular instance into the config to use later (or another node):

ConfiguredInstantiator.setSerialized(conf, new TestInst)
val cci2 = new ConfiguredInstantiator(conf)
cci2.newKryo // uses the particular instance we passed above
```

## Chill in Scala

Scala classes often have a number of properties that distinguish them from usual Java classes. Often
scala classes are immutable, and thus have no zero argument constructor. Secondly, `object` in scala is
a singleton that needs to be carefully serialized. Additionally, scala classes often have synthetic
(compiler generated) fields that need to be serialized, and by default Kryo does not serialize
those.

In addition to a `ScalaKryoInstantiator` which generates Kryo instances with options suitable for
scala, chill provides a number of Kryo serializers for standard scala classes (see below).

### The MeatLocker

Many existing systems use Java serialization. MeatLocker is an object that wraps a given instance
using Kryo serialization internally, but the MeatLocker itself is Java serializable.
The MeatLocker allows you to box Kryo-serializable objects and deserialize them lazily on the first call to `get`:

```scala
import com.twitter.chill.MeatLocker

val boxedItem = MeatLocker(someItem)

// boxedItem is java.io.Serializable no matter what it contains.
val box = roundTripThroughJava(boxedItem)
box.get == boxedItem.get // true!
```

To retrieve the boxed item without caching the deserialized value, use `meatlockerInstance.copy`.

### Serializers for Scala classes

These are found in the `chill-scala` directory in the chill jar (originally this project was
only scala serializers).  Chill provides support for singletons, scala Objects and the following types:

* Scala primitives
  * scala.Enumeration values
  * scala.Symbol
  * scala.reflect.Manifest
  * scala.reflect.ClassManifest
  * scala.Function[0-22] closure cleaning (removing unused `$outer` references).
* Collections and sequences
  * scala.collection.immutable.Map
  * scala.collection.immutable.List
  * scala.collection.immutable.Vector
  * scala.collection.immutable.Set
  * scala.collection.mutable.{Map, Set, Buffer, WrappedArray}
  * all 22 scala tuples

## Chill-bijection

[Bijections and Injections](https://github.com/twitter/bijection) are useful when considering serialization. If you have an Injection from `T` to `Array[Byte]` you have a serialization.  Additionally, if you have a Bijection between `A` and `B`, and a serialization for `B`, then you have a serialization for `A`.  See `BijectionEnrichedKryo` for easy interop between bijection and chill.

### KryoInjection: easy serialization to byte Arrays

KryoInjection is an injection from `Any` to `Array[Byte]`. To serialize using it:

```scala
import com.twitter.chill.KryoInjection

val bytes:  Array[Byte]    = KryoInjection(someItem)
val tryDecode: scala.util.Try[Any] = KryoInjection.invert(bytes)
```

KryoInjection can be composed with Bijections and Injections from `com.twitter.bijection`.

## Chill-Akka

To use, add a key to your config like:
```
    akka.actor.serializers {
      kryo = "com.twitter.chill.akka.AkkaSerializer"
    }
```

Then for the super-classes of all your message types, for instance, `java.io.Serializable` (all case classes and case objects are serializable), write:
```scala
   akka.actor.serialization-bindings {
     "java.io.Serializable" = kryo
   }
```

With this in place you can now [disable Java serialization entirely](https://doc.akka.io/docs/akka/current/remoting.html#disable-java-serializer):

```scala
akka.actor {
  # Set this to on to enable serialization-bindings defined in
  # additional-serialization-bindings. Those are by default not included
  # for backwards compatibility reasons. They are enabled by default if
  # akka.remote.artery.enabled=on.
  enable-additional-serialization-bindings = on
  
  allow-java-serialization = off
}
```


If you want to use the `chill.config.ConfiguredInstantiator` see `ConfiguredAkkaSerializer`
otherwise, subclass `AkkaSerializer` and override `kryoInstantiator` to control how the `Kryo`
object is created.

## Documentation

To learn more and find links to tutorials and information around the web, check out the [Chill Wiki](https://github.com/twitter/chill/wiki).

The latest ScalaDocs are hosted on Chill's [Github Project Page](http://twitter.github.io/chill).

## Contact

Discussion occurs primarily on the [Chill mailing list](https://groups.google.com/forum/#!forum/chill-user). Issues should be reported on the [GitHub issue tracker](https://github.com/twitter/chill/issues).

## Get Involved + Code of Conduct

Pull requests and bug reports are always welcome!

We use a lightweight form of project governance inspired by the one used by Apache projects.
Please see [Contributing and Committership](https://github.com/twitter/analytics-infra-governance#contributing-and-committership) for our code of conduct and our pull request review process.
The TL;DR is send us a pull request, iterate on the feedback + discussion, and get a +1 from a [Committer](COMMITTERS.md) in order to get your PR accepted.

The current list of active committers (who can +1 a pull request) can be found here: [Committers](COMMITTERS.md)

A list of contributors to the project can be found here: [Contributors](https://github.com/twitter/chill/graphs/contributors)

## Maven

Chill modules are available on Maven Central. The current groupid and version for all modules is, respectively, `"com.twitter"` and  `0.10.0`. Each scala project is published for `2.11`, `2.12` and `2.13`. Search [search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Cchill) when in doubt.

`chill-scala` is not published separately; to use it, reference `chill`. To add the dependency to your project using SBT:

    "com.twitter" %% "chill" % "0.10.0"

## Authors

* Oscar Boykin <https://twitter.com/posco>
* Mike Gagnon <https://twitter.com/MichaelNGagnon>
* Sam Ritchie <https://twitter.com/sritchie>

## License

Copyright 2012 Twitter, Inc.

Licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## Thanks to Yourkit
YourKit supports open source projects with innovative and intelligent tools
for monitoring and profiling Java and .NET applications.
YourKit is the creator of <a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a>,
<a href="https://www.yourkit.com/.net/profiler/">YourKit .NET Profiler</a>,
and <a href="https://www.yourkit.com/youmonitor/">YourKit YouMonitor</a>.
