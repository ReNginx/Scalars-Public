package ir.untyped

trait UntypedStatement extends UntypedIR

case class UntypedReturn(line: Int, col: Int, value: UntypedExpression) extends UntypedStatement {
  override def toString: String = s"return ${line}:${col}"
}
