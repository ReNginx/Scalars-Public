package codegen

import ir.components._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object Destruct {
  var counter = 0
  val reconstructLogical = mutable.Set[(CFG, CFG, CFG, CFG, CFG, CFG, CFG)]()

  /**
    * Link two CFG together.
    *
    * @param from
    * @param to
    * @return
    */
  private def link(from: CFG, to: CFG) = {
    if (from.next.isEmpty) {
      from.next = Some(to)
      to.parents.add(from)
    }
  }


  def linkFalse(from: CFGConditional, to: CFG) = {
    if (from.ifFalse.isEmpty) {
      from.ifFalse = Some(to)
      to.parents.add(from)
    }
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
    * this function reconstructs short-circuiting.
    * Note that this function should run after a normal destruction.
    * and this function should only run once.
    */
  def reconstruct() {
    System.err.println(reconstructLogical)
    for ((start, lhsSt, lhsEd, rhsSt, rhsEd, body, end) <- reconstructLogical) {
      val expr = body.asInstanceOf[CFGBlock].statements(0).asInstanceOf[LogicalOperation]
      counter = counter + 1
      val placeStr = s"_${counter}_r${expr.line}_c${expr.col}_Logical"

      Vector(start, lhsEd, rhsEd, body) foreach ( x => x.next = None)
      Vector(lhsSt, rhsSt, body, end) foreach ( x => x.parents.clear)

      expr.operator match {
        case And => {
          val assignTrue = AssignStatement(expr.line, expr.col,
            expr.eval.get,
            BoolLiteral(expr.line, expr.col, true))
          val assignFalse = AssignStatement(expr.line, expr.col,
            expr.eval.get,
            BoolLiteral(expr.line, expr.col, false))

          val (trueSt, trueEd) = Destruct(assignTrue)
          val (falseSt, falseEd) = Destruct(assignFalse)

          val cond2 = CFGConditional(placeStr + "_cond2", expr.rhs.eval.get, Option(trueSt), Option(falseSt), Option(end))
          val cond1 = CFGConditional(placeStr + "_cond1", expr.lhs.eval.get, Option(rhsSt), Option(falseSt), Option(end))

          link(start, lhsSt)
          link(lhsEd, cond1)
          link(rhsEd, cond2)
          link(trueEd, end)
          link(falseEd, end)
        }

        case Or => {
          val assignTrue = AssignStatement(expr.line, expr.col,
            expr.eval.get,
            BoolLiteral(expr.line, expr.col, true))
          val assignFalse = AssignStatement(expr.line, expr.col,
            expr.eval.get,
            BoolLiteral(expr.line, expr.col, false))

          val (trueSt, trueEd) = Destruct(assignTrue)
          val (falseSt, falseEd) = Destruct(assignFalse)

          val cond2 = CFGConditional(placeStr + "_cond2", expr.rhs.eval.get, Option(trueSt), Option(falseSt), Option(end))
          val cond1 = CFGConditional(placeStr + "_cond1", expr.lhs.eval.get, Option(trueSt), Option(rhsSt), Option(end))

          link(start, lhsSt)
          link(lhsEd, cond1)
          link(rhsEd, cond2)
          link(trueEd, end)
          link(falseEd, end)
        }
      }
    }
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
    val placeStr = s"_${counter}_r${expr.line}_c${expr.col}_Logical"
    val (start, end) = create(placeStr)
    val (lhsSt, lhsEd) = Destruct(expr.lhs)
    val (rhsSt, rhsEd) = Destruct(expr.rhs)

    expr.operator match {
      //      case And => {
      //        val assignTrue = AssignStatement(expr.line, expr.col,
      //          expr.eval.get,
      //          BoolLiteral(expr.line, expr.col, true))
      //        val assignFalse = AssignStatement(expr.line, expr.col,
      //          expr.eval.get,
      //          BoolLiteral(expr.line, expr.col, false))
      //
      //        val (trueSt, trueEd) = Destruct(assignTrue)
      //        val (falseSt, falseEd) = Destruct(assignFalse)
      //
      //        val cond2 = CFGConditional(placeStr+"_cond2", expr.rhs.eval.get, Option(trueSt), Option(falseSt), Option(end))
      //        val cond1 = CFGConditional(placeStr+"_cond1", expr.lhs.eval.get, Option(rhsSt), Option(falseSt), Option(end))
      //
      //        link(start, lhsSt)
      //        link(lhsEd, cond1)
      //        link(rhsEd, cond2)
      //        link(trueEd, end)
      //        link(falseEd, end)
      //      }
      //
      //      case Or => {
      //        val assignTrue = AssignStatement(expr.line, expr.col,
      //          expr.eval.get,
      //          BoolLiteral(expr.line, expr.col, true))
      //        val assignFalse = AssignStatement(expr.line, expr.col,
      //          expr.eval.get,
      //          BoolLiteral(expr.line, expr.col, false))
      //
      //        val (trueSt, trueEd) = Destruct(assignTrue)
      //        val (falseSt, falseEd) = Destruct(assignFalse)
      //
      //        val cond2 = CFGConditional(placeStr+"_cond2", expr.rhs.eval.get, Option(trueSt), Option(falseSt), Option(end))
      //        val cond1 = CFGConditional(placeStr+"_cond1", expr.lhs.eval.get, Option(trueSt), Option(rhsSt), Option(end))
      //
      //        link(start, lhsSt)
      //        link(lhsEd, cond1)
      //        link(rhsEd, cond2)
      //        link(trueEd, end)
      //        link(falseEd, end)
      //      }

      case _ => {
        link(start, lhsSt)
        link(lhsEd, rhsSt)
        expr.lhs = expr.lhs.eval.get
        expr.rhs = expr.rhs.eval.get
        val body = CFGBlock(placeStr + "_body", ArrayBuffer(expr))
        link(rhsEd, body)
        link(body, end)
        if (expr.operator == Or || expr.operator == And) {
          val tuple = (start, lhsSt, lhsEd, rhsSt, rhsEd, body, end)
          reconstructLogical += tuple
        }
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
                            loopEnd: Option[CFG] = None,
                            funcEnd: Option[CFG] = None): (CFG, CFG)

  = {
    val placeStr = s"_${counter}_r${block.line}_c${block.col}_Block"
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
            val (valSt, valEd) = Destruct(ret.value.get)
            link(last, valSt)
            ret.value = ret.value.get.eval
            last = valEd
          }
          val cfg = CFGBlock(placeStr + "_ret", ArrayBuffer(ret))
          link(last, cfg)
          if (funcEnd.isDefined) {
            link(cfg, end)
            link(end, funcEnd.get)
            return (start, end)
          }
          last = cfg
        }
        case _ => {
          val (stmtSt, stmtEd) = Destruct(stmt, loopStart, loopEnd, funcEnd)
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
                         loopEnd: Option[CFG] = None,
                         funcEnd: Option[CFG] = None): (CFG, CFG)

  = {
    val placeStr = s"_${counter}_r${ifstmt.line}_c${ifstmt.col}_If"
    val (start, end) = create(placeStr)
    val (condSt, condEd) = Destruct(ifstmt.condition)
    val (nextSt, nextEd) = Destruct(ifstmt.ifTrue, loopStart, loopEnd, funcEnd)
    val cfgCond = CFGConditional(placeStr + "_cond", ifstmt.condition.eval.get, Option(nextSt), end = Option(end))
    assert(nextSt.parents.contains(cfgCond))
    link(condEd, cfgCond)

    if (ifstmt.ifFalse.isDefined) {
      val (ifFalseSt, ifFalseEd) = Destruct(ifstmt.ifFalse.get, loopStart, loopEnd, funcEnd)
      linkFalse(cfgCond, ifFalseSt)
      link(ifFalseEd, end)
      if (ifFalseEd.next == loopEnd)
        cfgCond.end = loopEnd
      if (ifFalseEd.next == funcEnd)
        cfgCond.end = funcEnd
    }
    else {
      linkFalse(cfgCond, end)
    }
    link(start, condSt)
    link(nextEd, end)

    if (nextEd.next == loopEnd)
      cfgCond.end = loopEnd
    if (nextEd.next == funcEnd)
      cfgCond.end = funcEnd
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
  private def destructFor(forstmt: For, funcEnd: Option[CFG] = None): (CFG, CFG)

  = {
    val placeStr = s"_${counter}_r${forstmt.line}_c${forstmt.col}_For"
    val (start, end) = create(placeStr)
    val (initSt, initEd) = Destruct(forstmt.start)
    val (condSt, condEd) = Destruct(forstmt.condition)
    val (updSt, updEd) = Destruct(forstmt.update)
    val (bodySt, bodyEd) = Destruct(forstmt.ifTrue, Option(updSt), Option(end), funcEnd)

    assert(forstmt.condition.eval.isDefined)
    val cfgCond = CFGConditional(placeStr + "_cond", forstmt.condition.eval.get, Option(bodySt), Option(end), Option(end))

    link(start, initSt)
    link(initEd, condSt)
    link(condEd, cfgCond)
    link(bodyEd, updSt)
    link(updEd, condSt)

    (start, end)
  }

  /** Destruct an while loop.
    * pretty much the same as the previous one.
    */
  private def destructWhile(whilestmt: While, funcEnd: Option[CFG] = None): (CFG, CFG)

  = {
    val placeStr = s"_${counter}_r${whilestmt.line}_c${whilestmt.col}_While"
    val (start, end) = create(placeStr)
    val (condSt, condEd) = Destruct(whilestmt.condition)
    val (bodySt, bodyEd) = Destruct(whilestmt.ifTrue, Option(condSt), Option(end), funcEnd)

    assert(whilestmt.condition.eval.isDefined)
    val cfgCond = CFGConditional(placeStr + "_cond", whilestmt.condition.eval.get, Option(bodySt), Option(end), Option(end))

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
  private def destructMethodDeclaration(method: LocMethodDeclaration): (CFG, CFG)

  = {
    val placeStr = s"_${counter}_r${method.line}_c${method.col}_Method"
    val (start, end) = create(placeStr)
    val params = method.params
    val (blockSt, blockEd) = Destruct(method.block, None, None, funcEnd = Option(end))
    val cfgMthd = CFGMethod(method.name, Option(blockSt), params, method)

    link(start, cfgMthd)
    link(blockEd, end)
    (start, end)
  }

  /** destruct its param first. place at it front
    * create a CFGCall that its params contains every param.eval.
    * if the function returns type is not void, copy call.block at next block.
    *
    * @return (start, end, loc) where loc holds the
    */
  private def destructMethodCall(call: MethodCall): (CFG, CFG)

  = {
    val placeStr = s"_${counter}_r${call.line}_c${call.col}_call"
    val (start, end) = create(placeStr)
    val paramList = ArrayBuffer[Expression]()
    var last = start

    for (param <- call.params) {
      val (st, ed) = Destruct(param)
      link(last, st)
      last = ed
      assert(param.eval.isDefined)
      paramList += param.eval.get
    }

    val mthdCal = CFGMethodCall(placeStr + "_call", ArrayBuffer(paramList: _*), call.method.get.name)

    link(last, mthdCal)
    last = mthdCal

    if (call.method.get.typ != Option(VoidType)) {
      //assert(call.eval.isDefined)
      if (call.eval.isDefined) {
        val copy = AssignStatement(call.line, call.col,
          call.eval.get,
          Location(0, 0, "rax", None, Option(Registers("rax"))))
        val block = CFGBlock(placeStr + "_block", ArrayBuffer(copy))
        link(last, block)
        last = block
      }
    }

    link(last, end)
    (start, end)
  }

  private def destructProgram(program: Program): (CFG, CFG)

  = {
    val (start, end) = create("")
    val fields = program.fields
    val methods = program.methods map (Destruct(_)._1.next.get.asInstanceOf[CFGMethod]) // get methods block.
    val cfg = CFGProgram("program", fields, methods)

    fields foreach {
      case array: ArrayDeclaration => array.isGlobal = true
      case variable: VariableDeclaration => variable.isGlobal = true
    }

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
  private def destructAssignment(assignment: Assignment): (CFG, CFG)

  = {
    val placeStr = s"_${counter}_r${assignment.line}_c${assignment.col}_Assign"
    val (start, end) = create(placeStr)
    val (locSt, locEd) = Destruct(assignment.loc)
    var last = locEd

    link(start, locSt)

    assignment match {
      case asg: AssignStatement => {
        //println("Asghere") // DEBUG

        /* when we encouner assigning a temp to a non-temp,
          this implies this temp comes from an operation. which means we  can safely swap
          their place and won't cause any problem.
        */
        if (Helper.nameEndsWith(asg.value.eval.get, "_tmp") &&
          !Helper.nameEndsWith(asg.loc, "_tmp") &&
          asg.value.isInstanceOf[Operation]) {
          val oper = asg.value.asInstanceOf[Operation]
          val tmp = oper.eval.get
          asg.value.asInstanceOf[Operation].eval = Option(asg.loc)
          asg.loc = tmp.asInstanceOf[Location]
        }

        val (exprSt, exprEd) = Destruct(asg.value)
        link(last, exprSt)
        asg.value = asg.value.eval.get

        //println(asg.value.asInstanceOf[FieldDeclaration].name) //DEBUG
        val block = CFGBlock(placeStr + "_assign", ArrayBuffer(asg))
        link(exprEd, block)
        last = block
      }

      case cAsg: CompoundAssignStatement => {
        val (exprSt, exprEd) = Destruct(cAsg.value)
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
  // private def destructLocation(loc: Location): (CFG, CFG) = {
  //   val placeStr = s"_${counter}_r${loc.line}_c${loc.col}_Loc"
  //   val (start, end) = create(placeStr)
  //   if (loc.index.isDefined) {
  //     loc.index.get match {
  //       case location: Location => {
  //         //println(loc.field.get.name)
  //         val (indexSt, indexEd) = Destruct(loc.index.get)
  //         link(start, indexSt)
  //         loc.index = loc.index.get.eval
  //         val self = CFGBlock(placeStr + "_index", ArrayBuffer(loc))
  //         link(indexEd, self)
  //         link(self, end)
  //       }
  //       case _ => {
  //         val (indexSt, indexEd) = Destruct(loc.index.get)
  //         loc.index = loc.index.get.eval
  //         link(start, indexSt)
  //         link(indexEd, end)
  //       }
  //     }
  //   }
  //   else {
  //     link(start, end)
  //   }
  //
  //   (start, end)
  // }

  private def destructLocation(loc: Location): (CFG, CFG)

  = {
    val placeStr = s"_${counter}_r${loc.line}_c${loc.col}_Loc"
    val (start, end) = create(placeStr)
    var last = start
    if (loc.index.isDefined) {
      val (indexSt, indexEd) = Destruct(loc.index.get)
      link(last, indexSt)
      last = indexEd
      loc.index = loc.index.get.eval
      loc.index.get match {
        case indLoc: Location => {
          if (indLoc.index.isDefined) {
            val assign: AssignStatement = AssignStatement(indLoc.line, indLoc.col, loc.evalLoc.get.asInstanceOf[Location], indLoc)
            val block = CFGBlock(placeStr + "_ind", ArrayBuffer(assign))
            link(last, block)
            last = block
            loc.index = loc.evalLoc
          }
        }
        case lit: Literal =>
      }
      link(last, end)
    }
    else {
      link(start, end)
    }
    // val locSelf = CFGBlock(placeStr + "_self", ArrayBuffer(loc))
    // link(last, locSelf)
    // last = locSelf
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
  private def destructOperation(expr: Operation): (CFG, CFG)

  = {
    val placeStr = s"_${counter}_r${expr.line}_c${expr.col}_Oper"
    val (start, end) = create(placeStr)

    expr match {
      case not: Not => {
        val (exprSt, exprEd) = Destruct(not.expression)
        link(start, exprSt)

        assert(not.expression.eval.isDefined)
        not.expression = not.expression.eval.get

        val self = CFGBlock(placeStr + "_expr", ArrayBuffer(not))
        link(exprEd, self)
        link(self, end)
      }

      case negate: Negate => {
        val (exprSt, exprEd) = Destruct(negate.expression)
        link(start, exprSt)

        assert(negate.expression.eval.isDefined)
        negate.expression = negate.expression.eval.get

        val self = CFGBlock(placeStr + "_expr", ArrayBuffer(negate))
        link(exprEd, self)
        link(self, end)
      }

      case arithmeticOperation: ArithmeticOperation => {
        val (lhsSt, lhsEd) = Destruct(arithmeticOperation.lhs)
        val (rhsSt, rhsEd) = Destruct(arithmeticOperation.rhs)
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
        link(exprEd, end)
      }

      case ternaryOperation: TernaryOperation => {
        val (condSt, condEd) = Destruct(ternaryOperation.condition)
        val (ifTrueSt, ifTrueEd) = Destruct(ternaryOperation.ifTrue)
        val (ifFalseSt, ifFalseEd) = Destruct(ternaryOperation.ifFalse)

        val assignTrue = AssignStatement(expr.line, expr.col,
          ternaryOperation.eval.get,
          ternaryOperation.ifTrue.eval.get)
        val assignFalse = AssignStatement(expr.line, expr.col,
          ternaryOperation.eval.get,
          ternaryOperation.ifFalse.eval.get)

        val (trueSt, trueEd) = Destruct(assignTrue)
        val (falseSt, falseEd) = Destruct(assignFalse)

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

    // now eval is not always a temp anymore
    val (evalSt, evalEd) = Destruct(expr.eval.get)
    link(end, evalSt)
    (start, evalEd)
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
             loopEnd: Option[CFG] = None,
             funcEnd: Option[CFG] = None): (CFG, CFG) = {
    counter += 1;
    //println(ir.getClass.toString) //DEBUG
    ir match {
      // assignment
      case block: Block => destructBlock(block, loopStart, loopEnd, funcEnd) // break and continue only appears here.
      case ifstmt: If => destructIf(ifstmt, loopStart, loopEnd, funcEnd)
      case forstmt: For => destructFor(forstmt, funcEnd)
      case whilestmt: While => destructWhile(whilestmt, funcEnd)
      case method: LocMethodDeclaration => destructMethodDeclaration(method)
      case program: Program => destructProgram(program)
      case call: MethodCall => destructMethodCall(call)
      case expr: Operation => destructOperation(expr)
      case loc: Location => destructLocation(loc) // could only be array location
      case assign: Assignment => destructAssignment(assign)
      case literal: Literal => {
        val placeStr = s"emptyBlock${counter}"
        val (start, end) = create(placeStr)
        link(start, end)
        (start, end)
      }
      case _ => throw new NotImplementedError()
    }
  }
}
