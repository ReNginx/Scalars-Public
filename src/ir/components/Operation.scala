package ir.components

trait Operation extends Expression{
  def line: Int
  def col: Int
  var eval: Option[Location]
}

trait UnaryOperation extends Operation {
  def expression: Expression

  override def hashCode: Int = expression.hashCode
  override def equals(obj: Any): Boolean = {
    obj.isInstanceOf[UnaryOperation] &&
    obj.hashCode == this.hashCode
  }
}

case class Not(
    line: Int,
    col: Int,
    var eval: Option[Location],
    override val block: Option[Block],
    var expression: Expression) extends UnaryOperation {

  def typ: Option[Type] = Option(BoolType)
  override def toString: String = s"[Not] ${typ.get}  (${line}:${col})"
}

case class Negate(
    line: Int,
    col: Int,
    override var eval: Option[Location],
    override val block: Option[Block],
    var expression: Expression) extends UnaryOperation {

  def typ: Option[Type] = Option(IntType)
  override def toString: String = s"[Negate] ${typ.get}  (${line}:${col})"
}

trait BinaryOperation extends Operation {
  def lhs: Expression
  def rhs: Expression

  override def hashCode: Int = lhs.hashCode + rhs.hashCode
  override def equals(obj: Any): Boolean = {
    obj.isInstanceOf[UnaryOperation] &&
    obj.hashCode == this.hashCode
  }
}

case class ArithmeticOperation(
    line: Int,
    col: Int,
    override var eval: Option[Location],
    override val block: Option[Block],
    operator: ArithmeticOperator,
    var lhs: Expression,
    var rhs: Expression) extends BinaryOperation {

  def typ: Option[Type] = Option(IntType)
  override def toString: String = s"[ArithmeticOperation] ${operator} ${typ.get}  (${line}:${col})"
}

case class LogicalOperation(
    line: Int,
    col: Int,
    override var eval: Option[Location],
    override val block: Option[Block],
    operator: LogicalOperator,
    var lhs: Expression,
    var rhs: Expression) extends BinaryOperation {

  def typ: Option[Type] = Option(BoolType)
  override def toString: String = s"[LogicalOperation] ${operator} ${typ.get}  (${line}:${col})"
}

case class TernaryOperation(
    line: Int,
    col: Int,
    override var eval: Option[Location],
    override val block: Option[Block],
    condition: Expression,
    var ifTrue: Expression,
    var ifFalse: Expression) extends  Operation {

  def typ: Option[Type] = if (ifTrue.typ == ifFalse.typ) ifTrue.typ else None
  override def toString: String = s"[TernaryOperation] ${typ.get}  (${line}:${col})"
}
