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
          valueBlock = Some(IRto3Addr(value, iter).asInstanceOf[Block])
        )
      }

      case CompoundAssignStatement(line, col, loc, value, valueBlock, operator) => {
        irModified = ir
      }

      case Increment(line, col, loc) => {
        irModified = ir
      }

      case Decrement(line, col, loc) => {
        irModified = ir
      }

      // Call

      case MethodCall(line, col, name, params, paramBlocks, method) => {
        irModified = ir
      }

      // Expression

      case Length(line, col, location) => {
        irModified = ir
      }

      case Location(line, col, name, index, indexBlock, field) => {
        irModified = ir
      }

      // FieldDeclaration

      case FieldList(line, col, typ, declarations) => {
        irModified = ir
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
        irModified = ir
      }

      case While(line, col, condition, conditionBlock, ifTrue) => {
        irModified = ir
      }

      // MethodDeclaration

      case LocMethodDeclaration(line, col, name, typ, params, block) => {
        irModified = ir
      }

      case ExtMethodDeclaration(line, col, name, typ) => {
        irModified = ir
      }

      // Operation

      case Not(line, col, expression) => {
        irModified = ir
      }

      case Negate(line, col, expression) => {
        irModified = ir
      }

      case ArithmeticOperation(line, col, operator, lhs, rhs) => {
        irModified = ir
      }

      case LogicalOperation(line, col, operator, lhs, rhs) => {
        irModified = ir
      }

      case TernaryOperation(line, col, condition, ifTrue, ifFalse) => {
        irModified = ir
      }

      // Program

      case Program(line, col, imports, fields, methods) => {
        irModified = ir.asInstanceOf[Program].copy(
          imports = imports.map(IRto3Addr(_, iter)).asInstanceOf[Vector[ExtMethodDeclaration]],
          fields = fields.map(IRto3Addr(_, iter)).asInstanceOf[Vector[FieldDeclaration]],
          methods = methods.map(IRto3Addr(_, iter)).asInstanceOf[Vector[LocMethodDeclaration]]
        )
  	  }

      case Block(line, col, declarations, statements) => {
        irModified = ir
      }

      // Statement

      case Break(line, col, loop) => {
        irModified = ir
      }

      case Continue(line, col, loop) => {
        irModified = ir
      }

      case Return(line, col, value, valueBlock) => {
        irModified = ir
      }

      case If(line, col, condition, conditionBlock, ifTrue, ifFalse) => {
        irModified = ir
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