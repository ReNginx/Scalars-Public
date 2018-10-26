package codegen

import ir.components._

object IRto3Addr {

	def apply (ir: IR, iter:Iterator[Int]): IR = {

    var irModified: IR = ir

    ir match {

      // ArithmeticOperator

      // Assignment

      case AssignStatement(line, col, loc, value, valueBlock) => {
        irModified = ir.asInstanceOf[AssignStatement].copy(
          loc = IRto3Addr(loc, iter).asInstanceOf[Location],
          value = IRto3Addr(value, iter).asInstanceOf[Expression]
        )
      }

      case CompoundAssignStatement(line, col, loc, value, valueBlock, operator) => {
        irModified = ir.asInstanceOf[CompoundAssignStatement].copy(
          loc = IRto3Addr(loc, iter).asInstanceOf[Location],
          value = IRto3Addr(value, iter).asInstanceOf[Expression]
        )
      }

      case Increment(line, col, loc) => {
        irModified = ir.asInstanceOf[Increment].copy(
          loc = IRto3Addr(loc, iter).asInstanceOf[Location]
        )
      }

      case Decrement(line, col, loc) => {
        irModified = ir.asInstanceOf[Decrement].copy(
          loc = IRto3Addr(loc, iter).asInstanceOf[Location]
        )
      }

      // Call

      case MethodCall(line, col, name, params, paramBlocks, method) => {
        irModified = ir.asInstanceOf[MethodCall].copy(
          params = params.map(IRto3Addr(_, iter).asInstanceOf[Expression])
          // Do not recurse into method declaration
        )
      }

      // Expression

      case Length(line, col, location) => {
        irModified = ir.asInstanceOf[Length].copy(
          location = IRto3Addr(location, iter).asInstanceOf[Location]
        )
      }

      case Location(line, col, name, index, indexBlock, field) => {
        irModified = ir.asInstanceOf[Location].copy(
          index = if (!index.isEmpty)
            Some(IRto3Addr(index.get, iter).asInstanceOf[Expression])
            else index
          // Do not recurse into field declaration
        )
      }

      // FieldDeclaration

      case FieldList(line, col, typ, declarations) => {
        irModified = ir.asInstanceOf[FieldList].copy(
          declarations = declarations.map(IRto3Addr(_, iter).asInstanceOf[FieldDeclaration])
        )
      }

      case VariableDeclaration(line, col, name, typ) => {
        irModified = ir
      }

      case ArrayDeclaration(line, col, name, length, typ) => {
        irModified = ir
      }

      // Literal

      case IntLiteral(line, col, value) => {
        irModified = ir
      }

      case BoolLiteral(line, col, value) => {
        irModified = ir
      }

      case CharLiteral(line, col, value) => {
        irModified = ir
      }

      case StringLiteral(line, col, value) => {
        irModified = ir
      }

      // LogicalOperator

      // Loop

      case For(line, col, start, condition, conditionBlock, update, ifTrue) => {
        irModified = ir.asInstanceOf[For].copy(
          start = IRto3Addr(start, iter).asInstanceOf[AssignStatement],
          condition = IRto3Addr(condition, iter).asInstanceOf[Expression],
          update = IRto3Addr(update, iter).asInstanceOf[Assignment],
          ifTrue = IRto3Addr(ifTrue, iter).asInstanceOf[Block]
        )
      }

      case While(line, col, condition, conditionBlock, ifTrue) => {
        irModified = ir.asInstanceOf[While].copy(
          condition = IRto3Addr(condition, iter).asInstanceOf[Expression],
          ifTrue = IRto3Addr(ifTrue, iter).asInstanceOf[Block]
        )
      }

      // MethodDeclaration

      case LocMethodDeclaration(line, col, name, typ, params, block) => {
        irModified = ir.asInstanceOf[LocMethodDeclaration].copy(
          params = params.map(IRto3Addr(_, iter).asInstanceOf[FieldDeclaration]),
          block = IRto3Addr(block, iter).asInstanceOf[Block]
        )
      }

      case ExtMethodDeclaration(line, col, name, typ) => {
        irModified = ir
      }

      // Operation

      case Not(line, col, eval, block, expression) => {
        irModified = ir.asInstanceOf[Not].copy(
          expression = IRto3Addr(expression, iter).asInstanceOf[Expression]
        )

        val expressionChild = irModified.asInstanceOf[Not].expression

        var exprNew = Not(0, 0, None, None, expressionChild) // placeholder
        var blockChild = Block(0, 0, Vector(), Vector()) // placeholder

        expressionChild match {
          case
            Location (_, _, _, _, _, _) |
            MethodCall (_, _, _, _, _, _) |
            BoolLiteral (_, _, _) => {
            blockChild = Block(0, 0, Vector(), Vector())
            exprNew = exprNew.copy(
              expression = expressionChild
            )
          }
          case _ => {
            blockChild = expressionChild.block.get
            exprNew = exprNew.copy(
              expression = expressionChild.eval.get
            )
          }
        }

        val varIndex = iter.next
        val varNew = VariableDeclaration(0, 0, varIndex.toString + "_tmp", Some(BoolType))
        val evalNew = Location(0, 0, varNew.name, None, None, Some(varNew))
        val statementNew = AssignStatement(0, 0, evalNew, exprNew, None)
        val blockNew = blockChild.asInstanceOf[Block].copy(
          declarations = blockChild.declarations :+ varNew,
          statements = blockChild.statements :+ statementNew
        )
        irModified = irModified.asInstanceOf[Not].copy(
          eval = Some(evalNew),
          block = Some(blockNew)
        )
      }

      case Negate(line, col, eval, block, expression) => {
        irModified = ir.asInstanceOf[Negate].copy(
          expression = IRto3Addr(expression, iter).asInstanceOf[Expression]
        )

        val expressionChild = irModified.asInstanceOf[Negate].expression

        var exprNew = Negate(0, 0, None, None, expressionChild) // placeholder
        var blockChild = Block(0, 0, Vector(), Vector()) // placeholder

        expressionChild match {
          case
            Location (_, _, _, _, _, _) |
            Length (_, _, _) |
            MethodCall (_, _, _, _, _, _) |
            IntLiteral (_, _, _) |
            CharLiteral (_, _, _) => {
            blockChild = Block(0, 0, Vector(), Vector())
            exprNew = exprNew.copy(
              expression = expressionChild
            )
          }
          case _ => {
            blockChild = expressionChild.block.get
            exprNew = exprNew.copy(
              expression = expressionChild.eval.get
            )
          }
        }

        val varIndex = iter.next
        val varNew = VariableDeclaration(0, 0, varIndex.toString + "_tmp", Some(IntType))
        val evalNew = Location(0, 0, varNew.name, None, None, Some(varNew))
        val statementNew = AssignStatement(0, 0, evalNew, exprNew, None)
        val blockNew = blockChild.asInstanceOf[Block].copy(
          declarations = blockChild.declarations :+ varNew,
          statements = blockChild.statements :+ statementNew
        )
        irModified = irModified.asInstanceOf[Negate].copy(
          eval = Some(evalNew),
          block = Some(blockNew)
        )
      }

      case ArithmeticOperation(line, col, eval, block, operator, lhs, rhs) => {
        irModified = ir.asInstanceOf[ArithmeticOperation].copy(
          lhs = IRto3Addr(lhs, iter).asInstanceOf[Expression],
          rhs = IRto3Addr(rhs, iter).asInstanceOf[Expression]
        )

        val expressionLHS = irModified.asInstanceOf[ArithmeticOperation].lhs
        val expressionRHS = irModified.asInstanceOf[ArithmeticOperation].rhs

        var exprNew = ArithmeticOperation(0, 0, None, None, operator, expressionLHS, expressionRHS) // placeholder
        var blockLHS = Block(0, 0, Vector(), Vector()) // placeholder
        var blockRHS = Block(0, 0, Vector(), Vector()) // placeholder

        expressionLHS match {
          case
            Location (_, _, _, _, _, _) |
            Length (_, _, _) |
            MethodCall (_, _, _, _, _, _) |
            IntLiteral (_, _, _) |
            CharLiteral (_, _, _) => {
            blockLHS = Block(0, 0, Vector(), Vector())
            exprNew = exprNew.copy(
              lhs = expressionLHS
            )
          }
          case _ => {
            blockLHS = expressionLHS.block.get
            exprNew = exprNew.copy(
              lhs = expressionLHS.eval.get
            )
          }
        }

        expressionRHS match {
          case
            Location (_, _, _, _, _, _) |
            Length (_, _, _) |
            MethodCall (_, _, _, _, _, _) |
            IntLiteral (_, _, _) |
            CharLiteral (_, _, _) => {
            blockRHS = Block(0, 0, Vector(), Vector())
            exprNew = exprNew.copy(
              rhs = expressionRHS
            )
          }
          case _ => {
            blockRHS = expressionRHS.block.get
            exprNew = exprNew.copy(
              rhs = expressionRHS.eval.get
            )
          }
        }

        val varIndex = iter.next
        val varNew = VariableDeclaration(0, 0, varIndex.toString + "_tmp", Some(IntType))
        val evalNew = Location(0, 0, varNew.name, None, None, Some(varNew))
        val statementNew = AssignStatement(0, 0, evalNew, exprNew, None)
        val blockNew = blockLHS.asInstanceOf[Block].copy(
          declarations = blockLHS.declarations ++ blockRHS.declarations :+ varNew,
          statements = blockLHS.statements ++ blockRHS.statements :+ statementNew
        )
        irModified = irModified.asInstanceOf[ArithmeticOperation].copy(
          eval = Some(evalNew),
          block = Some(blockNew)
        )
      }

      case LogicalOperation(line, col, eval, block, operator, lhs, rhs) => {
        irModified = ir.asInstanceOf[LogicalOperation].copy(
          lhs = IRto3Addr(lhs, iter).asInstanceOf[Expression],
          rhs = IRto3Addr(rhs, iter).asInstanceOf[Expression]
        )

        val expressionLHS = irModified.asInstanceOf[LogicalOperation].lhs
        val expressionRHS = irModified.asInstanceOf[LogicalOperation].rhs

        var exprNew = LogicalOperation(0, 0, None, None, operator, expressionLHS, expressionRHS) // placeholder
        var blockLHS = Block(0, 0, Vector(), Vector()) // placeholder
        var blockRHS = Block(0, 0, Vector(), Vector()) // placeholder

        expressionLHS match {
          case
            Location (_, _, _, _, _, _) |
            Length (_, _, _) |
            MethodCall (_, _, _, _, _, _) |
            BoolLiteral (_, _, _) |
            IntLiteral (_, _, _) |
            CharLiteral (_, _, _) => {
            blockLHS = Block(0, 0, Vector(), Vector())
            exprNew = exprNew.copy(
              lhs = expressionLHS
            )
          }
          case _ => {
            blockLHS = expressionLHS.block.get
            exprNew = exprNew.copy(
              lhs = expressionLHS.eval.get
            )
          }
        }

        expressionRHS match {
          case
            Location (_, _, _, _, _, _) |
            Length (_, _, _) |
            MethodCall (_, _, _, _, _, _) |
            BoolLiteral (_, _, _) |
            IntLiteral (_, _, _) |
            CharLiteral (_, _, _) => {
            blockRHS = Block(0, 0, Vector(), Vector())
            exprNew = exprNew.copy(
              rhs = expressionRHS
            )
          }
          case _ => {
            blockRHS = expressionRHS.block.get
            exprNew = exprNew.copy(
              rhs = expressionRHS.eval.get
            )
          }
        }

        val varIndex = iter.next
        val varNew = VariableDeclaration(0, 0, varIndex.toString + "_tmp", Some(BoolType))
        val evalNew = Location(0, 0, varNew.name, None, None, Some(varNew))
        val statementNew = AssignStatement(0, 0, evalNew, exprNew, None)
        val blockNew = blockLHS.asInstanceOf[Block].copy(
          declarations = blockLHS.declarations ++ blockRHS.declarations :+ varNew,
          statements = blockLHS.statements ++ blockRHS.statements :+ statementNew
        )
        irModified = irModified.asInstanceOf[LogicalOperation].copy(
          eval = Some(evalNew),
          block = Some(blockNew)
        )
      }

      case TernaryOperation(line, col, eval, block, condition, ifTrue, ifFalse) => {
        irModified = ir.asInstanceOf[TernaryOperation].copy(
          condition = IRto3Addr(condition, iter).asInstanceOf[Expression],
          ifTrue = IRto3Addr(ifTrue, iter).asInstanceOf[Expression],
          ifFalse = IRto3Addr(ifFalse, iter).asInstanceOf[Expression]
        )

        val expressionCond = irModified.asInstanceOf[TernaryOperation].condition
        val expressionTrue = irModified.asInstanceOf[TernaryOperation].ifTrue
        val expressionFalse = irModified.asInstanceOf[TernaryOperation].ifFalse

        var exprNew = TernaryOperation(0, 0, None, None, expressionCond, expressionTrue, expressionFalse) // placeholder
        var blockCond = Block(0, 0, Vector(), Vector()) // placeholder
        var blockTrue = Block(0, 0, Vector(), Vector()) // placeholder
        var blockFalse = Block(0, 0, Vector(), Vector()) // placeholder

        expressionCond match {
          case
            Location (_, _, _, _, _, _) |
            MethodCall (_, _, _, _, _, _) |
            BoolLiteral (_, _, _) => {
            blockCond = Block(0, 0, Vector(), Vector())
            exprNew = exprNew.copy(
              condition = expressionCond
            )
          }
          case _ => {
            blockCond = expressionCond.block.get
            exprNew = exprNew.copy(
              condition = expressionCond.eval.get
            )
          }
        }

        expressionTrue match {
          case
            Location (_, _, _, _, _, _) |
            Length (_, _, _) |
            MethodCall (_, _, _, _, _, _) |
            BoolLiteral (_, _, _) |
            IntLiteral (_, _, _) |
            CharLiteral (_, _, _) => {
            blockTrue = Block(0, 0, Vector(), Vector())
            exprNew = exprNew.copy(
              ifTrue = expressionTrue
            )
          }
          case _ => {
            blockTrue = expressionTrue.block.get
            exprNew = exprNew.copy(
              ifTrue = expressionTrue.eval.get
            )
          }
        }

        expressionFalse match {
          case
            Location (_, _, _, _, _, _) |
            Length (_, _, _) |
            MethodCall (_, _, _, _, _, _) |
            BoolLiteral (_, _, _) |
            IntLiteral (_, _, _) |
            CharLiteral (_, _, _) => {
            blockFalse = Block(0, 0, Vector(), Vector())
            exprNew = exprNew.copy(
              ifFalse = expressionFalse
            )
          }
          case _ => {
            blockFalse = expressionFalse.block.get
            exprNew = exprNew.copy(
              ifFalse = expressionFalse.eval.get
            )
          }
        }

        val varIndex = iter.next
        val varNew = VariableDeclaration(0, 0, varIndex.toString + "_tmp", Some(BoolType))
        val evalNew = Location(0, 0, varNew.name, None, None, Some(varNew))
        val statementNew = AssignStatement(0, 0, evalNew, exprNew, None)
        val blockNew = blockCond.asInstanceOf[Block].copy(
          declarations = blockCond.declarations ++ blockTrue.declarations ++ blockFalse.declarations :+ varNew,
          statements = blockCond.statements ++ blockTrue.statements ++ blockFalse.statements :+ statementNew
        )
        irModified = irModified.asInstanceOf[TernaryOperation].copy(
          eval = Some(evalNew),
          block = Some(blockNew)
        )
      }

      // Program

      case Program(line, col, imports, fields, methods) => {
        irModified = ir.asInstanceOf[Program].copy(
          imports = imports.map(IRto3Addr(_, iter).asInstanceOf[ExtMethodDeclaration]),
          fields = fields.map(IRto3Addr(_, iter).asInstanceOf[FieldDeclaration]),
          methods = methods.map(IRto3Addr(_, iter).asInstanceOf[LocMethodDeclaration])
        )
  	  }

      case Block(line, col, declarations, statements) => {
        irModified = ir.asInstanceOf[Block].copy(
          declarations = declarations.map(IRto3Addr(_, iter).asInstanceOf[FieldDeclaration]),
          statements = statements.map(IRto3Addr(_, iter).asInstanceOf[Statement])
        )
      }

      // Statement

      case Break(line, col, loop) => {
        irModified = ir
        /* Do not recurse into loop
        irModified = ir.asInstanceOf[Break].copy(
          loop = if (!loop.isEmpty)
            Some(IRto3Addr(loop.get, iter).asInstanceOf[Loop])
            else loop
        )
        */
      }

      case Continue(line, col, loop) => {
        irModified = ir
        /* Do not recurse into loop
        irModified = ir.asInstanceOf[Continue].copy(
          loop = if (!loop.isEmpty)
            Some(IRto3Addr(loop.get, iter).asInstanceOf[Loop])
            else loop
        )
        */
      }

      case Return(line, col, value, valueBlock) => {
        irModified = ir.asInstanceOf[Return].copy(
          value = if (!value.isEmpty)
            Some(IRto3Addr(value.get, iter).asInstanceOf[Expression])
            else value
        )
      }

      case If(line, col, condition, conditionBlock, ifTrue, ifFalse) => {
        irModified = ir.asInstanceOf[If].copy(
          condition = IRto3Addr(condition, iter).asInstanceOf[Expression],
          ifTrue = IRto3Addr(ifTrue, iter).asInstanceOf[Block],
          ifFalse = if (!ifFalse.isEmpty)
            Some(IRto3Addr(ifFalse.get, iter).asInstanceOf[Block])
            else ifFalse
        )
      }

      // Catchall
      /*
      case _ => {
        println("Undefined for IRto3Addr!")
      }
      */
    }
    irModified
	}
}
