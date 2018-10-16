package ir.components

trait Expression extends IR {
  def typ: Option[Type]
}

case class Length(line: Int, col: Int, location: Location) extends Expression {
  val typ = Option(IntType)

  override def toString: String = s"Length ${line}:${col}"
}

case class Location(
    line: Int,
    col: Int,
    name: String,
    index: Option[Expression]) extends Expression {

  var field: Option[FieldDeclaration] = None

  override def typ: Option[Type] = {
    if (field.isEmpty) {
      return None
    }

    field.get match {
      case varDecl: VariableDeclaration => varDecl.typ
      case arrayDecl: ArrayDeclaration => {
        if (index.isDefined) {
          arrayDecl.typ
        } else {
          arrayDecl.typ.get match {
            case IntType  => Option(IntegerArrayType)
            case BoolType => Option(BoolArrayType)
          }
        }
      }
    }
  }

  override def toString: String = s"Location ${line}:${col}"
}
