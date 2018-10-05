package ir

import ir.typed._

/**
  * TypeChecking based on the following rules.
  * - Before running type checking, make sure that there's no error in function name, and variable binding.
  * - Literal are always valid.
  * - Type of an operation depends on operator (Int for Arithmetic operators, Bool for Logical operators)
  * - When an error occurs in operation, it will be reported but does not influence the type of operation.
  */
object TypeChecking {

  /**
    * Helper function for sequential Check
    *
    * @param lhs
    * @param rhs
    * @return
    */
  def seqAnd(lhs: Boolean, rhs: IR): Boolean = lhs && TypeChecking(rhs)

  /**
    * Helper function for sequential check
    *
    * @param lhs
    * @param rhs
    * @return
    */
  def combAnd(lhs: Boolean, rhs: Boolean): Boolean = lhs && rhs

  /**
    * checks a sequence of expression.
    *
    * @param v
    * @tparam T
    * @return if any element of v is invalid, return false otherwise true
    */
  def seqCheck[T <: IR](v: Iterable[T]): Boolean = v.aggregate(true)(seqAnd, combAnd)

  def apply(typedIr: IR): Boolean = typedIr match {
    // assume that imports and fields decl have no problem
    // Program part
    case Program(_, _, _, _, methods) => seqCheck(methods)

    case MethodDeclaration(_, _, _, _, _, block) => TypeChecking(block)

    case Block(_, _, _, statements) => seqCheck(statements)

    // Statement part

    // "typ" here should be the expected return type of the function
    case Return(line, col, typ, expression) =>
      val exprCheck = expression.typ == typ
      if (!exprCheck) {
        println(s"line: $line, col: $col, return type mismatch, $typ expected, ${expression.typ} given")
      }
      exprCheck

    case If(_, _, condition, ifTrue, ifFalse) => seqCheck(List(condition, ifTrue, ifFalse.getOrElse(VoidType)))

    // there is no type checking for Continue and Break

    //Assignment
    case AssignStatement(line, col, location, expression) =>
      val exprCheck = seqCheck(List(location, expression))
      val typeCheck = location.typ == expression.typ

      if (!exprCheck) {
        println(s"line: $line, col: $col, cannot assign a(n) ${expression.typ} to ${location.typ}")
      }

      exprCheck && typeCheck

    case CompoundAssignStatement(line, col, location, expression, _) =>
      val res = TypeChecking(location) && location.typ == expression.typ
      if (!res) {
        println(s"line: $line, col: $col, doing compound assignment on type: ${location.typ}")
      }
      res

    //Call
    case Callout(_, _, _, params) => seqCheck(params)

    case MethodCall(line, _, _, method, params) =>
      var callCheck = seqCheck(params)
      val paramExpr = params
      val paramDecl = method.params

      for ((a, b) <- paramExpr zip paramDecl) {
        if (a.typ != b.typ) {
          println(s"line: $line, param type ${b.typ} expected, ${a.typ} given")
          callCheck = false
        }
      }
      callCheck

    //Loop
    case For(_, _, start, condition, update, ifTrue) => seqCheck(List(start, condition, update, ifTrue))

    case While(_, _, condition, ifTrue) => seqCheck(List(condition, ifTrue))


    //Operation
    case Increment(line, col, _, location) =>
      val locationCheck = TypeChecking(location)
      val typeCheck = location.typ == IntType

      if (!typeCheck) {
        println(s"line: $line, col: $col, doing increment on type: ${location.typ}")
      }

      locationCheck && typeCheck

    case Decrement(line, col, _, location) =>
      val locationCheck = TypeChecking(location)
      val typeCheck = location.typ == IntType

      if (!typeCheck) {
        println(s"line: $line, col: $col, doing decrement on type: ${location.typ}")
      }

      locationCheck && typeCheck

    case Not(line, col, _, expression) =>
      val exprCheck = TypeChecking(expression)
      val typeCheck = expression.typ == BoolType

      if (!typeCheck) {
        println(s"line: $line, col: $col, cannot apply NOT to ${expression.typ}")
      }

      exprCheck && typeCheck

    case Negate(line, col, _, expression) =>
      val exprCheck = TypeChecking(expression)
      val typeCheck = expression.typ == IntType
      if (!typeCheck) {
        println(s"line: $line, col: $col, cannot apply Negation to ${expression.typ}")
      }
      exprCheck

    case ArithmeticOperation(line, col, _, operator, lhs, rhs) =>
      val exprCheck = seqCheck(List(lhs, rhs))
      val typeCheck = lhs.typ == IntType && rhs.typ == IntType
      if (!typeCheck) {
        println(s"line: $line, col: $col, $operator requires $IntType on both sides")
      }
      exprCheck && typeCheck

    case LogicalOperation(line, col, _, operator, lhs, rhs) =>
      val exprCheck = seqCheck(List(lhs, rhs))
      val typeAgree = lhs.typ == rhs.typ
      val typeCheck = operator == Equal || operator == NotEqual || lhs.typ == BoolType

      if (!typeAgree) {
        println(s"line: $line, col: $col, type of left-side and right-side expression do not agree")
      }
      if (!typeCheck) {
        println(s"line: $line, col: $col, unexpected types on either side of $operator")
      }

      exprCheck && typeAgree && typeCheck

    case TernaryOperation(line, col, _, condition, ifTrue, ifFalse) =>
      val exprCheck = seqCheck(List(condition, ifTrue, ifFalse))
      val typeAgree = ifTrue.typ == ifFalse.typ
      val validCondition = condition.typ == BoolType

      if (!validCondition) {
        println(s"line: $line, col: $col, condition is not a valid $BoolType expr")
      }
      if (!typeAgree) {
        println(s"line: $line, col: $col, branch types of ternary operator do not agree")
      }

      exprCheck && typeAgree && validCondition

    // Expression
    case Location(line, col, _, _, expression) =>
      val exprCheck = TypeChecking(expression)
      val typeCheck = expression.typ == IntType

      if (!typeCheck) {
        println(s"line: $line, col: $col, array index type mismatch, $IntType expected, ${expression.typ} given")
      }

      exprCheck && typeCheck

    case Length(line, col, _, location) =>
      val ArrayLocCheck = location.field.isInstanceOf[ArrayDeclaration]
      if (!ArrayLocCheck) {
        println(s"line: $line, col: $col, $location is not a valid array name")
      }
      ArrayLocCheck

    case _ => true
  }
}
