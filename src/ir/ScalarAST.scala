package ir

import scala.collection.mutable.ListBuffer

import antlr.CommonAST
import edu.mit.compilers.grammar.DecafParserTokenTypes
import ir.components._

/** Immutable, recursive ADT with simpler APIs than CommonAST.
 *
 * Don't use the constructor of this class directly.
 * Use ScalarAST.fromCommonAST
 *
 * This class differs from CommonAST in the following ways:
 *   1. each node contains children nodes as a collection
 *   2. each node contains pointer to the parent node
 *   3. has pretty-printing
 */
case class ScalarAST(
    token: Int,
    text: String,
    line: Int,
    column: Int,
    parent: Option[ScalarAST]
  )(lazyChildren: => Vector[ScalarAST]) {

  def children = lazyChildren

  /**
   * @param indentLevel OPTIONAL number of indents to prepend to this line
   * @param numSpace OPTIONAL number of space to represent a single indent
   */
  def prettyPrint(indentLevel: Int = 0, numSpace: Int = 2): Unit = {
    val leadingWS = " " * numSpace * indentLevel

    // if line & column are both 0, this token is virtual and doesn't actually exist in source code
    val location = if (line == 0 && column == 0) "" else s"  (${line}:${column})"
    println(s"${leadingWS}${token}:${text}${location}")

    // recurse for each child
    children foreach { _.prettyPrint(indentLevel + 1) }
  }

  def toIR: IR = ASTtoIR(this)

}

object ScalarAST {

  /** Get all children nodes as a Vector.
   *
   * @param parent the parent node whose children will be returned
   * @return all children of `parent` as a Vector, with the order of children preserved
   */
  private def getChildren(parent: CommonASTWithLines): Vector[CommonASTWithLines] = {
    val children = ListBuffer[CommonASTWithLines]()

    // aggregate all children
    var childOpt = Option(parent.getFirstChild)
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
    var commonChildren = getChildren(ast)
    val column = ast.getColumn
    val line = ast.getLine
    val text = ast.getText
    val token = ast.getType

    // ast is a leaf node, so it doesn't have children
    if (commonChildren.size == 0) {
      return new ScalarAST(token, text, line, column, parent)({ Vector() })
    }

    // lazy eval to create immutable double-link
    lazy val thisNode: ScalarAST = new ScalarAST(token, text, line, column, parent)({ children })
    lazy val children = {
      // for children to have parent pointer to this ast
      val thisASTOpt = Option(thisNode)
      val origChildren = commonChildren filter {
        // when '--' is used to negate expressions, they have no effect
        // so a DECREMENT token with 0 children is simply ignored
        c => ! (c.getType == DecafParserTokenTypes.DECREMENT && getChildren(c).size == 0)
      } map {
        fromCommonAST(_, thisASTOpt)
      } toVector

      // make sure method call has argument list, even if it is empty
      if (token == DecafParserTokenTypes.METHOD_CALL) {
        val name = "ARGS"
        val numArgs = origChildren map { _.text } filter { _ == name } size

        // add empty args vector if this method call has no argument vector
        val emptyArgs = new ScalarAST(DecafParserTokenTypes.ARGS, name, 0, 0, thisASTOpt)({ Vector() })
        origChildren ++ { if (numArgs == 0) Vector(emptyArgs) else Vector() }
      } else {
        origChildren
      }
    }

    thisNode
  }

}
