package macroflection
import Macroflection._


case class CC(i: Int, d: Double)
case class ZZ(cc: CC)

object TestApp extends App {
  val mcc = macroflection[CC]
  println(mcc.asCompound.children.mkString(", "))

  val mzz = macroflection[ZZ]
  println(mzz.asCompound.children.mkString(", "))

//  val szz = macroflection[Seq[ZZ]]
//  println(mzz.asSeq.elem.asCompound.children.mkString(", "))

  val mpair = macroflection[Tuple2[Int,Int]]
  println(mpair.asCompound.children.mkString(", "))

  val emptyVr = Validation.errorIfEmpty("" + "")
  println(emptyVr)
  val nonEmptyVr = Validation.errorIfEmpty("xxx")
  println(nonEmptyVr)
}
