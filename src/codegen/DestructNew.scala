package codegen

import ir.components._

import scala.collection.mutable.{ArrayBuffer}

object DestructNew {
  var counter = 0

  /**
    * Link two CFG together.
    *
    * @param from
    * @param to
    * @return
    */
  private def link(from: CFG, to: CFG) = {
    from.next = Some(to)
    to.parents.add(from)
  }

  /**
    * create start node and end node.
    *
    * @param str
    * @return
    */
  private def create(str: String): (CFG, CFG) = {
    val st = VirtualCFG(str + "_st")
    val ed = VirtualCFG(str + "_ed")
    (st, ed)
  }

  /**
    * this function deal with expressions whose topmost operator is bool operator
    * for && it should create a structure like the following example.
    *
    * * && example
    * * block1, block2, cond1, cond2
    * *
    * * block1.content = desstructlhs
    * * block1.next = cond1
    * * cond1.condtion = lhs.eval
    * * cond1.next = block2
    * * cond1.ifFalse = block{ assign false to %rdi}
    * * block2.content = desstructrhs
    * * block2.next = cond2
    * * cond2.condition = rhs.eval
    * * cond2.next = { assign true to %rdi}
    * * cond2.ifFalse = {assign false to %rdi}
    * * finally { assign %rdi to expr.eval } before the end node.
    *
    * so the final result can be found in %rdi
    *
    * || operator likewise, for other bool operator, it follows the usual start->left->right->self->end convention.
    *
    * @param expr : the expression to destruct
    */
  private def destructLogicalOperation(expr: LogicalOperation): (CFG, CFG) = {
    val placeStr = s"r${expr.line},c${expr.col},Logical"
    val (start, end) = create(placeStr)
    val (lhsSt, lhsEd) = DestructNew(expr.lhs)
    val (rhsSt, rhsEd) = DestructNew(expr.rhs)

    expr.operator match {
      case And => {
        val assignTrue = AssignStatement(expr.line, expr.col,
          expr.eval.get,
          BoolLiteral(expr.line, expr.col, true), None)
        val assignFalse = AssignStatement(expr.line, expr.col,
          expr.eval.get,
          BoolLiteral(expr.line, expr.col, false), None)

        val (trueSt, trueEd) = DestructNew(assignTrue)
        val (falseSt, falseEd) = DestructNew(assignFalse)

        val cond2 = CFGConditional(placeStr+"_cond2", expr.rhs.eval.get, Option(trueSt), Option(falseSt))
        val cond1 = CFGConditional(placeStr+"_cond1", expr.lhs.eval.get, Option(rhsSt), Option(falseSt))

        link(start, lhsSt)
        link(lhsEd, cond1)
        link(rhsEd, cond2)
        link(trueEd, end)
        link(falseEd, end)
      }

      case Or => {
        val assignTrue = AssignStatement(expr.line, expr.col,
          expr.eval.get,
          BoolLiteral(expr.line, expr.col, true), None)
        val assignFalse = AssignStatement(expr.line, expr.col,
          expr.eval.get,
          BoolLiteral(expr.line, expr.col, false), None)

        val (trueSt, trueEd) = DestructNew(assignTrue)
        val (falseSt, falseEd) = DestructNew(assignFalse)

        val cond2 = CFGConditional(placeStr+"_cond2", expr.rhs.eval.get, Option(trueSt), Option(falseSt))
        val cond1 = CFGConditional(placeStr+"_cond1", expr.lhs.eval.get, Option(trueSt), Option(rhsSt))

        link(start, lhsSt)
        link(lhsEd, cond1)
        link(rhsEd, cond2)
        link(trueEd, end)
        link(falseEd, end)
      }

      case _ => {
        link(start, lhsSt)
        link(lhsEd, rhsSt)
        expr.lhs = expr.lhs.eval.get
        expr.rhs = expr.rhs.eval.get
        val self = CFGBlock(placeStr+"_body", ArrayBuffer(expr))
        link(rhsEd, self)
        link(self, end)
      }
    }

    (start, end)
  }

  /**
    * destruct a block. Note that this function does not return a CFG block with begin, end node.
    * when encounter a break or continue, we ignore the rest, and end the transformation.
    *
    * @return (start, end) nodes as a result of destructuring this block
    */
  private def destructBlock(block: Block,
                            loopStart: Option[CFG] = None,
                            loopEnd: Option[CFG] = None): (CFG, CFG) = {
    val placeStr = s"r${block.line},c${block.col},Block"
    val (start, end) = create(placeStr)
    var last = start

    for (stmt <- block.statements) {
      stmt match {
        case break: Break => {
          assert(loopEnd.isDefined)
          link(last, end)
          link(end, loopEnd.get)
          return (start, end)
        }
        case continue: Continue => {
          assert(loopStart.isDefined)
          link(last, end)
          link(end, loopStart.get)
          return (start, end)
        }
        case ret: Return => {
          if (ret.value.isDefined) {
            val (valSt, valEd) = DestructNew(ret.value.get)
            link(last, valSt)
            ret.value = ret.value.get.eval
            last = valEd
          }
        }
        case _ => {
          val (stmtSt, stmtEd) = DestructNew(stmt)
          link(last, stmtSt)
          last = stmtEd
        }
      }
    }

    link(last, end)
    (start, end)
  }

  /**
    * this function first destruct its condition expression.
    * and then tests %rdi, and determine which branch to goto.
    *
    * @param ifstmt
    * @return
    */
  private def destructIf(ifstmt: If,
                         loopStart: Option[CFG] = None,
                         loopEnd: Option[CFG] = None): (CFG, CFG) = {
    val placeStr = s"r${ifstmt.line},c${ifstmt.col},If"
    val (start, end) = create(placeStr)
    val (condSt, condEd) = DestructNew(ifstmt.condition)
    val (nextSt, nextEd) = DestructNew(ifstmt.ifTrue)
    val cfgCond = CFGConditional(placeStr + "_cond", ifstmt.condition.eval.get, Option(nextSt))

    link(condEd, cfgCond)

    if (ifstmt.ifFalse.isDefined) {
      val (ifFalseSt, ifFalseEd) = DestructNew(ifstmt.ifFalse.get)
      cfgCond.ifFalse = Option(ifFalseSt)
      link(ifFalseEd, end)
    }
    else {
      cfgCond.ifFalse = Option(end)
    }
    link(start, condSt)

    (start, end)
  }

  /** Destruct an for loop.
    * 'for' has four parts.
    * 1 init
    * 2 test
    * 3 update
    * 4 body
    *
    * this function create a structure like this one.
    *
    * init
    * |
    * test<----
    * |     |
    * body    |
    * |     |
    * update--|
    *
    * loop start is start node of update. **after continue, you should first goto update rather than test**
    * loop end is ifFalse branch of test
    */
  private def destructFor(forstmt: For): (CFG, CFG) = {
    val placeStr = s"r${forstmt.line},c${forstmt.col},For"
    val (start, end) = create(placeStr)
    val (initSt, initEd) = DestructNew(forstmt.start)
    val (condSt, condEd) = DestructNew(forstmt.condition)
    val (updSt, updEd) = DestructNew(forstmt.update)
    val (bodySt, bodyEd) = DestructNew(forstmt.ifTrue, Option(updSt), Option(end))

    assert(forstmt.condition.eval.isDefined)
    val cfgCond = CFGConditional(placeStr + "_cond", forstmt.condition.eval.get, Option(bodySt), Option(end))

    link(start, initSt)
    link(initEd, condSt)
    link(condEd, cfgCond)
    link(bodyEd, updSt)

    (start, end)
  }

  /** Destruct an while loop.
    * pretty much the same as the previous one.
    */
  private def destructWhile(whilestmt: While): (CFG, CFG) = {
    val placeStr = s"r${whilestmt.line},c${whilestmt.col},While"
    val (start, end) = create(placeStr)
    val (condSt, condEd) = DestructNew(whilestmt.condition)
    val (bodySt, bodyEd) = DestructNew(whilestmt.ifTrue, Option(condSt), Option(end))

    assert(whilestmt.condition.eval.isDefined)
    val cfgCond = CFGConditional(placeStr + "_cond", whilestmt.condition.eval.get, Option(bodySt), Option(end))

    link(start, condSt)
    link(condEd, cfgCond)
    link(bodyEd, condSt)

    (start, end)
  }

  /**
    * create a CFGMethod, where it's label is its method name.
    * then place its block after it.
    *
    * @param method
    * @return
    */
  private def destructMethodDeclaration(method: LocMethodDeclaration): (CFG, CFG) = {
    val placeStr = s"r${method.line},c${method.col},Method"
    val (start, end) = create(placeStr)
    val params = method.params
    val (blockSt, blockEd) = DestructNew(method.block)
    val cfgMthd = CFGMethod(method.name, Option(blockSt), params, method)

    link(start, cfgMthd)
    link(cfgMthd, end)
    (start, end)
  }

  /** destruct its param first. place at it front
    * create a CFGCall that its params contains every param.eval.
    * if the function returns type is not void, copy call.block at next block.
    *
    * @return (start, end, loc) where loc holds the
    */
  private def destructMethodCall(call: MethodCall): (CFG, CFG) = {
    val placeStr = s"r${call.line},c${call.col},Call"
    val (start, end) = create(placeStr)
    val paramList = ArrayBuffer[Expression]()
    var last = start

    for (param <- call.params) {
      val (st, ed) = DestructNew(param)
      link(last, st)
      last = ed
      assert(param.eval.isDefined)
      paramList += param.eval.get
    }

    val mthdCal = CFGMethodCall(placeStr + "_call", paramList.toVector, call.method.get.name)

    link(last, mthdCal)
    last = mthdCal

    if (call.method.get.typ != Option(VoidType)) {
      assert(call.block.isDefined)
      val (st, ed) = DestructNew(call.block.get)
      link(last, st)
      last = ed
    }

    link(last, end)
    (start, end)
  }

  private def destructProgram(program: Program): (CFG, CFG) = {
    val (start, end) = create("")
    val fields = program.fields
    val methods = program.methods map (DestructNew(_)._1.next.get.asInstanceOf[CFGMethod]) // get methods block.
    val cfg = CFGProgram("program", fields, methods)
    link(start, cfg)
    link(cfg, end)
    (start, end)
  }

  /** Destruct an Assignment.
    *
    * Destructs an assignment statement to the following structure:
    * start -> block -> CFGBlock -> end
    * Where `block` contains the flattened code to calculate the location to assign to
    * as well as the expression to assign there.
    * CFGBlock contains one of Increment, Decrement, AssignStatement, CompoundAssignStatement.
    * This function assures that
    * assignment.expression = literal or location,
    * assignment.loc.index = None or literal or location.
    */
  private def destructAssignment(assignment: Assignment): (CFG, CFG) = {
    val placeStr = s"r${assignment.line},c${assignment.col},Assign"
    val (start, end) = create(placeStr)
    val (locSt, locEd) = DestructNew(assignment.loc)
    var last = locEd

    link(start, locSt)

    assignment match {
      case asg: AssignStatement => {
        //println("Asghere") // DEBUG
        val (exprSt, exprEd) = DestructNew(asg.value)
        link(last, exprSt)
        asg.value = asg.value.eval.get
        //println(asg.value.asInstanceOf[FieldDeclaration].name) //DEBUG
        val block = CFGBlock(placeStr + "_assign", ArrayBuffer(asg))
        link(exprEd, block)
        last = block
      }

      case cAsg: CompoundAssignStatement => {
        val (exprSt, exprEd) = DestructNew(cAsg.value)
        link(last, exprSt)
        cAsg.value = cAsg.value.eval.get
        val block = CFGBlock(placeStr + "_assign", ArrayBuffer(cAsg))
        link(exprEd, block)
        last = block
      }

      case inc: Increment => {
        val block = CFGBlock(placeStr + "_assign", ArrayBuffer(inc))
        link(last, block)
        last = block
      }

      case dec: Decrement => {
        val block = CFGBlock(placeStr + "_assign", ArrayBuffer(dec))
        link(last, block)
        last = block
      }

      case _ => throw new NotImplementedError()
    }

    link(last, end)

    (start, end)
  }

  /** Destruct a location. mainly flattens the index part of location, if it's not an array, then this function returns
    * start -> end. also, it sets the loc.index to final result of original loc.index
    *
    * MORE IMPORTANTLY, updates `loc` such that its `index` field now has the
    * location of the temporary variable that contains the result of flattened code.
    *
    * @return (start, end)
    */
  private def destructLocation(loc: Location): (CFG, CFG) = {
    val placeStr = s"r${loc.line},c${loc.col},Loc"
    val (start, end) = create(placeStr)
    if (loc.index.isDefined) {
      val (indexSt, indexEd) = DestructNew(loc.index.get)
      loc.index = loc.index.get.eval
      link(start, indexSt)
      link(indexEd, end)
    }
    else {
      link(start, end)
    }

    (start, end)
  }

  /**
    * if expr does not contain &&, ||, array indexing and function call, return (start)->expr.block->(end)
    * otherwise handles left, right, and create a new block for each.
    * then a block contains { expr.eval = left.eval op right.eval }
    *
    * @param expr
    * @return
    */
  private def destructOperation(expr: Operation): (CFG, CFG) = {
    val placeStr = s"r${expr.line},c${expr.col},Oper"
    val (start, end) = create(placeStr)

    expr match {
      case not: Not => {
        val (exprSt, exprEd) = DestructNew(not.expression)
        link(start, exprSt)

        assert(not.expression.eval.isDefined)
        not.expression = not.expression.eval.get

        val self = CFGBlock(placeStr + "_expr", ArrayBuffer(not))
        link(exprEd, self)
        link(self, end)
      }

      case negate: Negate => {
        val (exprSt, exprEd) = DestructNew(negate.expression)
        link(start, exprSt)

        assert(negate.expression.eval.isDefined)
        negate.expression = negate.expression.eval.get

        val self = CFGBlock(placeStr + "_expr", ArrayBuffer(negate))
        link(exprEd, self)
        link(self, end)
      }

      case arithmeticOperation: ArithmeticOperation => {
        val (lhsSt, lhsEd) = DestructNew(arithmeticOperation.lhs)
        val (rhsSt, rhsEd) = DestructNew(arithmeticOperation.rhs)
        link(start, lhsSt)
        link(lhsEd, rhsSt)

        assert(arithmeticOperation.lhs.eval.isDefined)
        assert(arithmeticOperation.rhs.eval.isDefined)
        arithmeticOperation.lhs = arithmeticOperation.lhs.eval.get
        arithmeticOperation.rhs = arithmeticOperation.rhs.eval.get

        val self = CFGBlock(placeStr + "_expr", ArrayBuffer(arithmeticOperation))
        link(rhsEd, self)
        link(self, end)
      }

      case logicalOperation: LogicalOperation => {
        val (exprSt, exprEd) = destructLogicalOperation(logicalOperation)
        link(start, exprSt)

        assert(logicalOperation.lhs.eval.isDefined)
        assert(logicalOperation.rhs.eval.isDefined)
        logicalOperation.lhs = logicalOperation.lhs.eval.get
        logicalOperation.rhs = logicalOperation.rhs.eval.get

        val self = CFGBlock(placeStr + "_expr", ArrayBuffer(logicalOperation))
        link(exprEd, self)
        link(self, end)
      }

      case ternaryOperation: TernaryOperation => {
        val (condSt, condEd) = DestructNew(ternaryOperation.condition)
        val (ifTrueSt, ifTrueEd) = DestructNew(ternaryOperation.ifTrue)
        val (ifFalseSt, ifFalseEd) = DestructNew(ternaryOperation.ifFalse)

        val assignTrue = AssignStatement(expr.line, expr.col,
          ternaryOperation.eval.get,
          ternaryOperation.ifTrue.eval.get, None)
        val assignFalse = AssignStatement(expr.line, expr.col,
          ternaryOperation.eval.get,
          ternaryOperation.ifFalse.eval.get, None)

        val (trueSt, trueEd) = DestructNew(assignTrue)
        val (falseSt, falseEd) = DestructNew(assignFalse)

        link(ifTrueEd, trueSt)
        link(ifFalseEd, falseSt)

        val cfgCond = CFGConditional(placeStr + "_cond",
          ternaryOperation.condition.eval.get,
          Option(ifTrueSt),
          Option(ifFalseSt),
          Option(end))
        link(start, condSt)
        link(condEd, cfgCond)
        link(trueEd, end)
        link(falseEd, end)
      }
    }

    (start, end)
  }

  /**
    *
    * @param ir        : a flattened ir.
    * @param loopStart a loop start
    * @param loopEnd   a loop end.
    * @return
    */
  def apply( // when called on a program, the returned start node simply points to the CFGProgram
             ir: IR, // the end node of CFGProgram has no meaning
             loopStart: Option[CFG] = None,
             loopEnd: Option[CFG] = None): (CFG, CFG) = {
    ir match {
      // assignment
      case block: Block => destructBlock(block, loopStart, loopEnd) // break and continue only appears here.
      case ifstmt: If => destructIf(ifstmt, loopStart, loopEnd)
      case forstmt: For => destructFor(forstmt)
      case whilestmt: While => destructWhile(whilestmt)
      case method: LocMethodDeclaration => destructMethodDeclaration(method)
      case program: Program => destructProgram(program)
      case call: MethodCall => destructMethodCall(call)
      case expr: Operation => destructOperation(expr)
      case loc: Location => destructLocation(loc) // could only be array location
      case assign: Assignment => destructAssignment(assign)
      case literal: Literal => {
        counter += 1;
        val placeStr = s"emptyBlock${counter}"
        val (start, end) = create(placeStr)
        link(start, end)
        (start, end)
      }
      case _ => throw new NotImplementedError()
    }
  }
}
