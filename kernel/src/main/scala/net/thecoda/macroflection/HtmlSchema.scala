package net.thecoda.macroflection

import xml.NodeSeq
import Macroflection._

object HtmlSchema {
  def htmlSchema[T: MacroflectionAux]: NodeSeq = {
    val m = macroflection[T]
    build(m)
  }

  def build(m: Macroflection): NodeSeq = {
    if(m.isProduct) { buildProduct(m.asProduct) }
    else if(m.isSeq) { buildSeq(m.asSeq) }
    else { buildPrimitive(m.asSeq) }
  }

  private def buildPrimitive(m: Macroflection): NodeSeq = {
    <span>{m.tag.toString()}</span>
  }

  private def buildSeq[T](m: SeqMacroflection[T]): NodeSeq = {
    <div>
      <span>...</span>
      <br/>
      {build(m.elem)}
    </div>
  }
  private def buildProduct[T](m: ProductMacroflection[T]): NodeSeq = {
    <table>{
      for(child <- m.children) {
        <tr>
          <td>{child.name}</td>
          <td>{build(child.macroflection)}</td>
        </tr>
      }
    }</table>
  }

}
