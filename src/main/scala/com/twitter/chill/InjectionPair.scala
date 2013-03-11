package com.twitter.chill

import com.twitter.bijection.Injection

/**
 *  @author Sam Ritchie
 *
 * Convenience class that holds both a Class[T] and an Injection from
 * type T to Array[Byte].
 */

object InjectionPair {
  def apply[T](klass: Class[T], injection: Injection[T, Array[Byte]]): InjectionPair[T] =
    new InjectionPair(klass, injection)
}

class InjectionPair[T](val klass: Class[T], @transient b: Injection[T, Array[Byte]])
  extends java.io.Serializable {
  protected val bBox = MeatLocker(b)
  def injection = bBox.copy
}
