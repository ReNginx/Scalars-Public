package ir.components

import scala.collection.mutable.ArrayBuffer

trait Expression extends IR {
  def typ: Option[Type]

  def eval: Option[Expression] = None

  def block: Option[Block] = None

  def rep: String = "" //only location and literal would have this.
}

case class Length(line: Int, col: Int, location: Location) extends Expression {
  val typ = Option(IntType)

  override def toString: String = s"[Length]  (${line}:${col})"

  override def eval: Option[Expression] = if (!location.field.isEmpty)
    Some(location.field.get.asInstanceOf[ArrayDeclaration].length)
  else None

  override def block: Option[Block] = Some(Block(0, 0, Vector(), Vector()))
}

case class Location(
                     line: Int,
                     col: Int,
                     name: String,
                     var index: Option[Expression], // lcoation or int linteral
                     indexBlock: Option[Block],
                     var field: Option[FieldDeclaration] = None) extends Expression {
  self =>

  override def eval: Option[Expression] = Some(self)

  override def block: Option[Block] = Some(Block(0, 0, Vector(), Vector()))

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
            case IntType => Option(IntegerArrayType)
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
        assert(variable.isGlobal || variable.isReg || variable.offset != 0)
        if (variable.isGlobal)
          s"${variable.name}"
        else if (variable.isReg)
          s"%${variable.reg}"
        else
          s"$$${variable.offset}(%rsp)"
      }
      case ary: ArrayDeclaration => {
        assert(!ary.isGlobal || ary.offset != 0)
        if (ary.isGlobal)
          s"${ary.name}(, ${index.get.rep}, 8)"
        else
          s"${ary.offset}(%rbp, ${index.get.rep}, 8)"
      }
      case reg: Registers => {
        s"%${reg.reg}"
      }
    }
  }

  def indexCheck: Vector[String] = {
    field.get match {
      case array: ArrayDeclaration => {
        val res: ArrayBuffer[String] = ArrayBuffer()
        res += s"movq ${index.get.rep}, %rax"
        res += s"cmpq %rax, $$ 0"
        res += s"jle outOfBound"
        res += s"cmpq %rax, $$${array.length.value}"
        res += s"jg outofBound"
        res.toVector
        Vector[String]()
      }
      case _ => Vector()
    }
  }


  override def toString: String = s"[Location] ${name}  (${line}:${col})"
}
