package ir.typed

trait Call extends Expression with Statement


case class MethodCall(line: Int, col: Int,
                      name: String,
                      params: Vector[Expression]) extends Call {
  var method: Option[MethodsDeclaration] = None

  def typ: Option[Type] = method match {
    case Some(x) => x.typ
    case None => None
  }
  override def toString: String = s"MethodCall ${typ} ${line}:${col}"
}
