package macroflection

import scala.reflect.runtime.universe._
import reflect.macros.Context
import scala.language.experimental.macros
import reflect.internal.util.Position

final case class ValidationResult(
  warnings: Seq[String] = Nil,
  errors: Seq[String] = Nil
)

object Validation {
//  private implicit object Guard
//
//  def validate(fragments: Seq[Any => ValidationResult]): ValidationResult = {
//
//  }
  def errorIfEmpty(value: String): ValidationResult = macro errorIfEmptyImpl[String]
  def errorIfEmpty[T <: Seq[_]](value: T): ValidationResult = macro errorIfEmptyImpl[Seq[_]]

  def errorIfEmptyImpl[T : c.WeakTypeTag](c: Context)(value: c.Expr[T]): c.Expr[ValidationResult] = {
    InContext[c.type](c).errorIfEmpty(value)
  }

  private object InContext {
    def apply[C <: Context](c: C): Instance[C] = new Instance[C](c)

    class Instance[C <: Context](val c: C) {
      import c.universe._
      val reflectiveHelpers = ReflectiveHelpers.forMacro[c.type](c)
      import reflectiveHelpers._

      def log(str: String) { c.echo(NoPosition, str) }

      def errorIfEmpty[T : WeakTypeTag](value: Expr[T]) = {
        val cls = c.enclosingClass
//        log("enclosing class is: " + c.enclosingClass)
//        log("enclosing position is: " + c.enclosingPosition)
//        log("value sym: " + value.tree.symbol)

//        {
//          val p = value.tree.pos
//          val lc = p.lineContent.stripLineEnd
////          Position.formatMessage(p, "position message", true)
//          log("pos: " + p)
////          log("pos src: \n>>>\n" + p.source.content.mkString("") + "\n<<<")
////          log("pos src: \n>>>\n" + p.source.content.mkString("").drop(p.point) + "\n<<<")
//          if (p.isRange) {
//            log("pos src: \n>>>\n" + p.source.content.mkString("").substring(p.start, p.end) + "\n<<<")
//          }
//          log("pos class: " + p.getClass)
//          log("pos lineContent: " + lc)
//          log("pos line: " + p.line)
//          log("pos column: " + p.column)
//          log("pos point: " + p.point)
//          log("pos show: " + p.show)
//
//          //        log("value pos is range: " + value.tree.pos.isRange)
//          //        log("value pos offset: " + value.tree.pos.point)
//          //        log("value pos column: " + value.tree.pos.column)
//        }

        val srcText = {
          val pos = value.tree.pos
          if (pos.isRange) {
            pos.source.content.mkString("").substring(pos.start, pos.end)
          } else {
            c.abort(pos, "This macro requires the -Yrangepos compiler option")
          }
        }
        if (value.actualType <:< typeOf[String]) {
//          log("it's a string!")
          //clone to avoid "Synthetic tree contains nonsynthetic tree" error under -Yrangepos
          val valueClone = c.Expr[T](value.tree.duplicate)
          reify {
            if(valueClone.splice.asInstanceOf[String].isEmpty)
              ValidationResult( errors = Seq(c.literal(srcText + " is empty").splice) )
            else
              ValidationResult()
          }
        } else {
//          log("it's not a string!")
          reify { ValidationResult() }
        }
      }
    }
  }
}
