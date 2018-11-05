package ir.components

trait LogicalOperator extends Operator

case object And extends LogicalOperator {
  override def toString: String = s"[And]"
}
case object Or extends LogicalOperator {
  override def toString: String = s"[Or]"
}
case object Equal extends LogicalOperator {
  override def toString: String = s"[Equal]"
}
case object NotEqual extends LogicalOperator {
  override def toString: String = s"[NotEqual]"
}
case object GreaterThan extends LogicalOperator {
  override def toString: String = s"[GreaterThan]"
}
case object GreaterThanOrEqual extends LogicalOperator {
  override def toString: String = s"[GreaterThanOrEqual]"
}
case object LessThan extends LogicalOperator {
  override def toString: String = s"[LessThan]"
}
case object LessThanOrEqual extends LogicalOperator {
  override def toString: String = s"[LessThanOrEqual]"
}
