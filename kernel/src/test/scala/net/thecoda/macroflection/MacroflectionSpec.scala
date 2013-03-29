package net.thecoda.macroflection

import org.specs2.mutable.Specification
import Macroflection._
import scala.reflect.runtime.universe._


class MacroflectionSpec extends Specification {

  case class Simple(i: Int, d: Double)
  case class SingleNested(s: String, cc: Simple)
  case class SeqNested(inner: Seq[Simple])

  "Macroflection" should {
    "capture a simple case class" >> {
      val mflect = macroflection[Simple]
      "be compound" >> { mflect.isProduct must_== true }
      "contain children 'i' and 'd'" >> { mflect.asProduct.children map {_.name} must_== Seq("i", "d") }
      "expose the right child types" >> {
        mflect.asProduct.children map {_.tag.tpe} must_== Seq(typeOf[Int], typeOf[Double])
      }
    }

    "capture a tuple" >> {
      val mflect = macroflection[(Int, String)]
      "be compound" >> { mflect.isProduct must_== true }
      "contain children '_1' and '_2'" >> { mflect.asProduct.children map {_.name} must_== Seq("_1", "_2") }
      "expose the right child types" >> {
        mflect.asProduct.children map {_.tag.tpe} must_== Seq(typeOf[Int], typeOf[String])
      }
    }

    "capture a case class with a nested case class" >> {
      val mflect = macroflection[SingleNested]
      "be compound" >> { mflect.isProduct must_== true }
      "contain children 's' and 'cc'" >> { mflect.asProduct.children map {_.name} must_== Seq("s", "cc") }
    }

    "capture a primitive sequence" >> {
      val mflect = macroflection[Seq[Int]]
      "not be compound" >> { mflect.isProduct must_== false }
      "be a seq mflection" >> { mflect.isSeq must_== true }
      "expose the element type" >> { (mflect.asSeq.elem.tag.tpe <:< typeOf[Int]) must_== true }
    }

    "capture a sequence of case classes" >> {
      val mflect = macroflection[Seq[Simple]]
      "not be compound" >> { mflect.isProduct must_== false }
      "be a seq mflection" >> { mflect.isSeq must_== true }
      "have a compound element type" >> { mflect.asSeq.elem.isProduct must_== true }
      "expose the element type" >> { (mflect.asSeq.elem.tag.tpe <:< typeOf[Simple]) must_== true }
    }

    "capture case class with a nested sequence of case classes" >> {
      val mflect = macroflection[SeqNested]
      "be compound" >> { mflect.isProduct must_== true }
      "have a seq child" >> {
        val child = mflect.asProduct.children.head
        "with the right name" >> {child.name must_== "inner" }
        "with the right type" >> { (child.tag.tpe <:< typeOf[Seq[Simple]]) must_== true }
        "that is a seq" >> {child.macroflection.isSeq must_== true }
        "of compound elements" >> {child.macroflection.asSeq.elem.isProduct must_== true }
      }
    }
  }


}
