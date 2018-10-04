package ir.untyped

trait UntypedLoop extends UntypedStatement {
  def condition: UntypedLogicalOperation
  def ifTrue: UntypedBlock
}

case class For(
  line: Int,
  col: Int,
  start: UntypedAssignStatement,
  condition: UntypedLogicalOperation,
  update: UntypedAssignment,
  ifTrue: UntypedBlock
) extends UntypedLoop {

  override def toString: String = s"For ${line}:${col}"

}

case class While(
  line: Int,
  col: Int,
  condition: UntypedLogicalOperation,
  ifTrue: UntypedBlock
) extends UntypedLoop {

  override def toString: String = s"While ${line}:${col}"

}
