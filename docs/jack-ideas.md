# Sketch of IR

```scala
trait Program
├── trait Expression
│   ├── trait BinaryOperation
│   │   ├── case class ArithmeticOperation(operator: ArithmeticOperator, lhs: Expression, rhs: Expression)
│   │   └── case class LogicalOperation(operator: LogicalOperator, lhs: Expression, rhs: Expression)
|   ├── trait UnaryOperation
│   │   ├── case class Decrement(location: Location)
|   |   └── case class Increment(location: Location)
│   ├── case class MethodCall(method: MethodDeclaration, params: Vector[Expression])
│   ├── case class Length
│   ├── trait Literal
│   │   ├── case class BooleanLiteral(value: Boolean)
│   │   └── case class IntLiteral(value: Int)
│   └── case class Location(id: FieldDeclaration, index: Expression)
├── trait MemberDeclaration
│   ├── trait FieldDeclaration
│   │   ├── case class VariableDeclaration(type: Type, name: String)
│   │   └── case class ArrayDeclaration(type: Type, name: String, length: IntLiteral)
│   └── case class MethodDeclaration(type: Type, name: String, params: Vector[FieldDeclaration])
├── trait Statement
│   ├── case class AssignStatement(location: Location, value: Expression)
│   ├── case class Break(loop: Loop)
│   ├── case class CompoundAssignStatement(location: Location, value: Expression, operator: ArithmeticOperator)
│   ├── case class Continue(loop: Loop)
│   ├── case class If(condition: BinaryOperation, jumpTo: Statement)  // specify to be logical operation
│   └── case class Import
├── trait Loop 
│   ├── case class For(intial: AssignStatement, condition: LogicalOperation, update: UnaryOperation, jumpFalse: Statement)
│   └── case class While(condition: LogicalOperation, jumpFalse: Statement)
├── trait Type
│   ├── case object Boolean
│   ├── case object BooleanArray
│   ├── case object Int
│   └── case object IntArray
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
    ├── case object Not
    ├── case object GreaterThan
    ├── case object GreaterThanOrEqual
    ├── case object LessThan
    └── case object LessThanOrEqual
```
