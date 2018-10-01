package compile

import scala.collection.mutable.{StringBuilder, ListBuffer}
import antlr.{CommonAST, Token}
import edu.mit.compilers.grammar.{ DecafParser, DecafParserTokenTypes, DecafScanner, DecafScannerTokenTypes }

/** Hack found online to force CommonAST to remember line/col information.
 *
 * Found at:
 * https://puredanger.github.io/tech.puredanger.com/2007/02/01/recovering-line-and-column-numbers-in-your-antlr-ast/
 */
class CommonASTWithLines extends CommonAST {
  private var line = 0;
  private var column = 0;

  override def initialize(token: Token): Unit = {
    super.initialize(token)
    line = token.getLine
    column = token.getColumn
  }

  override def getLine(): Int = line
  
  override def getColumn(): Int = column

  override def getFirstChild(): CommonASTWithLines = {
    super.getFirstChild.asInstanceOf[CommonASTWithLines]
  }

  override def getNextSibling(): CommonASTWithLines = {
    super.getNextSibling.asInstanceOf[CommonASTWithLines]
  }

}
