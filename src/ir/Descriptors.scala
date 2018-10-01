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
  lineNumber: Int,
  columnNumber: Int,
  types: HashMap[Vector[ParameterDescriptor], String],
  codes: HashMap[Vector[ParameterDescriptor], ScalarAST]
) extends Descriptor {

  def line = lineNumber
  def column = columnNumber
  def name = methodName

  // get the reutrn type corresponding to the given parameters
  def returnType(params: Vector[ParameterDescriptor]): String = throw new Exception()

  // get the ScalarAST corresponding to the given parameters
  def code(params: Vector[ParameterDescriptor]): ScalarAST = throw new Exception()

}
