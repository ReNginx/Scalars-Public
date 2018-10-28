package ir.components

trait Statement extends IR {
  def line: Int
  def col: Int
}

case class Break(
  line: Int,
  col: Int,
  var loop: Option[Loop] = None) extends Statement {
  override def toString: String = s"[Break]  (${line}:${col})"
}

case class Continue(
  line: Int,
  col: Int,
  var loop: Option[Loop] = None) extends Statement {
  override def toString: String = s"[Continue]  (${line}:${col})"
}

case class Return(
  line: Int,
  col: Int,
  var value: Option[Expression],
  valueBlock: Option[Block]) extends Statement {
  override def toString: String = s"[Return]  (${line}:${col})"
}

case class If(
    line: Int,
    col: Int,
    condition: Expression,
    conditionBlock: Option[Block],
    ifTrue: Block,
    ifFalse: Option[Block]) extends Statement {

  override def toString: String = s"[If]  (${line}:${col})"
}
