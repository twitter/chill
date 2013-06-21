## Chill Hadoop

This creates [Hadoop serializers](http://hadoop.apache.org/docs/r1.1.2/api/org/apache/hadoop/io/serializer/Serialization.html)
using [Kryo](https://code.google.com/p/kryo/). This was previously known as
[Cascading.kryo](https://github.com/Cascading/cascading.kryo/) which was a misnomer since the code
was never coupled to Cascading.

We only use Java in the main of this project for compatibility with
[Cascalog](https://github.com/nathanmarz/cascalog).

## Maven
groupid=`"com.twitter"` artifact=`"chill-hadoop"`.

## License

Copyright 2013 Twitter, Inc.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
