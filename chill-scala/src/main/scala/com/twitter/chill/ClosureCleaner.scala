/**
 * Copyright (c) 2010, Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of California, Berkeley nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior written
 *       permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.twitter.chill

import _root_.java.lang.reflect.{Constructor, Field}

import com.esotericsoftware.reflectasm.shaded.org.objectweb.asm.Opcodes._
import com.esotericsoftware.reflectasm.shaded.org.objectweb.asm.{ClassReader, ClassVisitor, MethodVisitor, Type}

import scala.annotation.tailrec
import scala.collection.mutable.{Map => MMap, Set => MSet, Stack => MStack}
import scala.util.Try

object ClosureCleaner {
  /** Clean the closure in place. */
  def apply[T <: AnyRef](func: T): T = {
    new TransitiveClosureCleaner(func).clean
    func
  }
}

trait ClosureCleaner {
  /** The closure to clean. */
  val func: AnyRef
  val funcClass: Class[_] = func.getClass
  val objectCtor: Constructor[_] = classOf[_root_.java.lang.Object].getDeclaredConstructor()
  final val OUTER = "$outer"

  /** Clean [[func]] by replacing its 'outer' with a cleaned clone. See [[cleanOuter()]]. */
  def clean: AnyRef = {
    val newOuter = cleanOuter()
    setOuter(func, newOuter)
    func
  }

  /**
   * Create a new 'cleaned' copy of [[func]]'s outer, without modifying the original. The cleaned
   * outer may have null values for fields that are determined to be unneeded in the context
   * of [[func]].
   *
   * @return The cleaned outer.
   */
  def cleanOuter(): AnyRef

  def outerFieldOf(c: Class[_]): Option[Field] = Try(c.getDeclaredField(OUTER)).toOption

  def setOuter(obj: AnyRef, outer: AnyRef): Unit = {
    if (outer != null) {
      val field = outerFieldOf(obj.getClass).get
      field.setAccessible(true)
      field.set(obj, outer)
    }
  }

  def copyField(f: Field, old: AnyRef, newv: AnyRef): Unit = {
    f.setAccessible(true)
    val accessedValue = f.get(old)
    f.set(newv, accessedValue)
  }

  def instantiateClass(cls: Class[_]): AnyRef =
    sun.reflect.ReflectionFactory
      .getReflectionFactory
      .newConstructorForSerialization(cls, objectCtor)
      .newInstance()
      .asInstanceOf[AnyRef]
}

/**
 * An implementation of [[ClosureCleaner]] that cleans [[func]] by transitively tracing its method
 * calls and field references up through its enclosing scopes. A new hierarchy of outers is
 * constructed by cloning the outer scopes and populating only the fields that are to be accessed
 * by [[func]] (including any of its inner closures). Additionally, outers are removed from the
 * new hierarchy if none of their fields are accessed by [[func]].
 */
private class TransitiveClosureCleaner(val func: AnyRef) extends ClosureCleaner {
  private val accessedFieldsMap = MMap[Class[_], Set[Field]]()
  private lazy val outers: List[(Class[_], AnyRef)] = getOutersOf(func)
  private lazy val outerClasses: List[Class[_]] = outers.map(_._1)

  override def cleanOuter(): AnyRef = {
    storeAccessedFields()
    outers.foldLeft(null: AnyRef) { (prevOuter, clsData) =>
      val (thisOuterCls, realOuter) = clsData
      val nextOuter = instantiateClass(thisOuterCls)
      accessedFieldsMap(thisOuterCls).foreach(copyField(_, realOuter, nextOuter))
      /* If this object's outer is not transitively referenced from the starting closure
         (or any of its inner closures), we can null it out. */
      val parent =
        if (!accessedFieldsMap(thisOuterCls).exists(_.getName == OUTER)) null else prevOuter
      setOuter(nextOuter, parent)
      nextOuter
    }
  }

  /**
   * Returns the (Class, AnyRef) pairs from highest level to lowest level. The last element is the
   * outer of the closure.
   */
  @tailrec
  private def getOutersOf(obj: AnyRef, hierarchy: List[(Class[_], AnyRef)] = Nil): List[(Class[_], AnyRef)] =
    outerFieldOf(obj.getClass) match {
      case None => hierarchy // We have finished
      case Some(f) =>
        // f is the $outer of obj
        f.setAccessible(true)
        // myOuter = obj.$outer
        val myOuter = f.get(obj)
        val outerType = myOuter.getClass
        getOutersOf(myOuter, (outerType, myOuter) :: hierarchy)
    }

  private def classReader(cls: Class[_]): ClassReader = {
    val className = cls.getName.replaceFirst("^.*\\.", "") + ".class"
    new ClassReader(cls.getResourceAsStream(className))
  }

  private def innerClasses: Set[Class[_]] = {
    val seen = MSet[Class[_]](funcClass)
    val stack = MStack[Class[_]](funcClass)
    while (stack.nonEmpty) {
      val cr = classReader(stack.pop())
      val set = MSet[Class[_]]()
      cr.accept(new InnerClosureFinder(set), 0)
      (set -- seen).foreach { cls =>
        seen += cls
        stack.push(cls)
      }
    }
    (seen - funcClass).toSet
  }

  private def getAccessedFields: MMap[Class[_], MSet[String]] = {
    val af = outerClasses
      .foldLeft(MMap[Class[_], MSet[String]]()) { (m, cls) =>
        m += ((cls, MSet[String]()))
      }
    (innerClasses + funcClass).foreach(classReader(_).accept(
      new AccessedFieldsVisitor(af)(classReader), 0))
    af
  }

  private def storeAccessedFields(): Unit = {
    if (accessedFieldsMap.isEmpty) {
      getAccessedFields.foreach {
        case (cls, mset) =>
          def toF(ss: Set[String]): Set[Field] = ss.map(cls.getDeclaredField)
          val set = mset.toSet
          accessedFieldsMap += ((cls, toF(set)))
      }
    }
  }
}

case class MethodIdentifier[T](cls: Class[T], name: String, desc: String)

class AccessedFieldsVisitor(output: MMap[Class[_], MSet[String]],
                            specificMethod: Option[MethodIdentifier[_]] = None,
                            visitedMethods: MSet[MethodIdentifier[_]] = MSet.empty)
                           (clsReader: Class[_] => ClassReader) extends ClassVisitor(ASM5) {
  override def visitMethod(access: Int, name: String, desc: String,
    sig: String, exceptions: Array[String]): MethodVisitor = {
    if (specificMethod.isDefined &&
      (specificMethod.get.name != name || specificMethod.get.desc != desc)) {
      null
    } else {
      new MethodVisitor(ASM5) {
        override def visitFieldInsn(op: Int, owner: String, name: String, desc: String): Unit = {
          if (op == GETFIELD) {
            for (cl <- output.keys if cl.getName == owner.replace('/', '.')) {
              output(cl) += name
            }
          }
        }

        override def visitMethodInsn(op: Int, owner: String, name: String,
          desc: String, itf: Boolean): Unit = {
          for (cl <- output.keys if cl.getName == owner.replace('/', '.')) {
            // Check for calls a getter method for a variable in an interpreter wrapper object.
            // This means that the corresponding field will be accessed, so we should save it.
            if (op == INVOKEVIRTUAL && owner.endsWith("$iwC") && !name.endsWith("$outer")) {
              output(cl) += name
            }
            val m = MethodIdentifier(cl, name, desc)
            if (!visitedMethods.contains(m)) {
              // Keep track of visited methods to avoid potential infinite cycles
              visitedMethods += m
              clsReader(cl).accept(
                new AccessedFieldsVisitor(output, Some(m), visitedMethods)(clsReader), 0)
            }
          }
        }
      }
    }
  }
}

class InnerClosureFinder(output: MSet[Class[_]]) extends ClassVisitor(ASM5) {
  var myName: String = _

  override def visit(version: Int, access: Int, name: String, sig: String,
    superName: String, interfaces: Array[String]): Unit = {
    myName = name
  }

  override def visitMethod(access: Int, name: String, desc: String,
    sig: String, exceptions: Array[String]): MethodVisitor =
    new MethodVisitor(ASM5) {
      override def visitMethodInsn(op: Int, owner: String, name: String,
        desc: String, itf: Boolean) {
        val argTypes = Type.getArgumentTypes(desc)
        if (op == INVOKESPECIAL && name == "<init>" && argTypes.nonEmpty
          && argTypes(0).toString.startsWith("L")
          && argTypes(0).getInternalName == myName) {
          output += Class.forName(owner.replace('/', '.'), false,
            Thread.currentThread.getContextClassLoader)
        }
      }
    }
}
