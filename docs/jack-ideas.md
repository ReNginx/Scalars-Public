# Sketch of IR

```scala
trait Program
├── trait Expression
│   ├── trait BinaryOperation(operator: Operator, lhs: Expression, rhs: Expression)
│   ├── trait Call
│   │   ├── case class Callout
│   │   └── case class MethodCall(method: MethodDeclaration, params: Vector[Expression])
│   ├── case class Length
│   ├── trait Literal
│   │   ├── case class BooleanLiteral(value: Boolean)
│   │   └── case class IntLiteral(value: Int)
│   └── case class Location(id: FieldDeclaration, index: Expression)
├── trait MemberDeclaration
│   ├── trait FieldDeclaration(id: String)
│   │   ├── case class VariableDeclaration(type: Type)
│   │   └── case class ArrayDeclaration(type: Type, length: IntLiteral)
│   └── case class MethodDeclaration
├── trait Statement
│   ├── case class AssignStatement(location: Location, value: Expression)
│   ├── case class Break
│   ├── case class CompoundAssignStatement
│   ├── case class Continue
│   ├── case class For
│   ├── case class If
│   ├── case class Import
│   └── case class While
├── trait Type
│   ├── case object Boolean
│   ├── case object BooleanArray
│   ├── case object Int
│   └── case object IntArray
└── trait Operator
    ├── case object Add
    ├── case object Subtract
    ├── case object Multiply
    ├── case object Divide
    ├── case object Modulo
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
