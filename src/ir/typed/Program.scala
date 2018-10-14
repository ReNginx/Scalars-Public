package ir.typed

trait IR

case class Program(line: Int, col: Int,
                   imports: Vector[ExtMethodDeclaration],
                   fields: Vector[FieldDeclaration],
                   methods: Vector[LocMethodDeclaration]) extends IR {
  //val typ: Option[Type] = Option(VoidType)

  override def toString: String = s"Program ${line}:${col}"
}

case class Block(line: Int, col: Int, declarations: Vector[FieldDeclaration], statements: Vector[Statement]) extends IR {
  //val typ: Option[Type] = Option(VoidType)
  override def toString: String = s"Block ${line}:${col}"
}
