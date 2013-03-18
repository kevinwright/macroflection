package macroflection

import org.specs2.mutable.Specification
import Macroflection._

class MacroflectionSpec extends Specification {

  case class CC(i: Int, d: Double)
  case class ZZ(cc: CC)
  case class SS(innner: Seq[CC])

  "Macroflection" should {
    "capture a simple case class" >> {
      val mflect = macroflection[CC]
      "be compound" >> { mflect.isCompound must_== true }
      "contain children 'i' and 'd'" >> { mflect.asCompound.children map {_.name} must_== Seq("i", "d") }
    }
  }
}
