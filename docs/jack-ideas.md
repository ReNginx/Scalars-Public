# Sketch of IR

```scala
trait Program
├── trait Expression
|   ├── trait UnaryOperation
│   │   ├── case class Increment(expression: Expression)
│   │   ├── case class Decrement(expression: Expression)
|   |   └── case class Not(expression: Expression)
│   ├── trait BinaryOperation
│   │   ├── case class ArithmeticOperation(operator: ArithmeticOperator, lhs: Expression, rhs: Expression)
│   │   └── case class LogicalOperation(operator: LogicalOperator, lhs: Expression, rhs: Expression)
│   ├── case class TernaryOperation(condition: LogicalOperation, ifTrue: Expression, ifFalse: Expression)
│   ├── trait Call
│   │   ├── case class Callout  // TODO not sure yet
│   │   └── case class MethodCall(method: MethodDeclaration, params: Vector[Expression])
│   ├── case class Length(array: ArrayDeclaration)
│   ├── trait Literal
│   │   ├── case class IntLiteral(value: Int)
│   │   ├── case class BooleanLiteral(value: Boolean)
│   │   ├── case class CharLiteral(char: Char)
│   │   └── case class StringLiteral(string: String)
│   └── case class Location(field: FieldDeclaration, index: Expression)
├── trait MemberDeclaration
│   ├── trait FieldDeclaration
│   │   ├── case class VariableDeclaration(type: Type, name: String)
│   │   └── case class ArrayDeclaration(type: Type, name: String, length: IntLiteral)
│   └── case class MethodDeclaration(type: Type, name: String, params: Vector[FieldDeclaration], code: Program)
├── trait Statement
│   ├── trait Assignment
│   |   ├── case class AssignStatement(location: Location, value: Expression)
│   │   └── case class CompoundAssignStatement(location: Location, value: Expression, operator: ArithmeticOperator)
│   ├── case class Break(loop: Loop)
│   ├── case class Continue(loop: Loop)
│   ├── case class Return(value: Expression)
│   ├── case class If(condition: LogicalOperation, jumpTo: Statement)
│   └── case class Import(id: String)
├── trait Loop
│   ├── case class For(start: AssignStatement, condition: LogicalOperation, update: Assignment, jumpFalse: Statement)
│   └── case class While(condition: LogicalOperation, jumpFalse: Statement)
├── trait Type
│   ├── case object IntArray
│   ├── case object Boolean
│   ├── case object BooleanArray
│   ├── case object Char
│   ├── case object Int
│   └── case object String
├── trait ArithmeticOperator
|   ├── case object Add
|   ├── case object Subtract
|   ├── case object Multiply
|   ├── case object Divide
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
