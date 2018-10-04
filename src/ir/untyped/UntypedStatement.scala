package ir.untyped

trait UntypedStatement extends UntypedIR

case class UntypedBreak(line: Int, col: Int) extends UntypedStatement {
  override def toString: String = s"break ${line}:${col}"
}

case class UntypedContinue(line: Int, col: Int) extends UntypedStatement {
  override def toString: String = s"continue ${line}:${col}"
}

case class UntypedReturn(line: Int, col: Int, value: UntypedExpression) extends UntypedStatement {
  override def toString: String = s"return ${line}:${col}"
}
