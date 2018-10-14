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
  var noError = true

  def printErrMsg(str: String): Unit = {
    println(str)
    noError = false;
  }

  def apply(Ir: IR,
            mthd: Option[MethodsDeclaration] = None,
            loop: Option[Loop] = None,
            params: Option[Vector[FieldDeclaration]] = None): Unit = Ir match {

    case Program(_, _, imports, fields, methods) => {
      imports.foreach(TypeChecking(_))
      fields.foreach(TypeChecking(_))
      methods.foreach(TypeChecking(_))
    }

    case extMthd: ExtMethodDeclaration => {
      noError &= SymbolTable.add(extMthd)
    }

    case variable: VariableDeclaration => {
      noError &= SymbolTable.add(variable)
    }

    case array: ArrayDeclaration => {
      if (array.length.value <= 0)
        printErrMsg(s"line: ${array.length.line}, col: ${array.length.col}, bad array size")
      noError &= SymbolTable.add(array)
    }

    case locMthd: LocMethodDeclaration => {
      noError &= SymbolTable.add(locMthd)
      TypeChecking(locMthd.block, Option(locMthd), params = Option(locMthd.params))
    }

    case Block(_, _, declarations, statements) => {
      SymbolTable.push
      if (params.isDefined) params.get.foreach(TypeChecking(_))
      declarations.foreach(TypeChecking(_))
      statements.foreach(TypeChecking(_, mthd, loop))
      SymbolTable.pop
    }

    // Statement part

    // "typ" here should be the expected return type of the function
    case ret: Return => {
      TypeChecking(ret.value)
      assert(mthd.isDefined)
      if (mthd.get.typ != ret.value.typ) {
        printErrMsg(s"line: ${ret.line}, col: ${ret.col}, return type mismatch, ${mthd.get.typ} expected, ${ret.value.typ} given")
      }
    }

    case If(line, col, condition, ifTrue, ifFalse) => {
      TypeChecking(condition)
      if (condition.typ != Option(BoolType)) {
        printErrMsg(s"line: ${line}, col: ${col}, if statement has a invalid condition, expect ${Option(BoolType)} found ${condition.typ}")

      }
      TypeChecking(ifTrue, mthd, loop)
      if (ifFalse.isDefined)
        TypeChecking(ifFalse.get, mthd, loop)
    }

    // there is no type checking for Continue and Break

    //Assignment
    case AssignStatement(line, col, location, expression) => {
      TypeChecking(location)
      TypeChecking(expression)
      val typeCheck = location.typ == expression.typ
      if (!typeCheck) {
        printErrMsg(s"line: $line, col: $col, cannot assign a(n) ${expression.typ} to ${location.typ}")

      }
    }

    case CompoundAssignStatement(line, col, location, expression, _) => {
      TypeChecking(location)
      TypeChecking(expression)
      val typeCheck = location.typ == expression.typ && location.typ == Option(IntType)
      if (!typeCheck) {
        printErrMsg(s"line: $line, col: $col, both sides of compound assignment must be ${Option(IntType)}")
      }
    }

    //Call

    case call: MethodCall => {
      call.params.foreach(TypeChecking(_))
      val decl = SymbolTable.get(call.name)

      if (decl.isEmpty) {
        printErrMsg(s"line: ${call.line}, col: ${call.col}, function ${call.name} is not defined")

      } else {
        val method = decl.get
        method match {
          case x: FieldDeclaration => {
            printErrMsg(s"line: ${call.line}, col: ${call.col}, ${x.name} is not a function")
          }
          case x: MethodsDeclaration => {
            call.method = Option(x)
            x match {
              case y: LocMethodDeclaration =>
                if (call.params.length != y.params.length) {
                  printErrMsg(s"line: ${call.line}, col: ${call.col}, incorrect number of arguments")
                }
                else
                  for ((a, b) <- call.params zip y.params) {
                    if (a.typ != b.typ) {
                      printErrMsg(s"line: ${call.line}, param type ${b.typ} expected, ${a.typ} given")

                    }
                  }
              case _ =>
            }
          }
        }
      }
    }

    //Loop
    case forLoop: For => {
      TypeChecking(forLoop.start)
      TypeChecking(forLoop.condition)
      if (forLoop.condition.typ != Option(BoolType)) {
        printErrMsg(s"line: ${forLoop.line}, col: ${forLoop.col}, Loop condition is not bool")
      }
      TypeChecking(forLoop.update)
      TypeChecking(forLoop.ifTrue, mthd, Option(forLoop))
    }

    case whileLoop: While => {
      TypeChecking(whileLoop.condition)
      if (whileLoop.condition.typ != Option(BoolType)) {
        printErrMsg(s"line: ${whileLoop.line}, col: ${whileLoop.col}, Loop condition is not bool")
      }
      TypeChecking(whileLoop.ifTrue, mthd, Option(whileLoop))
    }

    case con: Continue => {
      if (loop.isEmpty) {
        printErrMsg(s"line: ${con.line}, col: ${con.col}, continue is not inside a loop body")
      }
      con.loop = loop
    }

    case brk: Break => {
      if (loop.isEmpty) {
        printErrMsg(s"line: ${brk.line}, col: ${brk.col}, break is not inside a loop body")
      }
      brk.loop = loop
    }

    //Operation
    case Increment(line, col, location) => {
      TypeChecking(location)

      if (location.typ != Option(IntType)) {
        printErrMsg(s"line: $line, col: $col, doing increment on type: ${location.typ.getOrElse(None)}")
      }
    }

    case Decrement(line, col, location) => {
      TypeChecking(location)

      if (location.typ != Option(IntType)) {
        printErrMsg(s"line: $line, col: $col, doing decrement on type: ${location.typ.getOrElse(None)}")
      }
    }

    case Not(line, col, expression) => {
      TypeChecking(expression)

      if (expression.typ != Option(BoolType)) {
        printErrMsg(s"line: $line, col: $col, cannot apply NOT to ${expression.typ}")
      }
    }

    case Negate(line, col, expression) => {
      TypeChecking(expression)
      if (expression.typ != Option(IntType)) {
        printErrMsg(s"line: $line, col: $col, cannot apply Negation to ${expression.typ}")
      }
    }

    case ArithmeticOperation(line, col, operator, lhs, rhs) => {
      TypeChecking(lhs)
      TypeChecking(rhs)
      if (lhs.typ != Option(IntType) || rhs.typ != Option(IntType)) {
        printErrMsg(s"line: $line, col: $col, $operator requires $IntType on both sides")
      }
    }

    case LogicalOperation(line, col, operator, lhs, rhs) => {
      TypeChecking(lhs)
      TypeChecking(rhs)
      val typeAgree = lhs.typ == rhs.typ
      if (!typeAgree) {
        printErrMsg(s"line: $line, col: $col, types of left-side and right-side expression do not agree")
      } else if (lhs.typ == Option(IntType)) {
        operator match {
          case And | Or => {
            printErrMsg(s"line: $line, col: $col, cannot apply operator $operator to ${lhs.typ}")
          }
          case _ =>
        }
      }
      else if (lhs.typ == Option(BoolType)) {
        operator match {
          case LessThan | LessThanOrEqual | GreaterThan | GreaterThanOrEqual => {
            printErrMsg(s"line: $line, col: $col, cannot apply operator $operator to ${lhs.typ}")
          }
          case _ =>
        }
      }
      else {
        printErrMsg(s"line: $line, col: $col, unexpected type ${lhs.typ} for operator $operator")
      }
    }


    case TernaryOperation(line, col, condition, ifTrue, ifFalse) => {
      TypeChecking(condition)
      TypeChecking(ifTrue)
      TypeChecking(ifFalse)

      val typeAgree = ifTrue.typ == ifFalse.typ
      val validCondition = condition.typ == Option(BoolType)

      if (!validCondition) {
        printErrMsg(s"line: $line, col: $col, condition is not a valid $BoolType expr")
      }
      if (!typeAgree) {
        printErrMsg(s"line: $line, col: $col, branch types of ternary operator do not agree")
      }
    }

    // Expression
    case loc: Location => { //location only holds variables.
      if (loc.index.isDefined) {
        TypeChecking(loc.index.get)
        if (loc.index.get.typ != Option(IntType)) {
          printErrMsg(s"line: ${loc.line}, col: ${loc.col}, ${loc.index.get.typ} cannot be index")
        }
      }
      val decl = SymbolTable.get(loc.name)
      if (decl.isEmpty) {
        printErrMsg(s"line: ${loc.line}, col: ${loc.col}, variable ${loc.name} not defined")
      }
      else {
        decl.get match {
          case x: VariableDeclaration => {
            if (loc.index.isDefined)
              printErrMsg(s"line: ${loc.line}, col: ${loc.col}, variable ${loc.name} is not an array")
            loc.field = Option(x)
          }

          case x: ArrayDeclaration => {
            if (loc.index.isEmpty)
              printErrMsg(s"line: ${loc.line}, col: ${loc.col}, variable ${loc.name} is an array")
            loc.field = Option(x)
          }
          case _ => printErrMsg(s"line: ${loc.line}, col: ${loc.col}, variable ${loc.name} is not a variable name")
        }
      }
    }

    case Length(line, col, loc) => {
      val decl = SymbolTable.get(loc.name)
      if (decl.isEmpty) {
        printErrMsg(s"line: ${loc.line}, col: ${loc.col}, variable ${loc.name} not defined")
      }
      else {
        decl.get match {
          case x: ArrayDeclaration => {
            if (loc.index.isDefined) {
              printErrMsg(s"line: ${loc.line}, col: ${loc.col}, len operator takes an array")
            }
            else
              loc.field = Option(x)
          }
          case _ => printErrMsg(s"line: ${loc.line}, col: ${loc.col}, variable ${loc.name} is not an array")
        }
      }
    }

    case _ =>
  }

}
