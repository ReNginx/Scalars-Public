package ir.typed

trait MethodsDeclaration extends MemberDeclaration {
  val line: Int
  val col: Int
  val name: String
  val typ: Option[Type]
}

case class LocMethodDeclaration(line: Int,
                                col: Int,
                                name: String,
                                typ: Option[Type],
                                params: Vector[FieldDeclaration],
                                block: Block) extends MethodsDeclaration {
  override def toString: String = s"MethodDeclaration ${typ} ${name} ${line}:${col}"
}

case class ExtMethodDeclaration(line: Int,
                                col: Int, name: String,
                                typ:Option[Type] = Option(IntType)) extends MethodsDeclaration {
  override def toString: String = s"MethodDeclaration ${typ} ${name} ${line}:${col}"
}
