package ir

import ir.components._

object PrettyPrint {

	def apply (ir: IR, level: Int, indentLevel: Int = 0): Unit = {

    val leadingWS = " " * 2 * indentLevel
    print(leadingWS)

    ir match {

      // ArithmeticOperator

      // Assignment

      case AssignStatement(line, col, loc, value) => {
        println(ir)
        println(leadingWS + "- location")
        PrettyPrint(loc, level, indentLevel + 1)
        println(leadingWS + "- value")
        PrettyPrint(value, level, indentLevel + 1)
      }

      case CompoundAssignStatement(line, col, loc, value, operator) => {
        println(ir)
        println(leadingWS + "- location")
        PrettyPrint(loc, level, indentLevel + 1)
        println(leadingWS + "- value")
        PrettyPrint(value, level, indentLevel + 1)
      }

      case Increment(line, col, loc) => {
        println(ir)
        println(leadingWS + "- location")
        PrettyPrint(loc, level, indentLevel + 1)
      }

      case Decrement(line, col, loc) => {
        println(ir)
        println(leadingWS + "- location")
        PrettyPrint(loc, level, indentLevel + 1)
      }

      // Call

      case MethodCall(line, col, name, params, method, _) => {
        println(ir)
        println(leadingWS + "- params")
        params foreach { PrettyPrint(_, level, indentLevel + 1) }
        // Method declaration is not shown to prevent infinite loop
      }

      // Expression

      case Length(line, col, location) => {
        println(ir)
        println(leadingWS + "- location")
        PrettyPrint(location, level)
      }

      case Location(line, col, name, index, field) => {
        println(ir)
        if (!index.isEmpty) {
          println(leadingWS + "- index")
          PrettyPrint(index.get, level, indentLevel + 1)
        }
        if (level >= 2) {
          val (repVec: Vector[String], repStr: String) = ir.asInstanceOf[Location].getRep("%dum")
          println(leadingWS + "- repVec with %dum baseReg")
          if (!repVec.isEmpty) {
            print(repVec.mkString(leadingWS + "  ", "\n" + leadingWS + "  ", "\n"))
          }
          println(leadingWS + "- repStr with %dum baseReg")
          println(leadingWS + s"  ${repStr}")
        }

        /*
        if (!field.isEmpty) {
          println(leadingWS + "- field")
          PrettyPrint(field.get, level, indentLevel + 1)
        }
        */
      }

      // FieldDeclaration

      case FieldList(line, col, typ, declarations) => {
        println(ir)
        println(leadingWS + "- declarations")
        declarations foreach { PrettyPrint(_, level, indentLevel + 1) }
      }

      case VariableDeclaration(line, col, name, typ) => {
        println(ir)
        if (level >= 2) {
          println(leadingWS + "- rep")
          println(leadingWS + s"  ${ir.asInstanceOf[VariableDeclaration].rep}")
        }
      }

      case ArrayDeclaration(line, col, name, length, typ) => {
        println(ir)
        if (level >= 2) {
          println(leadingWS + "- rep")
          println(leadingWS + s"  ${ir.asInstanceOf[ArrayDeclaration].rep}")
        }
      }

      // Literal

      case IntLiteral(line, col, value) => {
        println(ir)
      }

      case BoolLiteral(line, col, value) => {
        println(ir)
      }
      
      case CharLiteral(line, col, value) => {
        println(ir)
      }
      
      case StringLiteral(line, col, value) => {
        println(ir)
      }

      // LogicalOperator

      // Loop

      case For(line, col, start, condition, update, ifTrue) => {
        println(ir)
        println(leadingWS + "- start")
        PrettyPrint(start, level, indentLevel + 1)
        println(leadingWS + "- condition")
        PrettyPrint(condition, level, indentLevel + 1)
        println(leadingWS + "- update")
        PrettyPrint(update, level, indentLevel + 1)
        println(leadingWS + "- ifTrue")
        PrettyPrint(ifTrue, level, indentLevel + 1)
      }

      case While(line, col, condition, ifTrue) => {
        println(ir)
        println(leadingWS + "- condition")
        PrettyPrint(condition, level, indentLevel + 1)
        println(leadingWS + "- ifTrue")
        PrettyPrint(ifTrue, level, indentLevel + 1)
      }

      // MethodDeclaration

      case LocMethodDeclaration(line, col, name, typ, params, block) => {
        println(ir)
        println(leadingWS + "- params")
        params foreach { PrettyPrint(_, level, indentLevel + 1) }
        println(leadingWS + "- block")
        PrettyPrint(block, level, indentLevel + 1)
      }

      case ExtMethodDeclaration(line, col, name, typ) => {
        println(ir)
      }

      // Operation

      case Not(line, col, eval, block, expression) => {
        println(ir)
        if (level >= 1) {
          if (!eval.isEmpty) {
            println(leadingWS + "- eval")
            PrettyPrint(eval.get, level, indentLevel + 1)
          }
          if (!block.isEmpty) {
            println(leadingWS + "- block")
            PrettyPrint(block.get, level, indentLevel + 1)
          }
          println(leadingWS + "- expression")
          PrettyPrint(expression, level, indentLevel + 1)
        }
      }

      case Negate(line, col, eval, block, expression) => {
        println(ir)
        if (level >= 1) {
          if (!eval.isEmpty) {
            println(leadingWS + "- eval")
            PrettyPrint(eval.get, level, indentLevel + 1)
          }
          if (!block.isEmpty) {
            println(leadingWS + "- block")
            PrettyPrint(block.get, level, indentLevel + 1)
          }
        }
        println(leadingWS + "- expression")
        PrettyPrint(expression, level, indentLevel + 1)
      }

      case ArithmeticOperation(line, col, eval, block, operator, lhs, rhs) => {
        println(ir)
        if (level >= 1) {
          if (!eval.isEmpty) {
            println(leadingWS + "- eval")
            PrettyPrint(eval.get, level, indentLevel + 1)
          }
          if (!block.isEmpty) {
            println(leadingWS + "- block")
            PrettyPrint(block.get, level, indentLevel + 1)
          }
        }
        println(leadingWS + "- lhs")
        PrettyPrint(lhs, level, indentLevel + 1)
        println(leadingWS + "- rhs")
        PrettyPrint(rhs, level, indentLevel + 1)
      }

      case LogicalOperation(line, col, eval, block, operator, lhs, rhs) => {
        println(ir)
        if (level >= 1) {
          if (!eval.isEmpty) {
            println(leadingWS + "- eval")
            PrettyPrint(eval.get, level, indentLevel + 1)
          }
          if (!block.isEmpty) {
            println(leadingWS + "- block")
            PrettyPrint(block.get, level, indentLevel + 1)
          }
        }
        println(leadingWS + "- lhs")
        PrettyPrint(lhs, level, indentLevel + 1)
        println(leadingWS + "- rhs")
        PrettyPrint(rhs, level, indentLevel + 1)
      }

      case TernaryOperation(line, col, eval, block, condition, ifTrue, ifFalse) => {
        println(ir)
        if (level >= 1) {
          if (!eval.isEmpty) {
            println(leadingWS + "- eval")
            PrettyPrint(eval.get, level, indentLevel + 1)
          }
          if (!block.isEmpty) {
            println(leadingWS + "- block")
            PrettyPrint(block.get, level, indentLevel + 1)
          }
        }
        println(leadingWS + "- condition")
        PrettyPrint(condition, level, indentLevel + 1)
        println(leadingWS + "- ifTrue")
        PrettyPrint(ifTrue, level, indentLevel + 1)
        println(leadingWS + "- ifFalse")
        PrettyPrint(ifFalse, level, indentLevel + 1)
      }

      // Program

      case Program(line, col, imports, fields, methods) => {
		  	println(ir)
        println(leadingWS + "- imports")
      	imports foreach { PrettyPrint(_, level, indentLevel + 1) }
        println(leadingWS + "- fields")
      	fields foreach { PrettyPrint(_, level, indentLevel + 1) }
        println(leadingWS + "- methods")
      	methods foreach { PrettyPrint(_, level, indentLevel + 1) }
  	  }

      case Block(line, col, declarations, statements) => {
        println(ir)
        println(leadingWS + "- declarations")
        declarations foreach { PrettyPrint(_, level, indentLevel + 1) }
        println(leadingWS + "- statements")
        statements foreach { PrettyPrint(_, level, indentLevel + 1) }
      }

      // Statement

      case Break(line, col, loop) => {
        println(ir)
        /*
        if (!loop.isEmpty) {
          println(leadingWS + "- loop")
          PrettyPrint(loop.get, level, indentLevel + 1)
        }
        */
      }

      case Continue(line, col, loop) => {
        println(ir)
        /*
        if (!loop.isEmpty) {
          println(leadingWS + "- loop")
          PrettyPrint(loop.get, level, indentLevel + 1)
        }
        */
      }

      case Return(line, col, value) => {
        println(ir)
        if (!value.isEmpty) {
          println(leadingWS + "- value")
          PrettyPrint(value.get, level, indentLevel + 1)
        }
      }

      case If(line, col, condition, ifTrue, ifFalse) => {
        println(ir)
        println(leadingWS + "- condition")
        PrettyPrint(condition, level, indentLevel + 1)
        println(leadingWS + "- ifTrue")
        PrettyPrint(ifTrue, level, indentLevel + 1)
        if (!ifFalse.isEmpty) {
          println(leadingWS + "- ifFalse")
          PrettyPrint(ifFalse.get, level, indentLevel + 1)
        }
      }

      // Catchall
      case _ => throw new NotImplementedError()
    }
	} 
}