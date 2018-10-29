package ir.components
import scala.collection.mutable.{ArrayBuffer}

trait MemberDeclaration extends IR {
  val line: Int
  val col: Int
  val name: String
}

case class FieldList(
    line: Int,
    col: Int,
    typ: Option[Type],
    declarations: Vector[FieldDeclaration]) extends IR {

  override def toString: String = s"[FieldList]  (${line}:${col})"
}


trait FieldDeclaration extends MemberDeclaration {
  def typ: Option[Type]
}

case class VariableDeclaration(
    line: Int,
    col: Int,
    name: String,
    typ: Option[Type]) extends FieldDeclaration {

  var isGlobal: Boolean = false
  var isReg: Boolean = false
  var reg: String = ""
  var offset: Int = 0
  def rep = {
    if (isGlobal)
      s"${name}"
    else if (isReg)
      s"%${reg}"
    else
      s"$$${offset}(%rsp)"
  }
  override def toString: String = s"[VariableDeclaration] ${typ.get} ${name}  (${line}:${col})"
}

case class ArrayDeclaration(
    line: Int,
    col: Int,
    name: String,
    length: IntLiteral,
    typ: Option[Type]) extends FieldDeclaration {

  var isGlobal: Boolean = false
  val isReg: Boolean = false
  val reg: String = ""
  var offset: Int = 0

  override def toString: String = s"[ArrayDeclaration] ${typ.get} ${name}[${length}]  (${line}:${col})"
}

/**
  * explicitly represent registers.
  * @param line: no use
  * @param col: no use
  * @param name a valid register name, without '%' mark
  * @param typ no use
  */
case class Registers(
     name: String,
     line: Int = 0,
     col: Int = 0,
     typ: Option[Type] = None) extends FieldDeclaration {

  val isGlobal: Boolean = false
  val isReg: Boolean = true
  var reg: String = ""
  val offset: Int = 0
  def rep = s"%{name}"
}
