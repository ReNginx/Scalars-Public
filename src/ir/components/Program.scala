package ir.components

trait IR

case class Program(
    line: Int,
    col: Int,
    imports: Vector[ExtMethodDeclaration],
    fields: Vector[FieldDeclaration],
    methods: Vector[LocMethodDeclaration]) extends IR {

  override def toString: String = s"[Program]  (${line}:${col})"
}

// value is store in the last register
case class Block(
    line: Int,
    col: Int,
    declarations: Vector[FieldDeclaration],
    statements: Vector[Statement]) extends IR {

  override def toString: String = s"[Block]  (${line}:${col})"
}
