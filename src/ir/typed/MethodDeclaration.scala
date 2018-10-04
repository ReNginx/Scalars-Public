package ir.typed

case class MethodDeclaration(line: Int, col: Int, name: String, typ: Type, params: Vector[FieldDeclaration], block: Block) extends MemberDeclaration {
  override def toString: String = s"MethodDeclaration ${typ} ${name} ${line}:${col}"
}
