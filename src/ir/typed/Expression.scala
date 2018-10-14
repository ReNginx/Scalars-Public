package ir.typed

trait Expression extends IR {
  def typ: Option[Type]
}

case class Length(line: Int, col: Int, location: Location) extends Expression {
  val typ = Option(IntType)

  override def toString: String = s"Length ${line}:${col}"
}

case class Location(line: Int, col: Int, name: String, index: Option[Expression]) extends Expression {
  var field: Option[FieldDeclaration] = None
  override def typ: Option[Type] = {
    field match {
      case Some(x) => {
          x match {
            case v: VariableDeclaration => v.typ
            case a: ArrayDeclaration => {
              if (index.isDefined)
                a.typ
              else
                a.typ.get match {
                  case IntType => Option(IntegerArrayType)
                  case BoolType => Option(BoolArrayType)
                }
            }
          }
      }
      case None => None
    }
  }

  override def toString: String = s"Location ${line}:${col}"
}
