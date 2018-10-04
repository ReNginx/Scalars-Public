package ir.untyped

import ir.typed.FieldDeclaration
import ir.typed.MethodDeclaration

trait UntypedIR

case class UntypedProgram(line: Int, col: Int, imports: Vector[UntypedImport], fields: Vector[FieldDeclaration], methods: Vector[MethodDeclaration]) extends UntypedIR {
  override def toString: String = s"Program ${line}:${col}"
}

case class UntypedImport(line: Int, col: Int, location: UntypedLocation) extends UntypedIR {
  override def toString: String = s"Import ${line}:${col}"
}

case class UntypedBlock(line: Int, col: Int, declarations: Vector[FieldDeclaration], statements: Vector[UntypedStatement]) extends UntypedIR {
  override def toString: String = s"Block ${line}:${col}"
}
