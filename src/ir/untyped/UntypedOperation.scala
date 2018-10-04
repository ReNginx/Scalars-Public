package ir.untyped

import ir.typed.ArithmeticOperator
import ir.typed.LogicalOperator
import ir.typed.LogicalOperation

trait UntypedUnaryOperation extends UntypedExpression

case class UntypedIncrement(line: Int, col: Int, location: UntypedLocation) extends UntypedUnaryOperation  {
  override def toString: String = s"Increment ${line}:${col}"
}

case class UntypedDecrement(line: Int, col: Int, location: UntypedLocation) extends UntypedUnaryOperation  {
  override def toString: String = s"Decrement ${line}:${col}"
}

case class UntypedNot(line: Int, col: Int, expression: UntypedExpression) extends UntypedUnaryOperation {
  override def toString: String = s"line ${line}:${col}"
}

trait BinaryOperation extends UntypedExpression {
  def lhs: UntypedExpression
  def rhs: UntypedExpression
}

case class UntypedArithmeticOperation(line: Int, col: Int, operator: ArithmeticOperator, lhs: UntypedExpression, rhs: UntypedExpression) extends BinaryOperation {
  override def toString: String = s"ArithmeticOperation ${line}:${col}"
}

case class UntypedLogicalOperation(line: Int, col: Int, operator: LogicalOperator, lhs: UntypedExpression, rhs: UntypedExpression) extends BinaryOperation {
  override def toString: String = s"LogicalOperation ${line}:${col}"
}

case class UntypedTernaryOperation(line: Int, col: Int, condition: LogicalOperation, ifTrue: UntypedExpression, ifFalse: UntypedExpression) extends UntypedExpression {
  override def toString: String = s"TernaryOperation ${line}:${col}"
}
