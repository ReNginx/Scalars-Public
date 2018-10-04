package ir.untyped

import ir.typed.ArrayDeclaration
import ir.typed.Literal
import ir.typed.FieldDeclaration

trait UntypedExpression extends UntypedIR

case class UntypedLength(line: Int, col: Int, array: ArrayDeclaration) extends UntypedExpression {
  override def toString: String = s"Length ${line}:${col}"
}

case class UntypedLocation(line: Int, col: Int, name: String, index: UntypedExpression) extends UntypedExpression {
  override def toString: String = s"Location ${name} ${line}:${col}"
}
