package ir.untyped

import ir.typed.Location
import ir.typed.Expression
import ir.typed.ArithmeticOperator

trait UntypedAssignment extends UntypedStatement

case class UntypedAssignStatement(line: Int, col: Int, location: Location, value: Expression) extends UntypedAssignment {
  override def toString: String = s"AssignStatement ${line}:${col}"
}

case class UntypedCompoundAssignStatement(line: Int, col: Int, location: Location, value: Expression, operator: ArithmeticOperator) extends UntypedAssignment {
  override def toString: String = s"CompoundAssignStatement ${operator} ${line}:${col}"
}
