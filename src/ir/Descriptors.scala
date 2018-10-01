package ir

import scala.collection.immutable.HashMap
import scala.collection.mutable.ListBuffer

trait Descriptor {
  def name: String
  def line: Int
  def column: Int
  // stack offset
  // memory address
}

case class ClassDescriptor(
  name: String,
  line: Int,
  column: Int,
  methods: SymbolTable[MethodDescriptor]
) extends Descriptor

case class ParameterDescriptor(
  name: String,
  line: Int,
  column: Int,
  types: String
) extends Descriptor

case class VariableDescriptor(
  name: String,
  line: Int,
  column: Int,
  types: String,
  value: String
) extends Descriptor

class MethodDescriptor(
  methodName: String,
  line: Int,
  column: Int,
  types: HashMap[Vector[ParameterDescriptor], String],
  codes: HashMap[Vector[ParameterDescriptor], ScalarAST]
) extends Descriptor {

  private val _name = methodName

  def name = _name

  // get the reutrn type corresponding to the given parameters
  def returnType(params: Vector[ParameterDescriptor]): String = throw RuntimeException()

  // get the ScalarAST corresponding to the given parameters
  def code(params: Vector[ParameterDescriptor]): ScalarAST = throw RuntimeException()

}
