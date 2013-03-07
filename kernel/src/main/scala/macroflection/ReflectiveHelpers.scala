package macroflection

import reflect.api.Universe
import annotation.tailrec
import scala.language.higherKinds

class ReflectiveHelpers[U <: Universe](val universe: U) {
  import universe._

  def typeCtorOf[T: WeakTypeTag] = weakTypeOf[T].typeConstructor

  lazy val pairApply = selectChain(Ident("scala"), "Tuple2", "apply")
  lazy val mapApply = selectChain(Ident("scala"), "Predef", "Map", "apply")
  lazy val seqApply = selectChain(Ident("scala"), "Seq", "apply")


  def mkPair(left: Tree, right: Tree) = Apply(pairApply, List(left, right))

  def mkDict(xs: Seq[(String, Tree)]): Tree = {
    val pairTrees = xs.toList map {case (k,v) => mkPair(Literal(Constant(k)), v)}
    Apply(mapApply, pairTrees)
  }

  def mkSeq(xs: List[Tree]): Tree = Apply(seqApply, xs)


  def constructorParamsFor(tpe: Type): Seq[TermSymbol] = {
    val ccname = tpe.typeSymbol.name.toString
    val ctor = tpe.members collectFirst {
      case sym: MethodSymbol if sym.toString == ("constructor " + ccname) => sym
    }
    val paramBlocks = ctor.toList flatMap {_.paramss}
    val params = paramBlocks.head map {_.asTerm}
    params
  }

  // A convenience method for creating a list of a type's accessor methods.
  def caseAccessorsFor(t: Type): List[MethodSymbol] = t.declarations.collect {
    case acc: MethodSymbol if acc.isCaseAccessor => acc
  }.toList

  /** Convenience method for creating an empty constructor tree. */
  def mkEmptyConstructor() = {

    val literalUnit = Literal(Constant(()))

    val rhsBlock = Block(
      Apply(
        Select(Super(This(tpnme.EMPTY), tpnme.EMPTY), nme.CONSTRUCTOR),
        Nil
      ) :: Nil,
      literalUnit
    )

    DefDef(
      mods     = Modifiers(),
      name     = nme.CONSTRUCTOR,
      tparams  = Nil,
      vparamss = Nil :: Nil,
      tpt      = TypeTree(),
      rhs      = rhsBlock
    )
  }

  def emptyMapTree = reify(Map.empty).tree


  @tailrec final def selectChain(qualifier: Tree, names: String*): Tree = names match {
    case Seq(h, t @ _*) => selectChain(Select(qualifier, newTermName(h)), t: _*)
    case _ => qualifier
  }

  def mkAppliedType[T[_]](app: Type)(implicit ev: WeakTypeTag[T[_]]) =
    appliedType(typeCtorOf[T[_]], List(app))

  def mkAppliedType2[T[_,_]](app1: Type, app2: Type)(implicit ev: WeakTypeTag[T[_,_]]) =
    appliedType(typeCtorOf[T[_,_]], List(app1, app2))
}

object ReflectiveHelpers {
  import reflect.runtime.{universe => ru}
  import scala.reflect.macros.Context

  def forRuntime = new ReflectiveHelpers[ru.type](ru)
  def forMacro[C <: Context](ctx: C) = new ReflectiveHelpers[ctx.universe.type](ctx.universe)
}

