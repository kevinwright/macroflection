package macroflection
import Macroflection._


case class CC(i: Int, d: Double)
case class ZZ(cc: CC)

object TestApp extends App {
  val mcc = macroflection[CC]
  println(mcc.children.mkString(", "))

  val mzz = macroflection[ZZ]
  println(mzz.children.mkString(", "))

  val mpair = macroflection[Tuple2[Int,Int]]
  println(mpair.children.mkString(", "))
}
