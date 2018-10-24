package ir.components

trait Statement extends IR

case class Break(line: Int, col: Int, var loop: Option[Loop] = None) extends Statement {
  override def toString: String = s"break ${line}:${col}"
}

case class Continue(line: Int, col: Int, var loop: Option[Loop] = None) extends Statement {
  override def toString: String = s"continue ${line}:${col}"
}

case class Return(line: Int, col: Int, value: Option[Expression] = None) extends Statement {
  override def toString: String = s"return ${line}:${col}"
}

case class If(
    line: Int,
    col: Int,
    condition: Expression,
    ifTrue: Block,
    ifFalse: Option[Block]) extends Statement {
      
  override def toString: String = s"if ${line}:${col}"
}
