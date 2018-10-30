package ir.components

trait Call extends Expression with Statement


case class MethodCall(
    line: Int,
    col: Int,
    name: String,
    params: Vector[Expression],
    var method: Option[MethodDeclaration] = None,
    override val eval: Option[Location] = None
    ) extends Call {

  def typ: Option[Type] = method match {
    case Some(x) => x.typ
    case None => None
  }
  
  override def toString: String = s"[MethodCall] ${typ.get} ${name}  (${line}:${col})"
}
