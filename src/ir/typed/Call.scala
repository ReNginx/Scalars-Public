package ir.typed

trait Call extends Expression with Statement

case class Callout(line: Int, col: Int, typ: Type, params: Vector[Expression]) extends Call {
  override def toString: String = s"Callout ${typ} ${line}:${col}"
}

case class MethodCall(line: Int, col: Int, typ: Type, method: MethodDeclaration, params: Vector[Expression]) extends Call {
  override def toString: String = s"MethodCall ${typ} ${line}:${col}"
}
