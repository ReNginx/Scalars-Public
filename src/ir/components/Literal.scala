package ir.components


trait Literal extends Expression {
  def rep: String
}

case class IntLiteral(line: Int, col: Int, value: Long) extends Literal {
  def typ: Option[Type] = Option(IntType)
  def rep: String = "$" + value.toString
  override def toString: String = s"[IntLiteral] ${value}  (${line}:${col})"
}

case class BoolLiteral(line: Int, col: Int, value: Boolean) extends Literal {
  def typ: Option[Type] = Option(BoolType)
  def rep: String = "$" + if (value) "1" else "0"
  override def toString: String = s"[BoolLiteral] ${value}  (${line}:${col})"
}

case class CharLiteral(line: Int, col: Int, value: Char) extends Literal {
  def typ: Option[Type] = Option(CharType)
  def rep: String = "$" + value.toInt.toString
  override def toString: String = s"[CharLiteral] ${value}  (${line}:${col})"
}

case class StringLiteral(line: Int, col: Int, value: String) extends Literal {
  def typ: Option[Type] = Option(StringType)
  def rep: String = value // have no meaning, and should not be called.
  override def toString: String = s"[StringLiteral] ${value}  (${line}:${col})"
}
