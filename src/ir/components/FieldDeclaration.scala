package ir.components

trait MemberDeclaration extends IR {
  val line: Int
  val col: Int
  val name: String
}

case class FieldList(
    line: Int,
    col: Int,
    typ: Option[Type],
    declarations: Vector[FieldDeclaration]) extends IR {

  override def toString: String = s"[FieldList]  (${line}:${col})"
}


trait FieldDeclaration extends MemberDeclaration {
  def typ: Option[Type]
}

case class VariableDeclaration(
    line: Int,
    col: Int,
    name: String,
    typ: Option[Type]) extends FieldDeclaration {

  override def toString: String = s"[VariableDeclaration] ${typ.get} ${name}  (${line}:${col})"
}

case class ArrayDeclaration(
    line: Int,
    col: Int,
    name: String,
    length: IntLiteral,
    typ: Option[Type]) extends FieldDeclaration {

  override def toString: String = s"[ArrayDeclaration] ${typ.get} ${name}[${length}]  (${line}:${col})"
}
