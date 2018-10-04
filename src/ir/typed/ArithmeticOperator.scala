package ir.typed

trait ArithmeticOperator extends IR {
  def typ: Type = VoidType
}

case class Add(line: Int, col: Int) extends ArithmeticOperator {
  override def toString: String = s"Add ${line}:${col}"
}
case class Divide(line: Int, col: Int) extends ArithmeticOperator {
  override def toString: String = s"Divide ${line}:${col}"
}
case class Modulo(line: Int, col: Int) extends ArithmeticOperator {
  override def toString: String = s"Modulo ${line}:${col}"
}
case class Multiply(line: Int, col: Int) extends ArithmeticOperator {
  override def toString: String = s"Multiply ${line}:${col}"
}
case class Subtract(line: Int, col: Int) extends ArithmeticOperator {
  override def toString: String = s"Subtract ${line}:${col}"
}
