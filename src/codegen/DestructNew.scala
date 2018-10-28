package codegen

import ir.components._
import sun.security.ssl.SSLContextImpl.DefaultSSLContext

import scala.collection.mutable.{ArrayBuffer, Map}

object DestructNew {
  /**
    * Link two CFG together.
    * @param from
    * @param to
    * @return
    */
  private def linkCFG(from: CFG, to: CFG)= {
    from.next = Some(to)
    to.parents.add(from)
  }

  /**
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
    * this function deal with expressions contains && and ||.
    * it should create a structure like the following example.
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
    * @param expr: the expression to destruct
    */
  private def destructBoolOperation(expr: Expression)= {
    throw new NotImplementedError()
  }
  /**
   * destruct a block. Note that this function does not return a CFG block with begin, end node.
    * when encounter a break or continue, we ignore the rest, and end the transformation.
   * @return (start, end) nodes as a result of destructuring this block
   */
  private def destructBlock(
      block: Block,
      loopStart: Option[CFG] = None,
      loopEnd: Option[CFG] = None): (CFG, CFG) = {
    throw new NotImplementedError()
  }

  /**
    * this function first destruct its condition expression.
    * and then tests %rdi, and determine which branch to goto.
    * @param ifstmt
    * @return
    */
  private def destructIf(ifstmt: If): (CFG, CFG) = {
    throw new NotImplementedError()
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
    *   |
    * test<----
    *   |     |
    * body    |
    *   |     |
    * update--|
    *
    * loop start is start node of update. **after continue, you first goto update rather than test**
    * loop end is ifFalse branch of test
   */
  private def destructFor(forstmt: For): (CFG, CFG) = {
    val placeStr = s"r${forstmt.line},c${forstmt.col}"
    val (start, end) = create(placeStr)
    val (initSt, initEd) = DestructNew(forstmt.start)
    val (condSt, condEd) = DestructNew(forstmt.condition)
    val (updSt, updEd) = DestructNew(forstmt.update)
    val (bodySt, bodyEd) = DestructNew(forstmt.ifTrue, Option(updSt), Option(end))

    assert(forstmt.condition.eval.isDefined)
    val cfgCond = CFGConditional(placeStr+"_cond", forstmt.condition.eval.get, Option(bodySt), Option(end))

    linkCFG(start, initSt)
    linkCFG(initEd, condSt)
    linkCFG(condEd, cfgCond)
    linkCFG(cfgCond.next.get.next.get, updSt)

    (start, end)
  }

  /** Destruct an while loop.
    * pretty much the same as the previous one.
   */
  private def destructWhile(whilestmt: While): (CFG, CFG) = {
    throw new NotImplementedError()
  }

  /**
    * create a CFGMethod, where it's label is its method name.
    * then place its block after it.
    * @param method
    * @return
    */
  private def destructMethodDeclaration(method: LocMethodDeclaration): (CFG, CFG) = {
    val placeStr = s"r${method.line},c${method.col}"
    val (start, end) = create(placeStr)
    val params = method.params
    val (blockSt, blockEd) = DestructNew(method.block)
    val cfgMthd = CFGMethod(method.name, Option(blockSt), params)

    linkCFG(start, cfgMthd)
    linkCFG(cfgMthd, end)
    (start, end)
  }

  /** destruct its param first. place at it front
    * create a CFGCall that its params contains every param.eval.
    * if the function returns type is not void, copy call.block at next block.
   * @return (start, end, loc) where loc holds the
   */
  private def destructMethodCall(call: MethodCall): (CFG, CFG) = {
    val placeStr = s"r${call.line},c${call.col}"
    val (start, end) = create(placeStr)
    val paramList = ArrayBuffer[Expression]()
    var last = start

    for (param <- call.params) {
      val (st, ed) = DestructNew(param)
      linkCFG(last, st)
      last = ed
      assert(param.eval.isDefined)
      paramList += param.eval.get
    }

    val mthdCal = CFGMethodCall(placeStr+"_call", paramList.toVector, call.method.get.name)

    linkCFG(last, mthdCal)
    last = mthdCal

    if (call.method.get.typ != Option(VoidType)) {
      assert(call.block.isDefined)
      val (st, ed) = DestructNew(call.block.get)
      linkCFG(last, st)
      last = ed
    }

    linkCFG(last, end)
    (start, end)
  }

  private def destructProgram(program: Program): (CFG, CFG) = {
    val (start, end) = create("")
    val fields = program.fields
    val methods = program.methods map (DestructNew(_)._1.next.get.asInstanceOf[CFGMethod]) // get methods block.
    val cfg = CFGProgram("program", fields, methods)
    linkCFG(start, cfg)
    linkCFG(cfg, end)
    (start, end)
  }

  /** Destruct an Assignment.
   *
   * Destructs an assignment statement to the following structure:
   *     start -> block -> CFGBlock -> end
   * Where `block` contains the flattened code to calculate the location to assign to
   * as well as the expression to assign there.
   * CFGBlock contains one of Increment, Decrement, AssignStatement, CompoundAssignStatement.
   */
  private def destructAssignment(assignment: Assignment): (CFG, CFG) = {
    throw new NotImplementedError()
  }

  /** Destruct a location.
   *
   * MORE IMPORTANTLY, updates `loc` such that its `index` field now has the
   * location of the temporary variable that contains the result of flattened code.
   *
   * @return (start, end, loc) such that:
   *         loc is the original location that was destructed, this is mainly for convenience
   */
  private def destructLocation(loc: Location): (CFG, CFG) = {
    throw new NotImplementedError()
  }

  /**
    * if expr does not contain &&, ||, array indexing and function call, return (start)->expr.block->(end)
    * otherwise handles left, right, and create a new block for each.
    * then a block contains { expr.eval = left.eval op right.eval }
    * @param expr
    * @return
    */
  private def destructExpression(expr: Operation): (CFG, CFG) = {
    throw new NotImplementedError()
  }

  /**
    *
    * @param ir: a flattened ir.
    * @param loopStart a loop start
    * @param loopEnd a loop end.
    * @return
    */
  def apply(  // when called on a program, the returned start node simply points to the CFGProgram
      ir: IR,  // the end node of CFGProgram has no meaning
      loopStart: Option[CFG] = None,
      loopEnd: Option[CFG] = None): (CFG, CFG) = {

    ir match {
      // assignment
      case block: Block => destructBlock(block, loopStart, loopEnd) // break and continue only appears here.
      case ifstmt: If => destructIf(ifstmt)
      case forstmt: For => destructFor(forstmt)
      case whilestmt: While => destructWhile(whilestmt)
      case method: LocMethodDeclaration => destructMethodDeclaration(method)
      case program: Program => destructProgram(program)
      case call: MethodCall => destructMethodCall(call)
      case expr: Operation => destructExpression(expr)
      case loc: Location => destructLocation(loc) // could only be array location
      case assign: Assignment => destructAssignment(assign)
      case _ => throw new NotImplementedError
    }
  }
}
