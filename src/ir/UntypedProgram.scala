trait UntypedIR
  case class UntypedProgram(line: Int, col: Int, imports: Vector[UntypedImport], fields: Vector[FieldDeclaration], methods: Vector[UntypedMethodDeclaration])
    case class UntypedImport(line: Int, col: Int, method: UntypedMethodDeclaration) extends UntypedIR
    trait UntypedExpression extends UntypedIR
      trait UntypedUnaryOperation extends UntypedExpression
        case class UntypedIncrement(line: Int, col: Int, expression: UntypedExpression) extends UntypedUnaryOperation
        case class UntypedDecrement(line: Int, col: Int, expression: UntypedExpression) extends UntypedUnaryOperation
        case class UntypedNot(line: Int, col: Int, expression: UntypedExpression) extends UntypedUnaryOperation
      trait UntypedBinaryOperation extends UntypedExpression
        case class UntypedArithmeticOperation(line: Int, col: Int, operator: ArithmeticOperator, lhs: UntypedExpression, rhs: UntypedExpression) extends UntypedBinaryOperation
        case class UntypedLogicalOperation(line: Int, col: Int, operator: LogicalOperator, lhs: UntypedExpression, rhs: UntypedExpression) extends UntypedBinaryOperation
      case class UntypedTernaryOperation(line: Int, col: Int, condition: UntypedLogicalOperation, ifTrue: UntypedExpression, ifFalse: UntypedExpression) extends UntypedExpression
      trait UntypedCall extends UntypedExpression with UntypedStatement
        case class UntypedCallout() extends UntypedCall
        case class UntypedMethodCall(line: Int, col: Int, method: UntypedMethodDeclaration, params: Vector[UntypedExpression]) extends UntypedCall
      case class UntypedLocation(line: Int, col: Int, field: FieldDeclaration, index: UntypedExpression) extends UntypedExpression
    trait UntypedMemberDeclaration extends UntypedIR
      case class UntypedMethodDeclaration(line: Int, col: Int, name: String, _type: Type, params: Vector[FieldDeclaration], block: UntypedBlock) extends UntypedMemberDeclaration
    case class UntypedBlock(line: Int, col: Int, declarations: Vector[FieldDeclaration], statements: Vector[UntypedStatement]) extends UntypedIR
    trait UntypedStatement extends UntypedIR
      trait UntypedAssignment extends UntypedStatement
        case class UntypedAssignStatement(line: Int, col: Int, location: UntypedLocation, value: UntypedExpression) extends UntypedAssignment
        case class UntypedCompoundAssignStatement(line: Int, col: Int, location: UntypedLocation, value: UntypedExpression, operator: ArithmeticOperator) extends UntypedAssignment
      case class UntypedBreak(line: Int, col: Int, loop: UntypedLoop) extends UntypedStatement
      case class UntypedContinue(line: Int, col: Int, loop: UntypedLoop) extends UntypedStatement
      case class UntypedReturn(line: Int, col: Int, value: UntypedExpression) extends UntypedStatement
      case class UntypedIf(line: Int, col: Int, condition: UntypedLogicalOperation, jumpTo: UntypedStatement) extends UntypedStatement
      trait UntypedLoop extends UntypedStatement
        case class UntypedFor(line: Int, col: Int, start: UntypedAssignStatement, condition: UntypedLogicalOperation, update: UntypedAssignment, jumpFalse: UntypedStatement) extends UntypedLoop
        case class UntypedWhile(line: Int, col: Int, condition: UntypedLogicalOperation, jumpFalse: UntypedStatement) extends UntypedLoop
