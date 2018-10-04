package ir.typed

import ir.untyped.UntypedAssignment

trait Assignment extends Statement with UntypedAssignment {
  override def typ: Type = VoidType
}

case class AssignStatement(line: Int, col: Int, location: Location, value: Expression) extends Assignment {
  override def toString: String = s"AssignStatement ${line}:${col}"
}

case class CompoundAssignStatement(line: Int, col: Int, location: Location, value: Expression, operator: ArithmeticOperator) extends Assignment {
  override def toString: String = s"CompoundAssignStatement ${operator} ${line}:${col}"
}
