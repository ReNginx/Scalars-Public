package ir

import scala.collection.mutable.ListBuffer

case class ClassDescriptor (
  parentClass: ClassDescriptor,
  fields: SymbolTable,
  methods: SymbolTable
)

case class MethodDescriptor (
  code: ScalarAST,
  localVariables: SymbolTable,
  parameters: SymbolTable
)

// local, parameter, field
case class VariableDescriptor ()

case class TypeDescriptor ()
