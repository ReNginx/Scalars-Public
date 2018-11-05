package ir.components

import scala.collection.mutable.ArrayBuffer

trait Expression extends IR {
  def typ: Option[Type]

  def eval: Option[Expression]

  def block: Option[Block] = None

  def rep: String = "" //only location and literal would have this.

  /*
    In the return, Vector[String] contains the additional asm for array,
    and String is just your basic rep.
  */
  def getRep (baseReg: String): (Vector[String], String) = (Vector(), rep)

  def cfgRep: String = ""
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
                    var index: Option[Expression], // location or int linteral
                    var field: Option[FieldDeclaration] = None) extends Expression {
  self =>

  var evalLoc: Option[Expression] = None
  var blockLoc: Option[Block] = None

  override def eval: Option[Expression] = Some(self)
  override def block: Option[Block] = if (!blockLoc.isEmpty) blockLoc else None

  override def hashCode: Int = index.hashCode + field.hashCode
  override def equals(obj: Any): Boolean = {
    obj.isInstanceOf[Location] &&
    obj.hashCode == this.hashCode
  }

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
        variable.rep
      }
      case ary: ArrayDeclaration => {
        assert(ary.isGlobal || ary.offset != 0)
        if (ary.isGlobal)
          s"${ary.name}(, ${index.get.rep}, 8)"
        else
          s"${ary.offset}(%rbp, ${index.get.rep}, 8)"
      }
      case reg: Registers => {
        s"${reg.rep}"
      }
    }
  }

  override def getRep (baseReg: String): (Vector[String], String) = {
    var resVec: ArrayBuffer[String] = ArrayBuffer()
    var resStr: String = ""
    field.get match {
      case ary: ArrayDeclaration => {
        val (resVecAry, resStrAry) = makeArrayRep(baseReg)
        resVec ++= resVecAry
        resStr = resStrAry
      }
      case _ => {
        resStr = rep
      }
    }
    (resVec.toVector, resStr)
  }

  private def makeArrayRep (baseReg: String): (Vector[String], String) = {
    assert(baseReg(0) == '%') // sanity check
    assert(!field.isEmpty)
    val ary = field.get
    assert(ary.isInstanceOf[ArrayDeclaration])
    assert(ary.isGlobal || ary.offset != 0)
    val resVec: ArrayBuffer[String] = ArrayBuffer()
    resVec += s"\tmovq ${index.get.rep}, ${baseReg}"
    var resStr = ""
    if (ary.isGlobal) {
      resStr = s"${ary.name}(, ${baseReg}, 8)"
    } else {
      /*
      resVec += s"\taddq $$${-ary.offset}, ${baseReg}"
      resStr = s"(%rbp, ${baseReg}, 8)"
      */
      resStr = s"${ary.offset}(%rbp, ${baseReg}, 8)"
    }
    (resVec.toVector, resStr)
  }

  override def cfgRep: String = {
    assert(field.isDefined)
    field.get.name
  }

  def indexCheck: Vector[String] = {
    field.get match {
      case array: ArrayDeclaration => {
        val res: ArrayBuffer[String] = ArrayBuffer()
        res += s"\tmovq ${index.get.rep}, %rax"
        res += s"\tmovq $$0, %r15"
        res += s"\tcmpq %r15, %rax"
        res += s"\tjl outOfBound"
        res += s"\tmovq $$${array.length.value}, %r15"
        res += s"\tcmpq %r15, %rax"
        res += s"\tjge outOfBound"
        res.toVector
        //Vector[String]()
      }
      case _ => Vector()
    }
  }


  override def toString: String = s"[Location] ${name}  (${line}:${col})"
}
