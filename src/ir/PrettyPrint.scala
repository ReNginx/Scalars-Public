package ir

import ir.components._

object PrettyPrint {

	def apply (ir: IR, indentLevel: Int = 0, numSpace: Int = 2): Unit = {

    val leadingWS = " " * numSpace * indentLevel
    print(leadingWS)

    ir match {

      // ArithmeticOperator

      // Assignment

      case AssignStatement(line, col, loc, value, valueBlock) => {
        println(ir)
        println(leadingWS + "- location")
        PrettyPrint(loc, indentLevel + 1)
        println(leadingWS + "- value")
        PrettyPrint(value, indentLevel + 1)
        if (!valueBlock.isEmpty) {
          println(leadingWS + "- valueBlock")
          PrettyPrint(valueBlock.get, indentLevel + 1)
        }
      }

      case CompoundAssignStatement(line, col, loc, value, valueBlock, operator) => {
        println(ir)
        println(leadingWS + "- location")
        PrettyPrint(loc, indentLevel + 1)
        println(leadingWS + "- value")
        PrettyPrint(value, indentLevel + 1)
        if (!valueBlock.isEmpty) {
          println(leadingWS + "- valueBlock")
          PrettyPrint(valueBlock.get, indentLevel + 1)
        }
      }

      case Increment(line, col, loc) => {
        println(ir)
        println(leadingWS + "- location")
        PrettyPrint(loc, indentLevel + 1)
      }

      case Decrement(line, col, loc) => {
        println(ir)
        println(leadingWS + "- location")
        PrettyPrint(loc, indentLevel + 1)
      }

      // Call

      case MethodCall(line, col, name, params, paramBlocks, method) => {
        println(ir)
        println(leadingWS + "- params")
        params foreach { PrettyPrint(_, indentLevel + 1) }
        // Method declaration is not shown to prevent infinite loop
      }

      // Expression

      case Length(line, col, location) => {
        println(ir)
        println(leadingWS + "- location")
        PrettyPrint(location)
      }

      case Location(line, col, name, index, indexBlock, field) => {
        println(ir)
        if (!index.isEmpty) {
          println(leadingWS + "- index")
          PrettyPrint(index.get, indentLevel + 1)
        }
        /*
        if (!field.isEmpty) {
          println(leadingWS + "- field")
          PrettyPrint(field.get, indentLevel + 1)
        }
        */
      }

      // FieldDeclaration

      case FieldList(line, col, typ, declarations) => {
        println(ir)
        println(leadingWS + "- declarations")
        declarations foreach { PrettyPrint(_, indentLevel + 1) }
      }

      case VariableDeclaration(line, col, name, typ) => {
        println(ir)
      }

      case ArrayDeclaration(line, col, name, length, typ) => {
        println(ir)
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

      case For(line, col, start, condition, conditionBlock, update, ifTrue) => {
        println(ir)
        println(leadingWS + "- start")
        PrettyPrint(start, indentLevel + 1)
        println(leadingWS + "- condition")
        PrettyPrint(condition, indentLevel + 1)
        println(leadingWS + "- update")
        PrettyPrint(update, indentLevel + 1)
        println(leadingWS + "- ifTrue")
        PrettyPrint(ifTrue, indentLevel + 1)
      }

      case While(line, col, condition, conditionBlock, ifTrue) => {
        println(ir)
        println(leadingWS + "- condition")
        PrettyPrint(condition, indentLevel + 1)
        println(leadingWS + "- ifTrue")
        PrettyPrint(ifTrue, indentLevel + 1)
      }

      // MethodDeclaration

      case LocMethodDeclaration(line, col, name, typ, params, block) => {
        println(ir)
        println(leadingWS + "- params")
        params foreach { PrettyPrint(_, indentLevel + 1) }
        println(leadingWS + "- block")
        PrettyPrint(block, indentLevel + 1)
      }

      case ExtMethodDeclaration(line, col, name, typ) => {
        println(ir)
      }

      // Operation

      case Not(line, col, block, expression) => {
        println(ir)
        println(leadingWS + "- expression")
        PrettyPrint(expression, indentLevel + 1)
      }

      case Negate(line, col, block, expression) => {
        println(ir)
        println(leadingWS + "- expression")
        PrettyPrint(expression, indentLevel + 1)
      }

      case ArithmeticOperation(line, col, block, operator, lhs, rhs) => {
        println(ir)
        println(leadingWS + "- lhs")
        PrettyPrint(lhs, indentLevel + 1)
        println(leadingWS + "- rhs")
        PrettyPrint(rhs, indentLevel + 1)
      }

      case LogicalOperation(line, col, block, operator, lhs, rhs) => {
        println(ir)
        println(leadingWS + "- lhs")
        PrettyPrint(lhs, indentLevel + 1)
        println(leadingWS + "- rhs")
        PrettyPrint(rhs, indentLevel + 1)
      }

      case TernaryOperation(line, col, block, condition, ifTrue, ifFalse) => {
        println(ir)
        println(leadingWS + "- condition")
        PrettyPrint(condition, indentLevel + 1)
        println(leadingWS + "- ifTrue")
        PrettyPrint(ifTrue, indentLevel + 1)
        println(leadingWS + "- ifFalse")
        PrettyPrint(ifFalse, indentLevel + 1)
      }

      // Program

      case Program(line, col, imports, fields, methods) => {
		  	println(ir)
        println(leadingWS + "- imports")
      	imports foreach { PrettyPrint(_, indentLevel + 1) }
        println(leadingWS + "- fields")
      	fields foreach { PrettyPrint(_, indentLevel + 1) }
        println(leadingWS + "- methods")
      	methods foreach { PrettyPrint(_, indentLevel + 1) }
  	  }

      case Block(line, col, declarations, statements) => {
        println(ir)
        println(leadingWS + "- declarations")
        declarations foreach { PrettyPrint(_, indentLevel + 1) }
        println(leadingWS + "- statements")
        statements foreach { PrettyPrint(_, indentLevel + 1) }
      }

      // Statement

      case Break(line, col, loop) => {
        println(ir)
        /*
        if (!loop.isEmpty) {
          println(leadingWS + "- loop")
          PrettyPrint(loop.get, indentLevel + 1)
        }
        */
      }

      case Continue(line, col, loop) => {
        println(ir)
        /*
        if (!loop.isEmpty) {
          println(leadingWS + "- loop")
          PrettyPrint(loop.get, indentLevel + 1)
        }
        */
      }

      case Return(line, col, value, valueBlock) => {
        println(ir)
        if (!value.isEmpty) {
          println(leadingWS + "- value")
          PrettyPrint(value.get, indentLevel + 1)
        }
      }

      case If(line, col, condition, conditionBlock, ifTrue, ifFalse) => {
        println(ir)
        println(leadingWS + "- condition")
        PrettyPrint(condition, indentLevel + 1)
        println(leadingWS + "- ifTrue")
        PrettyPrint(ifTrue, indentLevel + 1)
        if (!ifFalse.isEmpty) {
          println(leadingWS + "- ifFalse")
          PrettyPrint(ifFalse.get, indentLevel + 1)
        }
      }

      // Catchall

      case _ => {
        println("Undefined for PrettyPrint!")
      }
    }
	} 
}