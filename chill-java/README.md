## Chill Java

A set of Kryo serializers for things in the Java standard library. This only depends on Java and
Kryo. The main reason for this module is to have these items available to non-scala systems, but
have the same Kryo/ASM/etc dependencies as the rest of the chill package, so they can be used
together without diamond dependency issues.

Also see [Kryo Serializers](https://github.com/magro/kryo-serializers) which has a similar mission,
and from which we took the ArraysAsListSerializer.

## License

Copyright 2012 Twitter, Inc.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
