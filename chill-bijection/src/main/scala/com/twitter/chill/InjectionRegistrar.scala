package com.twitter.chill

import com.twitter.bijection.Injection

// need the root, because java looks like chill.java to scalac. :(
import _root_.java.io.Serializable

/**
 * @author
 *   Sam Ritchie
 * @author
 *   Oscar Boykin
 *
 * Convenience class that holds both a Class[T] and an Injection from type T to Array[Byte].
 */
object InjectionRegistrar {
  def apply[T](klass: Class[T], injection: Injection[T, Array[Byte]]): InjectionRegistrar[T] =
    new InjectionRegistrar(klass, injection)
}

class InjectionRegistrar[T](val klass: Class[T], @transient b: Injection[T, Array[Byte]])
    extends IKryoRegistrar
    with Serializable {
  protected val bBox: MeatLocker[Injection[T, Array[Byte]]] = MeatLocker(b)

  implicit def injection: Injection[T, Array[Byte]] = bBox.copy

  def apply(k: Kryo): Unit =
    if (!k.alreadyRegistered(klass)) {
      k.register(klass, InjectiveSerializer.asKryo[T])
    }
}

object InjectionDefaultRegistrar {
  def apply[T](klass: Class[T], injection: Injection[T, Array[Byte]]): InjectionDefaultRegistrar[T] =
    new InjectionDefaultRegistrar(klass, injection)
}

class InjectionDefaultRegistrar[T](klass: Class[T], @transient b: Injection[T, Array[Byte]])
    extends InjectionRegistrar(klass, b) {
  override def apply(k: Kryo): Unit =
    if (!k.alreadyRegistered(klass)) {
      k.addDefaultSerializer(klass, InjectiveSerializer.asKryo[T])
      k.register(klass)
    }
}
