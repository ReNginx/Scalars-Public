package ir.components

trait Expression extends IR {
  def typ: Option[Type]
  def eval: Option[Location] = None
  def block: Option[Block] = None
  def rep: String = "" //only location and literal would have this.
}

case class Length(line: Int, col: Int, location: Location) extends Expression {
  val typ = Option(IntType)
  override def toString: String = s"[Length]  (${line}:${col})"
}

case class Location(
    line: Int,
    col: Int,
    name: String,
    var index: Option[Expression],  // lcoation or int linteral
    indexBlock: Option[Block],
    var field: Option[FieldDeclaration] = None) extends Expression {

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

  override def rep: String = {
    assert(field.isDefined)
    field.get match {
      case variable: VariableDeclaration => {
        return variable.rep
      }
      case ary: ArrayDeclaration => {
        if (ary.isGlobal)
          return s"${ary.name}(, ${index.get.rep}, 8)"
        else
          return s"${ary.offset}(%rbp, ${index.get.rep}, 8)"
      }
      case _ =>
    }
    ""
  }

  //TODO
  def indexCheck: Vector[String] = {
    return Vector[String]()
  }

  override def toString: String = s"[Location] ${name}  (${line}:${col})"
}
