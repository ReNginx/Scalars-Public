trait IR
  case class Program(line: Int, col: Int, imports: Vector[Import], fields: Vector[FieldDeclaration], methods: Vector[MethodDeclaration])
    case class Import(line: Int, col: Int, method: MethodDeclaration) extends IR
    trait Expression extends IR
      trait UnaryOperation extends Expression
        case class Increment(line: Int, col: Int, expression: Expression) extends UnaryOperation
        case class Decrement(line: Int, col: Int, expression: Expression) extends UnaryOperation
        case class Not(line: Int, col: Int, expression: Expression) extends UnaryOperation
      trait BinaryOperation extends Expression
        case class ArithmeticOperation(line: Int, col: Int, operator: ArithmeticOperator, lhs: Expression, rhs: Expression) extends BinaryOperation
        case class LogicalOperation(line: Int, col: Int, operator: LogicalOperator, lhs: Expression, rhs: Expression) extends BinaryOperation
      case class TernaryOperation(line: Int, col: Int, condition: LogicalOperation, ifTrue: Expression, ifFalse: Expression) extends Expression
      trait Call extends Expression with Statement
        case class Callout() extends Call
        case class MethodCall(line: Int, col: Int, method: MethodDeclaration, params: Vector[Expression]) extends Call
      case class Length(line: Int, col: Int, array: ArrayDeclaration) extends UntypedExpression with Expression
      trait Literal extends UntypedExpression with Expression
        case class IntLiteral(line: Int, col: Int, value: Int) extends Literal
        case class BooleanLiteral(line: Int, col: Int, value: Boolean) extends Literal
        case class CharLiteral(line: Int, col: Int, char: Char) extends Literal
        case class StringLiteral(line: Int, col: Int, string: String) extends Literal
      case class Location(line: Int, col: Int, field: FieldDeclaration, index: Expression) extends Expression
    trait MemberDeclaration extends IR
      trait FieldDeclaration extends UntypedMemberDeclaration with Expression
        case class VariableDeclaration(line: Int, col: Int, name: String, _type: Type) extends FieldDeclaration
        case class ArrayDeclaration(line: Int, col: Int, name: String, _type: Type, length: IntLiteral) extends FieldDeclaration
      case class MethodDeclaration(line: Int, col: Int, name: String, _type: Type, params: Vector[FieldDeclaration], block: Block) extends MemberDeclaration
    case class Block(line: Int, col: Int, declarations: Vector[FieldDeclaration], statements: Vector[Statement]) extends IR
    trait Statement extends IR
      trait Assignment extends Statement
        case class AssignStatement(line: Int, col: Int, location: Location, value: Expression) extends Assignment
        case class CompoundAssignStatement(line: Int, col: Int, location: Location, value: Expression, operator: ArithmeticOperator) extends Assignment
      case class Break(line: Int, col: Int, loop: Loop) extends Statement
      case class Continue(line: Int, col: Int, loop: Loop) extends Statement
      case class Return(line: Int, col: Int, value: Expression) extends Statement
      case class If(line: Int, col: Int, condition: LogicalOperation, jumpTo: Statement) extends Statement
      trait Loop extends Statement
        case class For(line: Int, col: Int, start: AssignStatement, condition: LogicalOperation, update: Assignment, jumpFalse: Statement) extends Loop
        case class While(line: Int, col: Int, condition: LogicalOperation, jumpFalse: Statement) extends Loop
    trait Type extends UntypedIR with IR
      case object Boolean      extends Type
      case object BooleanArray extends Type
      case object Char         extends Type
      case object Int          extends Type
      case object IntArray     extends Type
      case object String       extends Type
    trait ArithmeticOperator extends UntypedIR with IR
      case object Add      extends ArithmeticOperator
      case object Divide   extends ArithmeticOperator
      case object Modulo   extends ArithmeticOperator
      case object Multiply extends ArithmeticOperator
      case object Subtract extends ArithmeticOperator
    trait LogicalOperator extends UntypedIR with IR
      case object And                extends LogicalOperator
      case object Or                 extends LogicalOperator
      case object Equal              extends LogicalOperator
      case object NotEqual           extends LogicalOperator
      case object GreaterThan        extends LogicalOperator
      case object GreaterThanOrEqual extends LogicalOperator
      case object LessThan           extends LogicalOperator
      case object LessThanOrEqual    extends LogicalOperator
