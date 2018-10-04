package ir.typed

trait MemberDeclaration extends IR {
  def typ: Type
}

trait FieldDeclaration extends MemberDeclaration {
  def name: String
}

case class VariableDeclaration(line: Int, col: Int, name: String, typ: Type) extends FieldDeclaration {
  override def toString: String = s"VariableDeclaration ${typ} ${name} ${line}:${col}"
}

case class ArrayDeclaration(line: Int, col: Int, name: String, typ: Type, length: IntLiteral) extends FieldDeclaration {
  override def toString: String = s"ArrayDeclaration ${typ} ${name} ${line}:${col}"
}
