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
  val isReg: Boolean = false
  var isGlobal: Boolean = false
  var reg: String = ""
  var offset: Int = 0
  def typ: Option[Type]
  def rep = {
    if (isGlobal)
      s"${name}"
    else if (isReg)
      s"%${reg}"
    else
      s"${offset}(%rbp)"
  }
}

case class VariableDeclaration(
    line: Int,
    col: Int,
    name: String,
    typ: Option[Type]) extends FieldDeclaration {

  override def toString: String = s"[VariableDeclaration] ${typ.get} ${name}  (${line}:${col})"
}

case class ArrayDeclaration(
    line: Int,
    col: Int,
    name: String,
    length: IntLiteral,
    typ: Option[Type]) extends FieldDeclaration {

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

  override val isReg: Boolean = true
}
