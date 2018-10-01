package compile

import scala.collection.mutable.{ListBuffer}
import antlr.CommonAST
import edu.mit.compilers.grammar.{ DecafParser, DecafParserTokenTypes, DecafScanner, DecafScannerTokenTypes }

/** Immutable, recursive AST with simpler APIs than CommonAST.
 *
 * Don't use the constructor of this class directly.
 * Use ScalarAST.fromCommonAST
 *
 * This class offers the following that CommonAST does not:
 *   1. child nodes as a collection
 *   2. pointer to the parent node
 *   3. pretty-printing
 */
class ScalarAST(textArg: String, lineArg: Int, colArg: Int, parArg: Option[ScalarAST])(lazyChildren: => Vector[ScalarAST]) {

  private val _line = lineArg
  private val _column = colArg
  private val _text = textArg
  private val _parent = parArg

  def line = _line
  def column = _column
  def text = _text
  def parent = _parent
  def children = lazyChildren

  def prettyPrint(numSpace: Int = 0): Unit = {
    val indent = (0 to numSpace) map { _ => " " } mkString ""
    println(s"${indent}${this.text} ${this.line}:${this.column}")
    children foreach { _.prettyPrint(numSpace + 2) }
  }

}

object ScalarAST {

  private def getChildren(ast: CommonASTWithLines): Vector[CommonASTWithLines] = {
    val children = ListBuffer[CommonASTWithLines]()

    var childOpt = Option(ast.getFirstChild)
    while (! childOpt.isEmpty) {
      val child = childOpt.get
      children += child
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
      new ScalarAST(text, line, column, parent)({ Vector() })
    } else {
      lazy val thisAST: ScalarAST = new ScalarAST(text, line, column, parent)({ children })
      lazy val children = commonChildren map (child => fromCommonAST(child, Option(thisAST)))
      thisAST
    }
  }

}
