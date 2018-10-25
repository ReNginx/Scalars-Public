package ir

import scala.Console

import ir.components._

/** Type-check an IR.
 *
 * Before using this object, make sure that there is no error in function
 * names and variable bindings.
 *
 * Type-checks using the following rules:
 *   - Literals are always valid
 *   - Type of an operation depends on the operator
 *       i.e. arithmetic operators -> Int, logical operators -> Bool
 *   - When an error occurs in an operation, it is reported but does not
 *     influence the type of operation
 */
object TypeCheck {

  // true if an error has occurred
  var error = false

  /** Print the string to stderr, and memo that an error has occurred.
   *
   * @param string message to print to stderr
   */
  def stderr(string: String): Unit = {
    Console.err.println(string)
    error = true;
  }

  /** Type-check the given IR.
   *
   * Type-checks the given IR and modifies it with necessary information.
   * If errors are encountered, error messages are printed to stderr.
   *
   * @param ir the IR to type-check
   * @param method
   * @param loop
   * @param params
   * @return the original ir, modified
   */
  def apply(
      ir: IR,
      method: Option[MethodDeclaration]=None,
      loop:   Option[Loop]=None,
      params: Option[Vector[FieldDeclaration]]=None): Unit = ir match {

    case Program(_, _, imports, fields, methods) => {
      imports foreach { TypeCheck(_) }
      fields  foreach { TypeCheck(_) }
      methods foreach { TypeCheck(_) }
    }

    case externalMethod: ExtMethodDeclaration => {
      error |= SymbolTable.add(externalMethod)
    }

    case variable: VariableDeclaration => {
      error |= SymbolTable.add(variable)
    }

    case array: ArrayDeclaration => {
      val len = array.length
      if (len.value <= 0) {
        stderr(s"line: ${len.line}, col: ${len.col}, array length ${len.value} <= 0")
      }

      error |= SymbolTable.add(array)
    }

    case localMethod: LocMethodDeclaration => {
      error |= SymbolTable.add(localMethod)
      TypeCheck(
        localMethod.block,
        Option(localMethod),
        params = Option(localMethod.params)
      )
    }

    case Block(_, _, declarations, statements) => {
      SymbolTable.push()
      if (params.isDefined) {
        params.get foreach { TypeCheck(_) }
      }

      declarations foreach { TypeCheck(_) }
      statements foreach { TypeCheck(_, method, loop) }
      SymbolTable.pop()
    }

    // Statement part

    // "typ" here should be the expected return type of the function
    case ret: Return => {
      assert(method.isDefined)
      if (method.get.typ == Option(VoidType)) {
        if (ret.value != None) {
          stderr(s"line: ${ret.line}, col: ${ret.col}, return type mismatch, ${method.get.typ} expected, ${ret.value.get.typ} given")
        }
      }
      else {
        TypeCheck(ret.value.get)
        if (method.get.typ != ret.value.get.typ) {
          stderr(s"line: ${ret.line}, col: ${ret.col}, return type mismatch, ${method.get.typ} expected, ${ret.value.get.typ} given")
        }
      }
    }

    case If(line, col, condition, conditionBlock, ifTrue, ifFalse) => {
      TypeCheck(condition)
      if (condition.typ != Option(BoolType)) {
        stderr(s"line: ${line}, col: ${col}, if statement has a invalid condition, expect ${Option(BoolType)} found ${condition.typ}")
      }

      TypeCheck(ifTrue, method, loop)
      if (ifFalse.isDefined) {
        TypeCheck(ifFalse.get, method, loop)
      }
    }

    case AssignStatement(line, col, location, expression, valueBlock) => {
      TypeCheck(location)
      TypeCheck(expression)
      if (location.typ != expression.typ) {
        stderr(s"line: $line, col: $col, cannot assign a(n) ${expression.typ} to ${location.typ}")
      }
    }
    case CompoundAssignStatement(line, col, location, expression, valueBlock, _) => {
      TypeCheck(location)
      TypeCheck(expression)
      if (location.typ != expression.typ || location.typ != Option(IntType)) {
        stderr(s"line: $line, col: $col, both sides of compound assignment must be ${Option(IntType)}")
      }
    }

    case call: MethodCall => {
      call.params foreach { TypeCheck(_) }

      val methodDeclaration = SymbolTable.get(call.name)
      if (methodDeclaration.isEmpty) {
        stderr(s"line: ${call.line}, col: ${call.col}, function ${call.name} is not defined")
        return
      }

      methodDeclaration.get match {
        case f: FieldDeclaration => {
          stderr(s"line: ${call.line}, col: ${call.col}, ${f.name} is not a function")
        }
        case method: MethodDeclaration => {
          call.method = Option(method)
          method match {
            case local: LocMethodDeclaration => {
              if (call.params.length != local.params.length) {
                stderr(s"line: ${call.line}, col: ${call.col}, incorrect number of arguments")
              } else {
                (call.params zip local.params) filter {
                  case (a, b) => a.typ != b.typ  // type mismatches
                } foreach {
                  case (a, b) => stderr(s"line: ${call.line}, parameter type ${b.typ} expected, ${a.typ} given")
                }
              }
            }
            case _ =>
          }
        }
      }
    }

    case forLoop: For => {
      TypeCheck(forLoop.start)
      TypeCheck(forLoop.condition)
      if (forLoop.condition.typ != Option(BoolType)) {
        stderr(s"line: ${forLoop.line}, col: ${forLoop.col}, Loop condition is not bool")
      }
      TypeCheck(forLoop.update)
      TypeCheck(forLoop.ifTrue, method, Option(forLoop))
    }

    case whileLoop: While => {
      TypeCheck(whileLoop.condition)
      if (whileLoop.condition.typ != Option(BoolType)) {
        stderr(s"line: ${whileLoop.line}, col: ${whileLoop.col}, Loop condition is not bool")
      }
      TypeCheck(whileLoop.ifTrue, method, Option(whileLoop))
    }

    case con: Continue => {
      if (loop.isEmpty) {
        stderr(s"line: ${con.line}, col: ${con.col}, continue is not inside a loop body")
      }
      con.loop = loop
    }

    case brk: Break => {
      if (loop.isEmpty) {
        stderr(s"line: ${brk.line}, col: ${brk.col}, break is not inside a loop body")
      }
      brk.loop = loop
    }

    case Increment(line, col, location) => {
      TypeCheck(location)
      if (location.typ != Option(IntType)) {
        stderr(s"line: $line, col: $col, doing increment on type: ${location.typ.getOrElse(None)}")
      }
    }

    case Decrement(line, col, location) => {
      TypeCheck(location)
      if (location.typ != Option(IntType)) {
        stderr(s"line: $line, col: $col, doing decrement on type: ${location.typ.getOrElse(None)}")
      }
    }

    case Not(line, col, expression) => {
      TypeCheck(expression)
      if (expression.typ != Option(BoolType)) {
        stderr(s"line: $line, col: $col, cannot apply NOT to ${expression.typ}")
      }
    }

    case Negate(line, col, expression) => {
      TypeCheck(expression)
      if (expression.typ != Option(IntType)) {
        stderr(s"line: $line, col: $col, cannot apply Negation to ${expression.typ}")
      }
    }

    case ArithmeticOperation(line, col, operator, lhs, rhs) => {
      TypeCheck(lhs)
      TypeCheck(rhs)
      if (lhs.typ != Option(IntType) || rhs.typ != Option(IntType)) {
        stderr(s"line: $line, col: $col, $operator requires $IntType on both sides")
      }
    }

    case LogicalOperation(line, col, operator, lhs, rhs) => {
      TypeCheck(lhs)
      TypeCheck(rhs)
      if (lhs.typ != rhs.typ) {
        stderr(s"line: $line, col: $col, types of left-side and right-side expression do not agree")
      } else if (lhs.typ == Option(IntType)) {
        // can't use logical operators to ints
        operator match {
          case And | Or => {
            stderr(s"line: $line, col: $col, cannot apply operator $operator to ${lhs.typ}")
          }
          case _ =>
        }
      } else if (lhs.typ == Option(BoolType)) {
        // can't use arithmetic operators to bools
        operator match {
          case LessThan | LessThanOrEqual | GreaterThan | GreaterThanOrEqual => {
            stderr(s"line: $line, col: $col, cannot apply operator $operator to ${lhs.typ}")
          }
          case _ =>
        }
      } else {  // neither a int or bool
        stderr(s"line: $line, col: $col, unexpected type ${lhs.typ} for operator $operator")
      }
    }

    case TernaryOperation(line, col, condition, ifTrue, ifFalse) => {
      TypeCheck(condition)
      TypeCheck(ifTrue)
      TypeCheck(ifFalse)

      if (condition.typ != Option(BoolType)) {
        stderr(s"line: $line, col: $col, condition is not a valid $BoolType expr")
      }
      if (ifTrue.typ != ifFalse.typ) {
        stderr(s"line: $line, col: $col, branch types of ternary operator do not agree")
      }
    }

    case loc: Location => { //location only holds variables.
      if (loc.index.isDefined) {
        TypeCheck(loc.index.get)
        if (loc.index.get.typ != Option(IntType)) {
          stderr(s"line: ${loc.line}, col: ${loc.col}, ${loc.index.get.typ} cannot be index")
        }
      }

      val decl = SymbolTable.get(loc.name)
      if (decl.isEmpty) {
        stderr(s"line: ${loc.line}, col: ${loc.col}, variable ${loc.name} not defined")
        return
      }

      decl.get match {
        case varDecl: VariableDeclaration => {
          if (loc.index.isDefined) {
            stderr(s"line: ${loc.line}, col: ${loc.col}, variable ${loc.name} is not an array")
          }
          loc.field = Option(varDecl)
        }

        case arrayDecl: ArrayDeclaration => {
          if (loc.index.isEmpty) {
            stderr(s"line: ${loc.line}, col: ${loc.col}, variable ${loc.name} is an array")
          }
          loc.field = Option(arrayDecl)
        }

        case _ => stderr(s"line: ${loc.line}, col: ${loc.col}, variable ${loc.name} is not a variable name")
      }
    }

    case Length(line, col, loc) => {
      val decl = SymbolTable.get(loc.name)
      if (decl.isEmpty) {
        stderr(s"line: ${loc.line}, col: ${loc.col}, variable ${loc.name} not defined")
        return
      }
      decl.get match {
        case arrayDecl: ArrayDeclaration => {
          if (loc.index.isDefined) {
            stderr(s"line: ${loc.line}, col: ${loc.col}, len operator takes an array")
          } else {
            loc.field = Option(arrayDecl)
          }
        }
        case _ => stderr(s"line: ${loc.line}, col: ${loc.col}, variable ${loc.name} is not an array")
      }
    }

    case _ =>
  }

}
