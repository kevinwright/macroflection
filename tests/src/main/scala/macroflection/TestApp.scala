package macroflection
import Macroflection._
import scala.reflect.runtime.universe._


case class CC(i: Int, d: Double)
case class ZZ(cc: CC)
case class SS(innner: Seq[CC])

object TestApp extends App {

  def implicitLab[S <: Seq[_]]
  (implicit tt: TypeTag[S]): TypeTag[S] = tt


  val mflectSampleCaseClass = macroflection[CC]
  println("mflectSampleCaseClass children: " + mflectSampleCaseClass.asCompound.children.mkString(", "))

  val mflectNestedCaseClass = macroflection[ZZ]
  println("mflectNestedCaseClass children: " + mflectNestedCaseClass.asCompound.children.mkString(", "))


  val mflectSeqOfInt = macroflection[Seq[Int]]
  println("mflectSeqOfInt elem: " + mflectSeqOfInt.asSeq.elem)

  val mflectSeqOfCaseClasses = macroflection[Seq[ZZ]]
  println("mflectSeqOfCaseClasses elem children: " + mflectSeqOfCaseClasses.asSeq.elem.asCompound.children.mkString(", "))

  val mflectOfCaseClassWithSeqOfCaseClasses = macroflection[SS]
  println("mflectOfCaseClassWithSeqOfCaseClasses children: " +
    mflectOfCaseClassWithSeqOfCaseClasses.asCompound.children.mkString(", "))

  val mflectPair = macroflection[Tuple2[Int,Int]]
  println("mflectPair children: " + mflectPair.asCompound.children.mkString(", "))

  val emptyVr = Validation.errorIfEmpty("" + "")
  println(emptyVr)
  val nonEmptyVr = Validation.errorIfEmpty("xxx")
  println(nonEmptyVr)
}
