package ir

import edu.mit.compilers.grammar.DecafParserTokenTypes
import ir.typed._
import ir.untyped._

object ASTtoUntypedIR {
  def apply(ast: ScalarAST): UntypedIR = {
    val children = ast.children
    val col = ast.column
    val line = ast.line
    val name = ast.text
    val token = ast.token

    lazy val isVirtualNode = ASTtoUntypedIR(children(0))
    lazy val lhs = ASTtoUntypedIR(children(0)).asInstanceOf[UntypedExpression]
    lazy val rhs = ASTtoUntypedIR(children(1)).asInstanceOf[UntypedExpression]

    token match {

      case DecafParserTokenTypes.MINUS => {
        val lhs = ASTtoUntypedIR(children(0)).asInstanceOf[UntypedExpression]
        if (children.size > 1) {  // is a binary operation
          UntypedNegate(line, col, lhs)
        } else {  // is unary
          val rhs = ASTtoUntypedIR(children(1)).asInstanceOf[UntypedExpression]
          UntypedArithmeticOperation(line, col, Subtract, lhs, rhs)
        }
      }

      // binary arithmetic operation
      case DecafParserTokenTypes.MULTIPLY => UntypedArithmeticOperation(line, col, Multiply, lhs, rhs)
      case DecafParserTokenTypes.DIVIDE =>   UntypedArithmeticOperation(line, col, Divide,   lhs, rhs)
      case DecafParserTokenTypes.PLUS =>     UntypedArithmeticOperation(line, col, Add,      lhs, rhs)
      case DecafParserTokenTypes.MOD =>      UntypedArithmeticOperation(line, col, Modulo,   lhs, rhs)

      case DecafParserTokenTypes.AND =>                UntypedLogicalOperation(line, col, And,                lhs, rhs)
      case DecafParserTokenTypes.OR =>                 UntypedLogicalOperation(line, col, Or,                 lhs, rhs)
      case DecafParserTokenTypes.EQUAL =>              UntypedLogicalOperation(line, col, Equal,              lhs, rhs)
      case DecafParserTokenTypes.NEQUAL =>             UntypedLogicalOperation(line, col, NotEqual,           lhs, rhs)
      case DecafParserTokenTypes.LESS_THAN =>          UntypedLogicalOperation(line, col, LessThan,           lhs, rhs)
      case DecafParserTokenTypes.LESS_THAN_OR_EQ =>    UntypedLogicalOperation(line, col, LessThanOrEqual,    lhs, rhs)
      case DecafParserTokenTypes.GREATER_THAN =>       UntypedLogicalOperation(line, col, GreaterThan,        lhs, rhs)
      case DecafParserTokenTypes.GREATER_THAN_OR_EQ => UntypedLogicalOperation(line, col, GreaterThanOrEqual, lhs, rhs)

      case DecafParserTokenTypes.NOT => UntypedNot(line, col, lhs)

      // case DecafParserTokenTypes.ARGS =>  can't do this since we can't return vector
      case DecafParserTokenTypes.METHOD_CALL => {
        val rhs = children(0).children map { ASTtoUntypedIR(_).asInstanceOf[UntypedExpression] }
        UntypedMethodCall(line, col, lhs.asInstanceOf[UntypedLocation], rhs.toVector)
      }

      case DecafParserTokenTypes.PROGRAM => {
        val imports = children(0).children map { ASTtoUntypedIR(_).asInstanceOf[UntypedImport] }
        val methods = children(2).children map { ASTtoUntypedIR(_).asInstanceOf[MethodDeclaration] }
        val fields  = children(1).children flatMap { ASTtoUntypedIR(_).asInstanceOf[FieldList].declarations }

        UntypedProgram(line, col, imports.toVector, fields.toVector, methods.toVector)
      }

      case DecafParserTokenTypes.INDEX => isVirtualNode
      case DecafParserTokenTypes.TYPE =>  isVirtualNode

      case DecafParserTokenTypes.INT =>   isVirtualNode
      case DecafParserTokenTypes.DECIMAL => IntLiteral(line, col, name.toInt)

      case DecafParserTokenTypes.HEX => isVirtualNode
      case DecafParserTokenTypes.HEXADECIMAL => {
        val hexAsInt = Integer.parseInt(name, 16)
        IntLiteral(line, col, hexAsInt)
      }

      case DecafParserTokenTypes.VAR =>   isVirtualNode
      case DecafParserTokenTypes.ID =>    isVirtualNode
      case DecafParserTokenTypes.SC_ID => {
        val zero = IntLiteral(0, 0, 0)
        UntypedLocation(line, col, name, zero)
      }

      case DecafParserTokenTypes.ARRAY => {
        val id = ASTtoUntypedIR(children(0)).asInstanceOf[UntypedLocation]  // eventually SC_ID
        UntypedLocation(id.line, id.col, id.name, rhs)
      }

      // case DecafParserTokenTypes.METHOD_DECLARATION => throw new Exception
      // case DecafParserTokenTypes.PARAM_LIST => throw new Exception
      case DecafParserTokenTypes.PARAMETER => {
        val typ = lhs.asInstanceOf[Type]
        val loc = rhs.asInstanceOf[UntypedLocation]
        val len = loc.index.asInstanceOf[IntLiteral]
        if (len.value > 0) ArrayDeclaration(line, col, name, typ, len) else VariableDeclaration(line, col, name, typ)
      }
      case DecafParserTokenTypes.FIELD_LIST => {
        val typ = lhs.asInstanceOf[Type]

        val fields = children.slice(1, children.size) map {
          _.children(0)
        } map {
          ASTtoUntypedIR(_).asInstanceOf[UntypedLocation]
        } map {
          f => {
            val len = f.index.asInstanceOf[IntLiteral]
            if (len.value > 0) ArrayDeclaration(f.line, f.col, f.name, typ, len) else VariableDeclaration(f.line, f.col, f.name, typ)
          }
        }
        FieldList(line, col, typ, fields.toVector)
      }

      case DecafParserTokenTypes.INCREMENT => UntypedIncrement(line, col, lhs.asInstanceOf[UntypedLocation])
      case DecafParserTokenTypes.DECREMENT => UntypedDecrement(line, col, lhs.asInstanceOf[UntypedLocation])

      case DecafParserTokenTypes.CHAR_LITERAL => CharLiteral(line, col, name(1))  // becuase 'a' is stored as a string, so second char is the one we want
      case DecafParserTokenTypes.STR_LITERAL => StringLiteral(line, col, name)
      case DecafParserTokenTypes.TK_true => BoolLiteral(line, col, true)
      case DecafParserTokenTypes.TK_false => BoolLiteral(line, col, false)

      // case DecafParserTokenTypes.TK_class => throw new Exception
      case DecafParserTokenTypes.TK_continue => UntypedContinue(line, col)
      case DecafParserTokenTypes.TK_break => UntypedBreak(line, col)

      case DecafParserTokenTypes.TK_void => VoidType
      case DecafParserTokenTypes.TK_int =>   IntType
      case DecafParserTokenTypes.TK_bool => BoolType

      case DecafParserTokenTypes.QUESTION => {
        val condition = lhs.asInstanceOf[LogicalOperation]
        val ifTrue = rhs.asInstanceOf[UntypedExpression]
        val ifFalse = ASTtoUntypedIR(children(2)).asInstanceOf[UntypedExpression]
        UntypedTernaryOperation(line, col, condition, ifTrue, ifFalse)
      }

      case DecafParserTokenTypes.BLOCK => {
        val fields = children filter {
          _.token == DecafParserTokenTypes.FIELD_LIST
        } flatMap {
          ASTtoUntypedIR(_).asInstanceOf[FieldList].declarations
        }
        val statements = children filter {
          _.token != DecafParserTokenTypes.FIELD_LIST
        } map {
          ASTtoUntypedIR(_).asInstanceOf[Statement]
        }

        Block(line, col, fields, statements)
      }

      case DecafParserTokenTypes.ASSIGN => {
        val location = lhs.asInstanceOf[UntypedLocation]
        UntypedAssignStatement(line, col, location, rhs)
      }
      case DecafParserTokenTypes.PLUS_ASSIGN => {
        val location = lhs.asInstanceOf[UntypedLocation]
        UntypedCompoundAssignStatement(line, col, location, rhs, Add)
      }
      case DecafParserTokenTypes.MINUS_ASSIGN => {
        val location = lhs.asInstanceOf[UntypedLocation]
        UntypedCompoundAssignStatement(line, col, location, rhs, Subtract)
      }

      case DecafParserTokenTypes.CONDITION => isVirtualNode
      case DecafParserTokenTypes.IF_BLOCK => isVirtualNode
      case DecafParserTokenTypes.ELSE_BLOCK => isVirtualNode
      case DecafParserTokenTypes.TK_if => {
        val condition = lhs.asInstanceOf[UntypedExpression]
        val ifTrue = rhs.asInstanceOf[UntypedBlock]

        val ifFalse = {
          if (children.size == 2) {
            None
          } else {
            val block = ASTtoUntypedIR(children(2)).asInstanceOf[UntypedBlock]
            Option(block)
          }
        }

        UntypedIf(line, col, condition, ifTrue, ifFalse)
      }

      case DecafParserTokenTypes.FOR_START => isVirtualNode
      case DecafParserTokenTypes.FOR_UPDATE => isVirtualNode

      case DecafParserTokenTypes.IF_NO => isVirtualNode
      case DecafParserTokenTypes.IF_YES => isVirtualNode

      case DecafParserTokenTypes.IMPORT => {
        val location = lhs.asInstanceOf[UntypedLocation]
        UntypedImport(line, col, location)
      }

      case DecafParserTokenTypes.TK_while => {
        val condition = lhs
        val block = rhs.asInstanceOf[UntypedBlock]
        UntypedWhile(line, col, lhs, block)
      }
      case DecafParserTokenTypes.TK_for => {
        val start = lhs.asInstanceOf[UntypedAssignStatement]
        val condition = rhs
        val update = ASTtoUntypedIR(children(2)).asInstanceOf[UntypedAssignment]
        val block  = ASTtoUntypedIR(children(3)).asInstanceOf[UntypedBlock]
        UntypedFor(line, col, start, condition, update, block)
      }

      case DecafParserTokenTypes.TK_return => UntypedReturn(line, col, lhs)

      case DecafParserTokenTypes.TK_len => {
        val location = lhs.asInstanceOf[UntypedLocation]
        UntypedLength(line, col, location)
      }

      case _ => {
        throw new Exception
      }
    }
  }

}
