package ir.components

trait Assignment extends Statement

case class AssignStatement(
    line: Int,
    col: Int,
    loc: Location,
    value: Expression) extends Assignment {
    
  def typ: Option[Type] = loc.typ

  override def toString: String = s"[AssignStatement] ${typ.get}  (${line}:${col})"
}

case class CompoundAssignStatement(
    line: Int,
    col: Int,
    loc: Location,
    value: Expression,
    operator: ArithmeticOperator) extends Assignment {

  override def toString: String = s"[CompoundAssignStatement] ${operator}  (${line}:${col})"
}

case class Increment(line: Int, col: Int, loc: Location) extends Assignment {
  override def toString: String = s"[Increment]  (${line}:${col})"
}

case class Decrement(line: Int, col: Int, loc: Location) extends Assignment {
  override def toString: String = s"[Decrement]  (${line}:${col})"
}
