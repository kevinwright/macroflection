package net.thecoda.macroflection
import Macroflection._
import scala.reflect.runtime.universe._


case class CC(i: Int, d: Double)
case class ZZ(cc: CC)
case class SS(innner: Seq[CC])

object TestApp extends App {



  val mflectSampleCaseClass = macroflection[CC]
  println("mflectSampleCaseClass children: " + mflectSampleCaseClass.asProduct.children.mkString(", "))

  val mflectNestedCaseClass = macroflection[ZZ]
  println("mflectNestedCaseClass children: " + mflectNestedCaseClass.asProduct.children.mkString(", "))


  val mflectSeqOfInt = macroflection[Seq[Int]]
  println("mflectSeqOfInt elem: " + mflectSeqOfInt.asSeq.elem)

  val mflectSeqOfCaseClasses = macroflection[Seq[ZZ]]
  println("mflectSeqOfCaseClasses elem children: " + mflectSeqOfCaseClasses.asSeq.elem.asProduct.children.mkString(", "))

  val mflectOfCaseClassWithSeqOfCaseClasses = macroflection[SS]
  println("mflectOfCaseClassWithSeqOfCaseClasses children: " +
    mflectOfCaseClassWithSeqOfCaseClasses.asProduct.children.mkString(", "))

  val mflectPair = macroflection[Tuple2[Int,Int]]
  println("mflectPair children: " + mflectPair.asProduct.children.mkString(", "))

  val emptyVr = Validation.errorIfEmpty("" + "")
  println(emptyVr)
  val nonEmptyVr = Validation.errorIfEmpty("xxx")
  println(nonEmptyVr)
}
