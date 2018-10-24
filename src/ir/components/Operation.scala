package ir.components

trait UnaryOperation extends Expression

case class Not(line: Int, col: Int, expression: Expression) extends UnaryOperation {
  def typ: Option[Type] = Option(BoolType)
  override def toString: String = s"[Not] ${typ.get}  (${line}:${col})"
}

case class Negate(line: Int, col: Int, expression: Expression) extends UnaryOperation {
  def typ: Option[Type] = Option(IntType)
  override def toString: String = s"[Negate] ${typ.get}  (${line}:${col})"
}

trait BinaryOperation extends Expression {
  def lhs: Expression
  def rhs: Expression
}

case class ArithmeticOperation(
    line: Int,
    col: Int,
    operator: ArithmeticOperator,
    lhs: Expression,
    rhs: Expression) extends BinaryOperation {

  def typ: Option[Type] = Option(IntType)
  override def toString: String = s"[ArithmeticOperation] ${operator} ${typ.get}  (${line}:${col})"
}

case class LogicalOperation(
    line: Int,
    col: Int,
    operator: LogicalOperator,
    lhs: Expression,
    rhs: Expression) extends BinaryOperation {

  def typ: Option[Type] = Option(BoolType)
  override def toString: String = s"[LogicalOperation] ${operator} ${typ.get}  (${line}:${col})"
}

case class TernaryOperation(
    line: Int,
    col: Int,
    condition: Expression,
    ifTrue: Expression,
    ifFalse: Expression) extends Expression {

  def typ: Option[Type] = if (ifTrue.typ == ifFalse.typ) ifTrue.typ else None
  override def toString: String = s"[TernaryOperation] ${typ.get}  (${line}:${col})"
}
