package macroflection


import scala.reflect.runtime.universe._
import reflect.macros.Context
import scala.language.experimental.macros


trait Macroflection {
  type TPE
  def tag: TypeTag[TPE]
  def isSeq: Boolean
  def isCompound: Boolean
  def asSeq: SeqMacroflection[TPE] = this.asInstanceOf[SeqMacroflection[TPE]]
  def asCompound: CompoundMacroflection[TPE] = this.asInstanceOf[CompoundMacroflection[TPE]]
}

abstract class MacroflectionAux[T : TypeTag] extends Macroflection {
  type TPE = T
  val tag = typeTag[T]
}




class SimpleMacroflection[T: TypeTag] extends MacroflectionAux[T] {
  val isSeq = false
  val isCompound = false
}

trait SeqMacroflection[S] extends MacroflectionAux[S] {
  val isSeq = true
  val isCompound = false
  def elem: Macroflection
}

class SeqMacroflectionAux[T, S <: Seq[T] : TypeTag](val elem: MacroflectionAux[T]) extends SeqMacroflection[S]

class CompoundMacroflection[T: TypeTag](val children: Seq[Macroflection.Child]) extends MacroflectionAux[T]  {
  val isSeq = false
  val isCompound = true
  def child(nme: String): Option[Macroflection.Child] = children find {_.name == nme}
}



object Macroflection {


  def macroflection[T](implicit mf: MacroflectionAux[T]): Macroflection = mf


  trait Child {
    type PARENT
    type TPE
    def tag: TypeTag[TPE]
    def name: String
    def macroflection: Macroflection
  }
  case class ChildAux[P: TypeTag, C: TypeTag](
    name: String,
    macroflection: Macroflection
  ) extends Child {
    type PARENT = P
    type TPE = C
    val tag = typeTag[TPE]
  }

  implicit object intHasMflection    extends SimpleMacroflection[Int]
  implicit object doubleHasMflection extends SimpleMacroflection[Double]
  implicit object floatHasMflection  extends SimpleMacroflection[Float]
  implicit object longHasMflection   extends SimpleMacroflection[Long]
  implicit object charHasMflection   extends SimpleMacroflection[Char]
  implicit object stringHasMflection extends SimpleMacroflection[String]


//  implicit def seqsHaveMflection[T, S <: Seq[T]]
//    (implicit tt: TypeTag[T], tts: TypeTag[S], elem: MacroflectionAux[T]): SeqMacroflection[S] =
//    new SeqMacroflectionAux[T,S](elem)

  implicit def seqsHaveMflection[S](implicit isSeq: S <:< Seq[_]): SeqMacroflection[S] =
    macro seqsHaveMflectionImpl[S]

  def seqsHaveMflectionImpl[S : c.WeakTypeTag](c: Context)(isSeq: c.Expr[S <:< Seq[_]]): c.Expr[SeqMacroflection[S]] = {
    c.Expr[SeqMacroflection[S]](InContext[c.type](c).mkSeqMflection(c.weakTypeOf[S]))
  }

  implicit def productsHaveMflection[T](implicit isProduct: T <:< Product): CompoundMacroflection[T] =
    macro productsHaveMflectionImpl[T]

  def productsHaveMflectionImpl[T : c.WeakTypeTag](c: Context)(isProduct: c.Expr[T <:< Product]): c.Expr[CompoundMacroflection[T]] = {
    c.Expr[CompoundMacroflection[T]](InContext[c.type](c).mkProductMflection(c.weakTypeOf[T]))
  }

  private object InContext {
    def apply[C <: Context](c: C): Instance[C] = new Instance[C](c)

    class Instance[C <: Context](val c: C) {
      import c.universe._
      val reflectiveHelpers = ReflectiveHelpers.forMacro[c.type](c)
      import reflectiveHelpers._

      def log(str: String) { c.echo(NoPosition, str) }

      def mkSeqMflection(seqType: Type): Tree = {
        seqType match {
          case TypeRef(pre, sym, args) =>
            val elemType = args.head
            val mflectType = appliedType(typeCtorOf[SeqMacroflectionAux[_,_]], List(elemType, seqType))

            log("finding seq child mflect for: " + elemType)
            val elemMflect = mflectFor(elemType)
            log(s" mflectFor($elemType) = $elemMflect")

            val tree = Apply(
              Select(
                New(TypeTree( mflectType )),
                nme.CONSTRUCTOR
              ),
              List(elemMflect) //1st arg block (children)
            )
            log("tree: " + tree)
            tree
          case _ => c.abort(NoPosition, "WTF???")
        }
      }


      def mkProductMflection(prodType: Type): Tree = {

        //log("mkProductMflection for " + show(prodType))

        val caseAccessors = caseAccessorsFor(prodType)
        val childTypes = caseAccessors map {_.returnType.asSeenFrom(prodType, prodType.typeSymbol.asClass)}
        val childNames = caseAccessors map {_.name.toString}

        //log("childNames: " + childNames.mkString(", "))

        val childTrees = (childNames zip childTypes) map { case (name, tpe) => mkChildTree(name, prodType, tpe) }
        val tree = mkProductSchemaTree(prodType, childTrees)
        //log("tree: " + show(tree))
        tree
      }

      def mkChildTree(name: String, parentType: Type, childElemType: Type): Tree = {
        //log("childType: " + show(childType))
        val childMflect = mflectFor(childElemType)
        //log("childMflect: " + show(childMflect))

        val childAuxType = mkAppliedType2[ChildAux](parentType, childElemType)

        val childTree = Apply(
          Select(
            New(TypeTree(childAuxType)),
            nme.CONSTRUCTOR
          ),
          List(
            Literal(Constant(name)),
            childMflect
          )
        )
        //log("childTree: " + show(childTree))
        childTree
      }

      def mflectFor(tpe: Type): Tree = {
//        log("internally finding mflect for: " + tpe)
        if (tpe <:< typeOf[Product]) {
          mkProductMflection(tpe)
        } else {
          val mflectType = mkAppliedType[MacroflectionAux](tpe)
          //log("mflectType: " + show(mflectType))
          c.inferImplicitValue(mflectType)
        }
      }

      def mkProductSchemaTree(prodType: Type, kids: List[Tree]): Tree = {
        val prodSchemaType = mkAppliedType[CompoundMacroflection](prodType)
        val kidsTree = mkSeq(kids)
//        log("kidsTree: " + show(kidsTree))

        Apply(
          Select(
            New(TypeTree( prodSchemaType )),
            nme.CONSTRUCTOR
          ),
          List(kidsTree) //1st arg block (children)
        )
      }

    }
  }





}
