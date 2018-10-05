package ir.typed

trait MemberDeclaration extends IR {
  def line: Int
  def col: Int
  def typ: Type
  def name: String
}

case class FieldList(line: Int, col: Int, typ: Type, declarations: Vector[FieldDeclaration]) extends IR {
  override def toString: String = s"FieldList ${line}:${col}"
}


trait FieldDeclaration extends MemberDeclaration {
}

case class VariableDeclaration(line: Int, col: Int, name: String, typ: Type) extends FieldDeclaration {
  override def toString: String = s"VariableDeclaration ${typ} ${name} ${line}:${col}"
}

case class ArrayDeclaration(line: Int, col: Int, name: String, typ: Type, length: IntLiteral) extends FieldDeclaration {
  override def toString: String = s"ArrayDeclaration ${typ} ${name} ${line}:${col}"
}
