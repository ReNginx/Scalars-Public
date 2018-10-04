package ir.typed

import ir.untyped.UntypedExpression
import ir.untyped.UntypedIR
import ir.untyped.UntypedStatement

trait Statement extends IR with UntypedStatement

case class Break(line: Int, col: Int, loop: Loop) extends Statement {
  def typ: Type = VoidType
  override def toString: String = s"break ${line}:${col}"
}

case class Continue(line: Int, col: Int, loop: Loop) extends Statement {
  def typ: Type = VoidType
  override def toString: String = s"continue ${line}:${col}"
}

case class Return(line: Int, col: Int, _typ: Type, value: Expression) extends Statement {
  override def typ: Type = _typ
  override def toString: String = s"return ${typ} ${line}:${col}"
}

case class If(line: Int, col: Int, condition: LogicalOperation, ifTrue: Block, ifFalse: Option[Block]) extends Statement {
  def typ: Type = VoidType
  override def toString: String = s"if ${line}:${col}"
}
