# Typed
  for every case class, add line column
  ```scala
  case class UntypedProgram(line: Int, col: Int, imports: Vector[Import], fields: Vector[FieldDeclaration], methods: Vector[UntypedMethodDeclaration])
  ├── case class Import(line: Int, col: Int, method: UntypedMethodDeclaration)  // returns int nay valid signature
  ├── trait UntypedExpression
  │   ├── trait UntypedUnaryOperation
  │   │   ├── case class UntypedIncrement(line: Int, col: Int, expression: UntypedExpression)
  │   │   ├── case class UntypedDecrement(line: Int, col: Int, expression: UntypedExpression)
  │   │   └── case class UntypedNot(line: Int, col: Int, expression: UntypedExpression)
  │   ├── trait UntypedBinaryOperation
  │   │   ├── case class UntypedArithmeticOperation(line: Int, col: Int, operator: ArithmeticOperator, lhs: UntypedExpression, rhs: UntypedExpression)
  │   │   └── case class UntypedLogicalOperation(line: Int, col: Int, operator: LogicalOperator, lhs: UntypedExpression, rhs: UntypedExpression)
  │   ├── case class UntypedTernaryOperation(line: Int, col: Int, condition: UntypedLogicalOperation, ifTrue: UntypedExpression, ifFalse: UntypedExpression)
  │   ├── trait UntypedCall
  │   │   ├── case class UntypedCallout  // TODO not sure yet
  │   │   └── case class UntypedMethodCall(line: Int, col: Int, method: UntypedMethodDeclaration, params: Vector[UntypedExpression])  // look into mutable field for method
  │   ├── case class Length(line: Int, col: Int, array: ArrayDeclaration)
  │   ├── trait Literal
  │   │   ├── case class IntLiteral(line: Int, col: Int, value: Int)
  │   │   ├── case class BooleanLiteral(line: Int, col: Int, value: Boolean)
  │   │   ├── case class CharLiteral(line: Int, col: Int, char: Char)
  │   │   └── case class StringLiteral(line: Int, col: Int, string: String)
  │   └── case class UntypedLocation(line: Int, col: Int, field: FieldDeclaration, index: UntypedExpression)  // 0 if regular
  ├── trait UntypedMemberDeclaration
  │   ├── trait FieldDeclaration
  │   │   ├── case class VariableDeclaration(line: Int, col: Int, name: String, type: Type)
  │   │   └── case class ArrayDeclaration(line: Int, col: Int, name: String, type: Type, length: IntLiteral)
  │   └── case class UntypedMethodDeclaration(line: Int, col: Int, name: String, type: Type, params: Vector[FieldDeclaration], block: UntypedBlock)
  ├── case class UntypedBlock(line: Int, col: Int, declarations: Vector[FieldDeclaration], statements: Vector[UntypedStatement])
  ├── trait UntypedStatement
  │   ├── trait UntypedAssignment
  │   │   ├── case class UntypedAssignStatement(line: Int, col: Int, location: UntypedLocation, value: UntypedExpression)
  │   │   └── case class UntypedCompoundAssignStatement(line: Int, col: Int, location: UntypedLocation, value: UntypedExpression, operator: ArithmeticOperator)
  │   ├── case class Break(line: Int, col: Int, loop: Loop)
  │   ├── trait UntypedCall
  │   │   ├── case class UntypedCallout  // TODO not sure yet
  │   │   └── case class UntypedMethodCall(line: Int, col: Int, method: UntypedMethodDeclaration, params: Vector[UntypedExpression])  // look into mutable field for method
  │   ├── case class Continue(line: Int, col: Int, loop: Loop)
  │   ├── case class UntypedReturn(line: Int, col: Int, value: UntypedExpression)
  │   ├── case class UntypedIf(line: Int, col: Int, condition: UntypedLogicalOperation, jumpTo: UntypedStatement)
  │   └── trait UntypedLoop
  │       ├── case class UntypedFor(line: Int, col: Int, start: AssignStatement, condition: UntypedLogicalOperation, update: UntypedAssignment, jumpFalse: UntypedStatement)
  │       └── case class UntypedWhile(line: Int, col: Int, condition: UntypedLogicalOperation, jumpFalse: UntypedStatement)
  ├── trait Type
  │   ├── case object IntArray
  │   ├── case object Boolean
  │   ├── case object BooleanArray
  │   ├── case object Char
  │   ├── case object Int
  │   └── case object String
  ├── trait ArithmeticOperator
  │   ├── case object Add
  │   ├── case object Subtract
  │   ├── case object Multiply
  │   ├── case object Divide
  │   └── case object Modulo
  └── trait LogicalOperator
      ├── case object Or
      ├── case object And
      ├── case object Equal
      ├── case object NotEqual
      ├── case object GreaterThan
      ├── case object GreaterThanOrEqual
      ├── case object LessThan
      └── case object LessThanOrEqual
  ```
