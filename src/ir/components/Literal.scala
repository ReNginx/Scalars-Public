package ir.components


trait Literal extends Expression { self =>
  override def eval: Option[Expression] = Some(self)
  override def block: Option[Block] = Some(Block(0, 0, Vector(), Vector()))
}

case class IntLiteral(line: Int, col: Int, value: Long) extends Literal {
  def typ: Option[Type] = Option(IntType)
  override def rep: String = "$" + value.toString
  override def toString: String = s"[IntLiteral] ${value}  (${line}:${col})"
}

case class BoolLiteral(line: Int, col: Int, value: Boolean) extends Literal {
  def typ: Option[Type] = Option(BoolType)
  override def toString: String = s"[BoolLiteral] ${value}  (${line}:${col})"
  override def rep: String = "$" + (if (value) "1" else "0")
}

case class CharLiteral(line: Int, col: Int, value: Char) extends Literal {
  def typ: Option[Type] = Option(CharType)
  override def rep: String = "$" + value.toInt.toString
  override def toString: String = s"[CharLiteral] ${value}  (${line}:${col})"
}

case class StringLiteral(line: Int, col: Int, value: String) extends Literal {
  def typ: Option[Type] = Option(StringType)
  override def rep: String = value // have no meaning, and should not be called.
  override def toString: String = s"[StringLiteral] ${value}  (${line}:${col})"
}
