package ir

import edu.mit.compilers.grammar.DecafParserTokenTypes
import ir.typed._

import scala.collection.mutable

object ASTtoIR {
  var noError = true

  def apply(ast: ScalarAST): IR = {
    val children = ast.children
    val col = ast.column
    val line = ast.line
    val name = ast.text
    val token = ast.token

    lazy val isVirtualNode = ASTtoIR(children(0))
    lazy val lhs = ASTtoIR(children(0))
    lazy val rhs = ASTtoIR(children(1))
    lazy val lhsExpr = lhs.asInstanceOf[Expression]
    lazy val rhsExpr = rhs.asInstanceOf[Expression]
    lazy val lhsLoc = lhs.asInstanceOf[Location]
    lazy val rhsLoc = rhs.asInstanceOf[Location]
    lazy val lhsTyp = lhs.asInstanceOf[Type]

    token match {

      case DecafParserTokenTypes.MINUS => {
        if (children.size == 1) { // is a binary operation
          Negate(line, col, lhsExpr)
        } else { // is unary
          ArithmeticOperation(line, col, Subtract, lhsExpr, rhsExpr)
        }
      }

      // binary arithmetic operation
      case DecafParserTokenTypes.MULTIPLY => ArithmeticOperation(line, col, Multiply, lhsExpr, rhsExpr)
      case DecafParserTokenTypes.DIVIDE => ArithmeticOperation(line, col, Divide, lhsExpr, rhsExpr)
      case DecafParserTokenTypes.PLUS => ArithmeticOperation(line, col, Add, lhsExpr, rhsExpr)
      case DecafParserTokenTypes.MOD => ArithmeticOperation(line, col, Modulo, lhsExpr, rhsExpr)

      case DecafParserTokenTypes.AND => LogicalOperation(line, col, And, lhsExpr, rhsExpr)
      case DecafParserTokenTypes.OR => LogicalOperation(line, col, Or, lhsExpr, rhsExpr)
      case DecafParserTokenTypes.EQUAL => LogicalOperation(line, col, Equal, lhsExpr, rhsExpr)
      case DecafParserTokenTypes.NEQUAL => LogicalOperation(line, col, NotEqual, lhsExpr, rhsExpr)
      case DecafParserTokenTypes.LESS_THAN => LogicalOperation(line, col, LessThan, lhsExpr, rhsExpr)
      case DecafParserTokenTypes.LESS_THAN_OR_EQ => LogicalOperation(line, col, LessThanOrEqual, lhsExpr, rhsExpr)
      case DecafParserTokenTypes.GREATER_THAN => LogicalOperation(line, col, GreaterThan, lhsExpr, rhsExpr)
      case DecafParserTokenTypes.GREATER_THAN_OR_EQ => LogicalOperation(line, col, GreaterThanOrEqual, lhsExpr, rhsExpr)

      case DecafParserTokenTypes.NOT => Not(line, col, lhsExpr)

      // case DecafParserTokenTypes.ARGS =>  can't do this since we can't return vector
      case DecafParserTokenTypes.METHOD_CALL => {
        val rhs = children(1).children map {
          ASTtoIR(_).asInstanceOf[Expression]
        }
        MethodCall(lhsLoc.line, lhsLoc.col, lhsLoc.name, rhs)
      }

      case DecafParserTokenTypes.PROGRAM => {
        val imports = mutable.MutableList[ExtMethodDeclaration]()
        val fields = mutable.MutableList[FieldDeclaration]()
        val methods = mutable.MutableList[LocMethodDeclaration]()

        for (x <- children) {
          val res = ASTtoIR(x)
          res match {
            case x: ExtMethodDeclaration => imports += x
            case x: FieldList => fields ++= x.declarations
            case x: LocMethodDeclaration => methods += x
          }
        }
        //        val imports = children(0).children map { ASTtoIR(_).asInstanceOf[ExtMethodDeclaration] }
        //        val methods = children(2).children map { ASTtoIR(_).asInstanceOf[LocMethodDeclaration] }
        //        val fields  = children(1).children flatMap { ASTtoIR(_).asInstanceOf[FieldList].declarations }

        Program(line, col, imports.toVector, fields.toVector, methods.toVector)
      }

      case DecafParserTokenTypes.INDEX => isVirtualNode
      case DecafParserTokenTypes.TYPE => isVirtualNode

      case DecafParserTokenTypes.INT => isVirtualNode
      case DecafParserTokenTypes.DECIMAL => {
        try {
          IntLiteral(line, col, name.toLong)
        } catch {
          case e: java.lang.NumberFormatException => {
            println(s"line: $line, col: $col, IntLiteral Overflow")
            noError = false
            IntLiteral(line, col, 0)
          }
        }
      } // literal overflow would throw an exception here.

      case DecafParserTokenTypes.HEX => isVirtualNode
      case DecafParserTokenTypes.HEXADECIMAL => {
        try {
           val hexAsInt = java.lang.Long.parseLong(name.substring(2), 16)
           IntLiteral(line, col, hexAsInt)
        }
        catch {
          case e: java.lang.NumberFormatException => {
            println(s"line: $line, col: $col, IntLiteral Overflow")
            noError = false
            IntLiteral(line, col, 0)
          }
        } // literal overflow would throw an exception here.
      }

      case DecafParserTokenTypes.VAR => isVirtualNode
      case DecafParserTokenTypes.ID => isVirtualNode
      case DecafParserTokenTypes.SC_ID => {
        Location(line, col, name, None)
      }

      case DecafParserTokenTypes.ARRAY => {
        val id = lhsLoc // eventually SC_ID
        //Array Decl is just a place holder. would be replaced in next iteration.
        Location(id.line, id.col, id.name, Option(rhsExpr))
      }

      // case DecafParserTokenTypes.METHOD_DECLARATION => throw new Exception
      // case DecafParserTokenTypes.PARAM_LIST => throw new Exception
      case DecafParserTokenTypes.METHOD_DECLARATION => {
        val typ = lhsTyp
        val loc = rhsLoc
        val paramList = children(2).children map (ASTtoIR(_).asInstanceOf[FieldDeclaration])
        val block = ASTtoIR(children(3)).asInstanceOf[Block]
        LocMethodDeclaration(loc.line, loc.col, loc.name, Option(typ), paramList, block)
      }

      case DecafParserTokenTypes.PARAMETER => {
        val typ = lhsTyp
        val loc = rhsLoc
        val len = loc.index
        if (len.isDefined) {
          println(s"line: $line, col: $col, parameter cannot be an array")
          noError = false
        }
        VariableDeclaration(line, col, loc.name, Option(typ))
      }

      case DecafParserTokenTypes.FIELD_LIST => {
        val typ = lhsTyp
        val fields = children.slice(1, children.size) map {
          _.children(0)
        } map {
          ASTtoIR(_).asInstanceOf[Location]
        } map {
          f => {
            val len = f.index
            if (len.isDefined)
              ArrayDeclaration(f.line, f.col, f.name, len.get.asInstanceOf[IntLiteral], Option(typ))
            else
              VariableDeclaration(f.line, f.col, f.name, Option(typ))
          }
        }
        FieldList(line, col, Option(typ), fields)
      }

      case DecafParserTokenTypes.INCREMENT => Increment(line, col, lhsLoc)
      case DecafParserTokenTypes.DECREMENT => Decrement(line, col, lhsLoc)

      case DecafParserTokenTypes.CHAR_LITERAL => CharLiteral(line, col, name(1)) // becuase 'a' is stored as a string, so second char is the one we want
      case DecafParserTokenTypes.STR_LITERAL => StringLiteral(line, col, name)
      case DecafParserTokenTypes.TK_true => BoolLiteral(line, col, true)
      case DecafParserTokenTypes.TK_false => BoolLiteral(line, col, false)

      // case DecafParserTokenTypes.TK_class => throw new Exception
      case DecafParserTokenTypes.TK_continue => Continue(line, col, null)
      case DecafParserTokenTypes.TK_break => Break(line, col, null)

      case DecafParserTokenTypes.TK_void => VoidType
      case DecafParserTokenTypes.TK_int => IntType
      case DecafParserTokenTypes.TK_bool => BoolType

      case DecafParserTokenTypes.QUESTION => {
        val condition = lhsExpr
        val ifTrue = rhsExpr
        val ifFalse = ASTtoIR(children(2)).asInstanceOf[Expression]
        TernaryOperation(line, col, condition, ifTrue, ifFalse)
      }

      case DecafParserTokenTypes.BLOCK => {
        val fields = children filter {
          _.token == DecafParserTokenTypes.FIELD_LIST
        } flatMap {
          ASTtoIR(_).asInstanceOf[FieldList].declarations
        }
        val statements = children filter {
          _.token != DecafParserTokenTypes.FIELD_LIST
        } map {
          ASTtoIR(_).asInstanceOf[Statement]
        }

        Block(line, col, fields, statements)
      }

      case DecafParserTokenTypes.ASSIGN => {
        AssignStatement(line, col, lhsLoc, rhsExpr)
      }
      case DecafParserTokenTypes.PLUS_ASSIGN => {
        val location = lhs.asInstanceOf[Location]
        CompoundAssignStatement(line, col, location, rhsExpr, Add)
      }
      case DecafParserTokenTypes.MINUS_ASSIGN => {
        val location = lhs.asInstanceOf[Location]
        CompoundAssignStatement(line, col, location, rhsExpr, Subtract)
      }

      case DecafParserTokenTypes.CONDITION => isVirtualNode
      case DecafParserTokenTypes.IF_BLOCK => isVirtualNode
      case DecafParserTokenTypes.ELSE_BLOCK => isVirtualNode
      case DecafParserTokenTypes.TK_if => {
        val condition = lhs.asInstanceOf[Expression]
        val ifTrue = rhs.asInstanceOf[Block]

        val ifFalse = {
          if (children.size == 2) {
            None
          } else {
            val block = ASTtoIR(children(2)).asInstanceOf[Block]
            Option(block)
          }
        }

        If(line, col, condition, ifTrue, ifFalse)
      }

      case DecafParserTokenTypes.FOR_START => isVirtualNode
      case DecafParserTokenTypes.FOR_UPDATE => isVirtualNode

      case DecafParserTokenTypes.IF_NO => isVirtualNode
      case DecafParserTokenTypes.IF_YES => isVirtualNode

      case DecafParserTokenTypes.IMPORT => {
        val location = lhs.asInstanceOf[Location]
        ExtMethodDeclaration(location.line, location.col, location.name)
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
        val block = ASTtoIR(children(3)).asInstanceOf[Block]
        For(line, col, start, condition, update, block)
      }

      case DecafParserTokenTypes.TK_return => Return(line, col, lhsExpr)

      case DecafParserTokenTypes.TK_len => Length(line, col, lhsLoc)

      case _ => {
        throw new Exception
      }
    }
  }

}
