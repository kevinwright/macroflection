package macroflection


import scala.reflect.runtime.universe._
import reflect.macros.Context
import scala.language.experimental.macros


trait Macroflection {
  type TPE
  def tag: TypeTag[TPE]
  def isSeq: Boolean
  def isCompound: Boolean
  def children: Seq[Macroflection.Child]

  def child(nme: String): Option[Macroflection.Child] = children find {_.name == nme}
}

abstract class MacroflectionAux[T : TypeTag] extends Macroflection {
  type TPE = T
  val tag = typeTag[T]
}



trait CompoundMacroflection extends Macroflection {
  val isSeq = false
  val isCompound = true
}

class SimpleMacroflection[T: TypeTag] extends MacroflectionAux[T] {
  val isSeq = false
  val isCompound = false
  val children = Seq.empty[Macroflection.Child]
}

//trait SeqMacroflection[T: TypeTag] extends MacroflectionAux[T] {
//  val isSeq = true
//  val isCompound = false
//}

class ProductMacroflection[T: TypeTag](val children: Seq[Macroflection.Child])
  extends MacroflectionAux[T] with CompoundMacroflection



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

  type SeqType[A] = Seq[A] {
    type TPE = A
  }

//  implicit def seqsHaveMflection[({type λ[α] = Seq[A]})#λ](implicit tt: TypeTag[S]): SeqMacroflection[S] =
//    macro productsHaveMflectionImpl[T]

  implicit def productsHaveMflection[T](implicit isProduct: T <:< Product): ProductMacroflection[T] =
    macro productsHaveMflectionImpl[T]

  def productsHaveMflectionImpl[T : c.WeakTypeTag](c: Context)(isProduct: c.Expr[T <:< Product]): c.Expr[ProductMacroflection[T]] = {
    c.Expr[ProductMacroflection[T]](InContext[c.type](c).mkProductMflection(c.weakTypeOf[T]))
  }

  private object InContext {
    def apply[C <: Context](c: C): Instance[C] = new Instance[C](c)

    class Instance[C <: Context](val c: C) {
      import c.universe._
      val reflectiveHelpers = ReflectiveHelpers.forMacro[c.type](c)
      import reflectiveHelpers._

      def log(str: String) { c.echo(NoPosition, str) }


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
        if (tpe <:< typeOf[Product]) {
          mkProductMflection(tpe)
        } else {
          val mflectType = mkAppliedType[MacroflectionAux](tpe)
          //log("mflectType: " + show(mflectType))
          c.inferImplicitValue(mflectType)
        }
      }

      def mkProductSchemaTree(prodType: Type, kids: List[Tree]): Tree = {
        val prodSchemaType = mkAppliedType[ProductMacroflection](prodType)
        val kidsTree = mkSeq(kids)
        log("kidsTree: " + show(kidsTree))

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
