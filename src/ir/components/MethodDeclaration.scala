package ir.components

trait MethodDeclaration extends MemberDeclaration {
  val line: Int
  val col: Int
  val name: String
  val typ: Option[Type]
}

case class LocMethodDeclaration(
    line: Int,
    col: Int,
    name: String,
    typ: Option[Type],
    params: Vector[FieldDeclaration],
    block: Block) extends MethodDeclaration {

  override def toString: String = s"[MethodDeclaration] ${typ.get} ${name}  (${line}:${col})"
}

case class ExtMethodDeclaration(
    line: Int,
    col: Int,
    name: String,
    typ: Option[Type]=Option(IntType)) extends MethodDeclaration {
      
  override def toString: String = s"[MethodDeclaration] ${typ.get} ${name}  (${line}:${col})"
}
