package compile

import scala.collection.mutable.{StringBuilder, ListBuffer}
import antlr.{CommonAST, Token}
import edu.mit.compilers.grammar.{ DecafParser, DecafParserTokenTypes, DecafScanner, DecafScannerTokenTypes }

/** Immutable abstract syntax tree with simpler APIs than CommonAST
 *
 */
case class ScalarAST(line: Int, column: Int, text: String, parent: Option[ScalarAST])(lazyChildren: => Vector[ScalarAST]) {

  def children = lazyChildren

  def prettyPrint(numSpace: Int = 0): Unit = {
    val indent = (0 to numSpace) map { _ => " " } mkString ""
    println(s"${indent}${this.text} ${this.line}:${this.column}")

    children foreach {
      _ prettyPrint (numSpace + 2)
    }
  }

}

object ScalarAST {

  private def getChildren(ast: CommonASTWithLines): Vector[CommonASTWithLines] = {
    val children = ListBuffer[CommonASTWithLines]()

    var childOpt = Option(ast.getFirstChild)
    while (! childOpt.isEmpty) {
      val child = childOpt.get
      children.append(child)
      childOpt = Option(child.getNextSibling)
    }

    children.toVector
  }

  def fromCommonAST(ast: CommonASTWithLines, parent: Option[ScalarAST] = None): ScalarAST = {
    val commonChildren = getChildren(ast)
    val column = ast.getColumn
    val line = ast.getLine
    val text = ast.getText

    if (commonChildren.size == 0) {
      new ScalarAST(line, column, text, parent)({Vector()})
    } else {
      lazy val thisTree: ScalarAST = new ScalarAST(line, column, text, parent)({children})
      lazy val children = commonChildren map (child => fromCommonAST(child, Option(thisTree)))
      thisTree
    }
  }

}
