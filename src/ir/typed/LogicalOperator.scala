package ir.typed

trait LogicalOperator extends IR {
  def typ: Type = VoidType
}

case class And(line: Int, col: Int) extends LogicalOperator {
  override def toString: String = s"And ${line}:${col}"
}
case class Or(line: Int, col: Int) extends LogicalOperator {
  override def toString: String = s"Or ${line}:${col}"
}
case class Equal(line: Int, col: Int) extends LogicalOperator {
  override def toString: String = s"Equal ${line}:${col}"
}
case class NotEqual(line: Int, col: Int) extends LogicalOperator {
  override def toString: String = s"NotEqual ${line}:${col}"
}
case class GreaterThan(line: Int, col: Int) extends LogicalOperator {
  override def toString: String = s"GreaterThan ${line}:${col}"
}
case class GreaterThanOrEqual(line: Int, col: Int) extends LogicalOperator {
  override def toString: String = s"GreaterThanOrEqual ${line}:${col}"
}
case class LessThan(line: Int, col: Int) extends LogicalOperator {
  override def toString: String = s"LessThan ${line}:${col}"
}
case class LessThanOrEqual(line: Int, col: Int) extends LogicalOperator {
  override def toString: String = s"LessThanOrEqual ${line}:${col}"
}
