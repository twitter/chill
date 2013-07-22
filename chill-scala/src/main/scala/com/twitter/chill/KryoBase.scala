/*
Copyright 2012 Twitter, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.twitter.chill

import com.esotericsoftware.kryo.KryoException
import com.esotericsoftware.reflectasm.ConstructorAccess
import com.esotericsoftware.kryo.serializers.FieldSerializer

import org.objenesis.instantiator.ObjectInstantiator
import org.objenesis.strategy.InstantiatorStrategy

import _root_.java.lang.reflect.{Constructor, Modifier}

/*
 * This is the base class of Kryo we use to fix specific scala
 * related issues discovered (ideally, this should be fixed in Kryo)
 */
class KryoBase extends Kryo {

  lazy val objSer = new ObjectSerializer[AnyRef]

  protected var strategy: Option[InstantiatorStrategy] = None

  val functions: Iterable[Class[_]] =
    (0 to 22).map { idx => Class.forName("scala.Function" + idx.toString) }

  def isFn(klass: Class[_]): Boolean =
    functions.find { _.isAssignableFrom(klass) }.isDefined

  override def newDefaultSerializer(klass : Class[_]) : KSerializer[_] = {
    if(isSingleton(klass)) {
      objSer
    }
    else {
      super.newDefaultSerializer(klass) match {
        case fs: FieldSerializer[_] =>
        //Scala has a lot of synthetic fields that must be serialized:
          if(classOf[scala.Serializable].isAssignableFrom(klass)) {
            fs.setIgnoreSyntheticFields(false)
          }
          // Todo: if it is a Function, call the ClosureCleaner on it.
          if(isFn(klass))
            new CleaningSerializer(fs.asInstanceOf[FieldSerializer[AnyRef]])
          else
            fs
        case x: KSerializer[_] => x
      }
    }
  }

  /** return true if this class is a scala "object"
   */
  def isSingleton(klass : Class[_]) : Boolean =
    klass.getName.last == '$' && objSer.accepts(klass)

  // Get the strategy if it is not null
  def tryStrategy(cls: Class[_]): InstantiatorStrategy =
    strategy.getOrElse {
      val name = cls.getName
      if (cls.isMemberClass() && !Modifier.isStatic(cls.getModifiers()))
        throw new KryoException("Class cannot be created (non-static member class): " + name);
      else
        throw new KryoException("Class cannot be created (missing no-arg constructor): " + name);
    }

  override def setInstantiatorStrategy(st: InstantiatorStrategy) {
    super.setInstantiatorStrategy(st)
    strategy = Some(st)
  }

  /* Fixes the case where Kryo's reflectasm doesn't work, even though it claims to
   * TODO this should be fixed in Kryo. When it is, remove this
   */
  override def newInstantiator(cls: Class[_]) = {
    import Instantiators._
    newOrElse(cls,
      List(reflectAsm _, normalJava _),
      // Or fall back on the strategy:
      tryStrategy(cls).newInstantiatorOf(cls))
  }
}

object Instantiators {
  // Go through the list and use the first that works
  def newOrElse(cls: Class[_],
    it: TraversableOnce[Class[_] => Either[Throwable, ObjectInstantiator]],
    elsefn: => ObjectInstantiator): ObjectInstantiator = {
      // Just go through and try each one,
      it.map { fn =>
        fn(cls) match {
          case Left(x) => None // ignore the exception
          case Right(obji) => Some(obji)
        }
      }
      .find { _.isDefined } // Find the first Some(x), returns Some(Some(x))
      .flatMap { x => x } // flatten
      .getOrElse(elsefn)
    }

  // Use call by name:
  def forClass(t: Class[_])(fn:() => Any): ObjectInstantiator =
    new ObjectInstantiator {
      override def newInstance() = {
        try { fn().asInstanceOf[AnyRef] }
        catch {
          case x: Exception => {
            throw new KryoException("Error constructing instance of class: " + t.getName, x)
          }
        }
      }
    }

  // This one tries reflectasm, which is a fast way of constructing an object
  def reflectAsm(t: Class[_]): Either[Throwable, ObjectInstantiator] = {
    try {
      val access = ConstructorAccess.get(t)
      // Try it once, because this isn't always successful:
      access.newInstance
      // Okay, looks good:
      Right(forClass(t) { () => access.newInstance() })
    }
    catch {
      case x: Throwable => Left(x)
    }
  }

  def getConstructor(c: Class[_]): Constructor[_] = {
    try {
      c.getConstructor()
    }
    catch {
      case _: Throwable => {
        val cons = c.getDeclaredConstructor()
        cons.setAccessible(true)
        cons
      }
    }
  }

  def normalJava(t: Class[_]): Either[Throwable, ObjectInstantiator] = {
    try {
      val cons = getConstructor(t)
      Right(forClass(t) { () => cons.newInstance() } )
    }
    catch {
      case x: Throwable => Left(x)
    }
  }
}
