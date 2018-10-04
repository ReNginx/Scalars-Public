package ir.typed

import ir.untyped.UntypedArithmeticOperation
import ir.untyped.UntypedLogicalOperation
import ir.untyped.UntypedTernaryOperation

trait UnaryOperation extends Expression

case class Increment(line: Int, col: Int, typ: Type, location: Location) extends UnaryOperation  {
  override def toString: String = s"Increment ${typ} ${line}:${col}"
}

case class Decrement(line: Int, col: Int, typ: Type, location: Location) extends UnaryOperation  {
  override def toString: String = s"Decrement ${typ} ${line}:${col}"
}

case class Not(line: Int, col: Int, typ: Type, expression: Expression) extends UnaryOperation {
  override def toString: String = s"line ${typ} ${line}:${col}"
}

case class Negate(line: Int, col: Int, typ: Type, expression: Expression) extends UnaryOperation {
  override def toString: String = s"line ${typ} ${line}:${col}"
}

trait BinaryOperation extends Expression {
  def lhs: Expression
  def rhs: Expression
}

case class ArithmeticOperation(line: Int, col: Int, typ: Type, operator: ArithmeticOperator, lhs: Expression, rhs: Expression) extends BinaryOperation {
  override def toString: String = s"ArithmeticOperation ${typ} ${line}:${col}"
}

case class LogicalOperation(line: Int, col: Int, typ: Type, operator: LogicalOperator, lhs: Expression, rhs: Expression) extends BinaryOperation {
  override def toString: String = s"LogicalOperation ${typ} ${line}:${col}"
}

case class TernaryOperation(line: Int, col: Int, typ: Type, condition: LogicalOperation, ifTrue: Expression, ifFalse: Expression) extends Expression {
  override def toString: String = s"TernaryOperation ${typ} ${line}:${col}"
}
