package ir

import scala.collection.mutable.ListBuffer

import antlr.CommonAST

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
case class ScalarAST(text: String, line: Int, column: Int, parent: Option[ScalarAST])(lazyChildren: => Vector[ScalarAST]) {

  def children = lazyChildren

  /**
   * @param indentLevel OPTIONAL number of indents to prepend to this line
   * @param numSpace OPTIONAL number of space to represent a single indent
   */
  def prettyPrint(indentLevel: Int = 0, numSpace: Int = 2): Unit = {
    // `indentLevel * numSpace` number of spaces
    val leadingWS = (0 to indentLevel) map { _ => " "*numSpace } mkString ""

    // if line & column are both 0, this token is virtual and doesn't actually exist in source code
    val location = if (line == 0 && column == 0) "" else s"  (${line}:${column})"
    println(s"${leadingWS}${text}${location}")

    // recurse
    children foreach { _.prettyPrint(indentLevel + 1) }
  }

}

object ScalarAST {

  /** Get all children of `ast` as a Vector.
   *
   * @param ast
   * @return all children of `ast` as a Vector, with order of children preserved
   */
  private def getChildren(ast: CommonASTWithLines): Vector[CommonASTWithLines] = {
    // all children of `ast`
    val children = ListBuffer[CommonASTWithLines]()

    // aggregate all children
    var childOpt = Option(ast.getFirstChild)
    while (! childOpt.isEmpty) {
      val child = childOpt.get
      children += child
      childOpt = Option(child.getNextSibling)
    }

    children.toVector
  }

  /** Transform a CommonASTWithLines to a ScalarAST.
   *
   * @param ast the CommonASTWithLines to transform
   * @param parent OPTIONAL the parent of the tree to be returned
   *               `parent == None` implies `ast` is a root tree
   * @return a ScalarAST instance
   */
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
