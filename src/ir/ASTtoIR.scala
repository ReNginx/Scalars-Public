package ir

import edu.mit.compilers.grammar.DecafParserTokenTypes
import ir.components._

/** Convert ScalarAST to IR.
 *
 * See package ir.components to see the IR class hierarchy.
 */
object ASTtoIR {

  // set to true if any error is detected
  var error = false

  /** Convert ScalarAST to IR.
   *
   * @param ast the ScalarAST to convert
   * @param positive true if arithmetic value is positive, false otherwise
   */
  def apply(ast: ScalarAST, positive: Boolean=true): IR = {
    val children = ast.children
    val line = ast.line
    val col = ast.column
    val name = ast.text
    val token = ast.token

    // may or may not throw error, most likely index out of bounds
    def isVirtualNode = ASTtoIR(children(0), positive)
    def lhs = ASTtoIR(children(0), positive)
    def rhs = ASTtoIR(children(1), positive)
    def lhsExpr = lhs.asInstanceOf[Expression]
    def rhsExpr = rhs.asInstanceOf[Expression]
    def lhsLoc = lhs.asInstanceOf[Location]
    def rhsLoc = rhs.asInstanceOf[Location]
    def lhsTyp = lhs.asInstanceOf[Type]

    // actual bulk of the work
    token match {

      // may be unary or binary operation
      case DecafParserTokenTypes.MINUS => {
        // is a binary operation
        if (children.size == 2) {
          return ArithmeticOperation(line, col, Subtract, lhsExpr, rhsExpr)
        }

        // unary operation, so negate the sign
        val expr = ASTtoIR(children(0), !positive).asInstanceOf[Expression]
        expr match {
          case int: IntLiteral => int
          case _ => Negate(line, col, expr)
        }
      }

      // binary arithmetic operations
      case DecafParserTokenTypes.MULTIPLY => ArithmeticOperation(line, col, Multiply, lhsExpr, rhsExpr)
      case DecafParserTokenTypes.DIVIDE   => ArithmeticOperation(line, col, Divide,   lhsExpr, rhsExpr)
      case DecafParserTokenTypes.PLUS     => ArithmeticOperation(line, col, Add,      lhsExpr, rhsExpr)
      case DecafParserTokenTypes.MOD      => ArithmeticOperation(line, col, Modulo,   lhsExpr, rhsExpr)

      // binary logical operations
      case DecafParserTokenTypes.AND                => LogicalOperation(line, col, And,                lhsExpr, rhsExpr)
      case DecafParserTokenTypes.OR                 => LogicalOperation(line, col, Or,                 lhsExpr, rhsExpr)
      case DecafParserTokenTypes.EQUAL              => LogicalOperation(line, col, Equal,              lhsExpr, rhsExpr)
      case DecafParserTokenTypes.NEQUAL             => LogicalOperation(line, col, NotEqual,           lhsExpr, rhsExpr)
      case DecafParserTokenTypes.LESS_THAN          => LogicalOperation(line, col, LessThan,           lhsExpr, rhsExpr)
      case DecafParserTokenTypes.LESS_THAN_OR_EQ    => LogicalOperation(line, col, LessThanOrEqual,    lhsExpr, rhsExpr)
      case DecafParserTokenTypes.GREATER_THAN       => LogicalOperation(line, col, GreaterThan,        lhsExpr, rhsExpr)
      case DecafParserTokenTypes.GREATER_THAN_OR_EQ => LogicalOperation(line, col, GreaterThanOrEqual, lhsExpr, rhsExpr)

      // unary logical operation
      case DecafParserTokenTypes.NOT => Not(line, col, lhsExpr)

      case DecafParserTokenTypes.METHOD_CALL => {
        val rhs = children(1).children map { ASTtoIR(_).asInstanceOf[Expression] }
        MethodCall(lhsLoc.line, lhsLoc.col, lhsLoc.name, rhs)
      }

      case DecafParserTokenTypes.PROGRAM => {
        val irs = children map { ASTtoIR(_) }

        val imports = irs collect { case r: ExtMethodDeclaration => r }
        val methods = irs collect { case l: LocMethodDeclaration => l }
        val fields  = irs collect { case f: FieldList => f } flatMap { _.declarations }

        Program(line, col, imports, fields, methods)
      }

      // INDEX is simply integer or hex
      case DecafParserTokenTypes.INDEX => isVirtualNode

      // TYPE is one of ( TK_int | TK_bool | TK_void )
      case DecafParserTokenTypes.TYPE => isVirtualNode

      // INT is just DECIMAL
      case DecafParserTokenTypes.INT => isVirtualNode
      case DecafParserTokenTypes.DECIMAL => {
        try {
          val num = if (positive) name else "-" + name
          IntLiteral(line, col, num.toLong)
        } catch {
          // literal overflow would throw an exception here.
          case e: java.lang.NumberFormatException => {
            println(s"line: $line, col: $col, IntLiteral Overflow")
            error = true
            IntLiteral(line, col, 0)
          }
        }
      }

      // HEX is just HEXADECIMAL
      case DecafParserTokenTypes.HEX => isVirtualNode
      case DecafParserTokenTypes.HEXADECIMAL => {
        try {
          val num = if (positive) name else "-" + name
          val hexAsInt = java.lang.Long.parseLong(num.replace("0x", ""), 16)
          IntLiteral(line, col, hexAsInt)
        } catch {
          // literal overflow would throw an exception here.
          case e: java.lang.NumberFormatException => {
            println(s"line: $line, col: $col, IntLiteral Overflow")
            error = true
            IntLiteral(line, col, 0)
          }
        }
      }

      case DecafParserTokenTypes.VAR => isVirtualNode  // VAR is just id
      case DecafParserTokenTypes.ID  => isVirtualNode  // ID is just SC_ID
      case DecafParserTokenTypes.SC_ID => Location(line, col, name, None)

      case DecafParserTokenTypes.ARRAY => {
        val id = lhsLoc  // eventually SC_ID
        val index = Option(rhsExpr)
        Location(id.line, id.col, id.name, index)
      }

      case DecafParserTokenTypes.METHOD_DECLARATION => {
        val typ = Option(lhsTyp)
        val loc = rhsLoc
        val params = children(2).children map { ASTtoIR(_).asInstanceOf[FieldDeclaration] }
        val block = ASTtoIR(children(3)).asInstanceOf[Block]
        LocMethodDeclaration(loc.line, loc.col, loc.name, typ, params, block)
      }

      case DecafParserTokenTypes.PARAMETER => {
        val typ = Option(lhsTyp)
        val loc = rhsLoc
        val length = loc.index
        if (length.isDefined) {
          println(s"line: $line, col: $col, parameter cannot be an array")
          error = true
        }
        VariableDeclaration(line, col, loc.name, typ)
      }

      case DecafParserTokenTypes.FIELD_LIST => {
        val typ = Option(lhsTyp)
        val fields = children.slice(1, children.size) map {
          _.children(0)  // for each declaration, get their location
        } map {
          ASTtoIR(_).asInstanceOf[Location]
        } map {
          loc => {
            val len = loc.index
            if (len.isDefined) {
              ArrayDeclaration(loc.line, loc.col, loc.name, len.get.asInstanceOf[IntLiteral], typ)
            } else {
              VariableDeclaration(loc.line, loc.col, loc.name, typ)
            }
          }
        }

        FieldList(line, col, typ, fields)
      }

      case DecafParserTokenTypes.INCREMENT => Increment(line, col, lhsLoc)
      case DecafParserTokenTypes.DECREMENT => Decrement(line, col, lhsLoc)

      // for example, 'a' is stored as a string with 3 chars, so we want the second char
      case DecafParserTokenTypes.CHAR_LITERAL => CharLiteral(line, col, name(1))
      case DecafParserTokenTypes.STR_LITERAL => StringLiteral(line, col, name)
      case DecafParserTokenTypes.TK_true  => BoolLiteral(line, col, true)
      case DecafParserTokenTypes.TK_false => BoolLiteral(line, col, false)

      case DecafParserTokenTypes.TK_break => Break(line, col, null)
      case DecafParserTokenTypes.TK_continue => Continue(line, col, null)

      case DecafParserTokenTypes.TK_bool => BoolType
      case DecafParserTokenTypes.TK_int  =>  IntType
      case DecafParserTokenTypes.TK_void => VoidType

      case DecafParserTokenTypes.QUESTION => {
        val condition = lhsExpr
        val ifTrue = rhsExpr
        val ifFalse = ASTtoIR(children(2)).asInstanceOf[Expression]
        TernaryOperation(line, col, condition, ifTrue, ifFalse)
      }

      case DecafParserTokenTypes.BLOCK => {
        val fieldDecls = children filter {
          _.token == DecafParserTokenTypes.FIELD_LIST
        } flatMap {
          ASTtoIR(_).asInstanceOf[FieldList].declarations
        }

        val statements = children filter {
          _.token != DecafParserTokenTypes.FIELD_LIST
        } map {
          ASTtoIR(_).asInstanceOf[Statement]
        }

        Block(line, col, fieldDecls, statements)
      }

      case DecafParserTokenTypes.ASSIGN       =>         AssignStatement(line, col, lhsLoc, rhsExpr)
      case DecafParserTokenTypes.PLUS_ASSIGN  => CompoundAssignStatement(line, col, lhsLoc, rhsExpr, Add)
      case DecafParserTokenTypes.MINUS_ASSIGN => CompoundAssignStatement(line, col, lhsLoc, rhsExpr, Subtract)

      // CONDITION is just an expression
      case DecafParserTokenTypes.CONDITION => isVirtualNode

      // IF_BLOCK and ELSE_BLOCK are both just BLOCK's
      case DecafParserTokenTypes.IF_BLOCK => isVirtualNode
      case DecafParserTokenTypes.ELSE_BLOCK => isVirtualNode

      case DecafParserTokenTypes.TK_if => {
        val condition = lhsExpr

        val ifTrue = rhs.asInstanceOf[Block]
        val ifFalse = {
          if (children.size < 3) {
            None  // there is no else block
          } else {
            val elseBlock = ASTtoIR(children(2)).asInstanceOf[Block]
            Option(elseBlock)
          }
        }

        If(line, col, condition, ifTrue, ifFalse)
      }

      // FOR_START is just ASSIGN
      case DecafParserTokenTypes.FOR_START  => isVirtualNode

      // FOR_UPDATE is in/decrrement or compound assign
      case DecafParserTokenTypes.FOR_UPDATE => isVirtualNode

      // both are expressions
      case DecafParserTokenTypes.IF_NO  => isVirtualNode
      case DecafParserTokenTypes.IF_YES => isVirtualNode

      case DecafParserTokenTypes.IMPORT => {
        val loc = lhsLoc
        ExtMethodDeclaration(loc.line, loc.col, loc.name)
      }

      case DecafParserTokenTypes.TK_while => {
        val condition = lhsExpr
        val block = rhs.asInstanceOf[Block]
        While(line, col, condition, block)
      }
      case DecafParserTokenTypes.TK_for => {
        val start = lhs.asInstanceOf[AssignStatement]
        val condition = rhsExpr
        val update = ASTtoIR(children(2)).asInstanceOf[Assignment]
        val block  = ASTtoIR(children(3)).asInstanceOf[Block]
        For(line, col, start, condition, update, block)
      }

      case DecafParserTokenTypes.TK_len    => Length(line, col, lhsLoc)
      case DecafParserTokenTypes.TK_return => Return(line, col, lhsExpr)

      case _ => {
        throw new Exception
      }
    }
  }

}
