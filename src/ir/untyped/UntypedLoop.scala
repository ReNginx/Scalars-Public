package ir.untyped

trait UntypedLoop extends UntypedStatement {
  def condition: UntypedExpression
  def ifTrue: UntypedBlock
}

case class UntypedFor(
  line: Int,
  col: Int,
  start: UntypedAssignStatement,
  condition: UntypedExpression,
  update: UntypedAssignment,
  ifTrue: UntypedBlock
) extends UntypedLoop {

  override def toString: String = s"For ${line}:${col}"

}

case class UntypedWhile(
  line: Int,
  col: Int,
  condition: UntypedExpression,
  ifTrue: UntypedBlock
) extends UntypedLoop {

  override def toString: String = s"While ${line}:${col}"

}
