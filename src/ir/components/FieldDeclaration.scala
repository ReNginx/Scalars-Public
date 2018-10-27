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
  def indexCheck: Vector[String]
}

case class VariableDeclaration(
    line: Int,
    col: Int,
    name: String,
    typ: Option[Type]) extends FieldDeclaration {

  var isGlobal: Boolean = false
  var offset: Int = 0
  def rep = s"$${offset.toString}(%rbp)"
  override def indexCheck: Vector[String] = Vector[String]()
  override def toString: String = s"[VariableDeclaration] ${typ.get} ${name}  (${line}:${col})"
}

case class ArrayDeclaration(
    line: Int,
    col: Int,
    name: String,
    length: IntLiteral,
    typ: Option[Type]) extends FieldDeclaration {

  var isGlobal: Boolean = false
  var offset: Int = 0
  override def indexCheck: Vector[String] = {
    // val res: ArrayBuffer[String] = ArrayBuffer()
    // res += s"movq ${index.get.rep}, %rax"
    // res += s"cmpq %rax, $$0"
    // res += s"jle outOfBound"
    // res += s"cmpq %rax, $$${index}"
    // res += s"jg outofBound"
    // res.toVector
    Vector[String]()
  }
  override def toString: String = s"[ArrayDeclaration] ${typ.get} ${name}[${length}]  (${line}:${col})"
}
