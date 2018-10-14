package ir.typed

trait Assignment extends Statement

case class AssignStatement(line: Int, col: Int, location: Location, value: Expression) extends Assignment {
  override def toString: String = s"AssignStatement ${line}:${col}"
}

case class CompoundAssignStatement(line: Int, col: Int, location: Location, value: Expression, operator: ArithmeticOperator) extends Assignment {
  override def toString: String = s"CompoundAssignStatement ${operator} ${line}:${col}"
}

case class Increment(line: Int, col: Int, location: Location) extends Assignment {
  override def toString: String = s"Increment ${line}:${col}"
}

case class Decrement(line: Int, col: Int, location: Location) extends Assignment {
  override def toString: String = s"Decrement ${line}:${col}"
}