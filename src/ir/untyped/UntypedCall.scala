package ir.untyped

import ir.typed.MethodDeclaration

trait UntypedCall extends UntypedExpression with UntypedStatement

case class UntypedCallout(line: Int, col: Int) extends UntypedCall {
  override def toString: String = s"Callout ${line}:${col}"
}

case class UntypedMethodCall(line: Int, col: Int, method: MethodDeclaration, params: Vector[UntypedExpression]) extends UntypedCall {
  override def toString: String = s"MethodCall ${line}:${col}"
}
