package ir

import scala.collection.immutable.HashMap
import scala.collection.mutable.ListBuffer

import edu.mit.compilers.grammar.DecafParserTokenTypes

trait Descriptor {
  def name: String
  def line: Int
  def column: Int
}

case class ProgramDescriptor(
  name: String,
  line: Int,
  column: Int,
  fields:  SymbolTable[VariableDescriptor],
  methods: SymbolTable[MethodDescriptor]
) extends Descriptor

case class MethodDescriptor(
  name: String,
  line: Int,
  column: Int,
  variables:   HashMap[Vector[ParameterDescriptor], SymbolTable[VariableDescriptor]],
  returnTypes: HashMap[Vector[ParameterDescriptor], String],
  codes:       HashMap[Vector[ParameterDescriptor], ScalarAST]
) extends Descriptor

case class ParameterDescriptor(
  name: String,
  line: Int,
  column: Int,
  paramType: DecafParserTokenTypes  // either int or bool
) extends Descriptor

case class VariableDescriptor(
  name: String,
  line: Int,
  column: Int,
  varType: DecafParserTokenTypes,  // either int or bool
  value: String
) extends Descriptor

case class BooleanDescriptor(
  name: String,
  line: Int,
  column: Int,
  value: String
) extends Descriptor
