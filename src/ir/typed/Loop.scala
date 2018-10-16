package ir.typed

import ir.untyped.UntypedLoop

trait Loop extends Statement {
  def condition: Expression
  def ifTrue: Block
  def typ: Type = VoidType
}

case class For(
  line: Int,
  col: Int,
  start: AssignStatement,
  condition: Expression,
  update: Assignment,
  ifTrue: Block
) extends Loop {

  override def toString: String = s"For ${line}:${col}"

}

case class While(
  line: Int,
  col: Int,
  condition: Expression,
  ifTrue: Block
) extends Loop {

  override def toString: String = s"While ${line}:${col}"

}
