package com.twitter.chill

import com.twitter.bijection.Bijection

/**
 *  @author Sam Ritchie
 *
 * Convenience class that holds both a Class[T] and a Bijection
 * from type T to Array[Byte].
 */

object BijectionPair {
  def apply[T](klass: Class[T], bijection: Bijection[T,Array[Byte]]): BijectionPair[T] =
    new BijectionPair(klass, bijection)
}

class BijectionPair[T](val klass: Class[T],
                       @transient b: Bijection[T,Array[Byte]])
extends java.io.Serializable {
  protected val bBox = MeatLocker(b)
  def bijection = bBox.copy
}
