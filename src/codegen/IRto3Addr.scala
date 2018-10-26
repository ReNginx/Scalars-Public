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

      case Not(line, col, block, expression) => {
        irModified = ir
      }

      case Negate(line, col, block, expression) => {
        irModified = ir
      }

      case ArithmeticOperation(line, col, block, operator, lhs, rhs) => {
        irModified = ir
      }

      case LogicalOperation(line, col, block, operator, lhs, rhs) => {
        irModified = ir
      }

      case TernaryOperation(line, col, block, condition, ifTrue, ifFalse) => {
        irModified = ir
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
        irModified = ir.asInstanceOf[Break].copy(
          loop = if (!loop.isEmpty)
            Some(IRto3Addr(loop.get, iter).asInstanceOf[Loop])
            else loop
        )
      }

      case Continue(line, col, loop) => {
        irModified = ir.asInstanceOf[Continue].copy(
          loop = if (!loop.isEmpty)
            Some(IRto3Addr(loop.get, iter).asInstanceOf[Loop])
            else loop
        )
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