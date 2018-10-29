package ir.components


trait Loop extends Statement {
  def condition: Expression
  def ifTrue: Block
}

case class For(
    line: Int,
    col: Int,
    start: AssignStatement,
    condition: Expression,
    update: Assignment,
    var ifTrue: Block) extends Loop {

  override def toString: String = s"[For]  (${line}:${col})"
}

case class While(
    line: Int,
    col: Int,
    condition: Expression,
    var ifTrue: Block) extends Loop {

  override def toString: String = s"[While]  (${line}:${col})"
}
