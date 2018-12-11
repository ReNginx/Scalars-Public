package optimization

import codegen._
import ir.components._

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, HashMap, Map, MultiMap, Set}


/**
  * Note we only deal with ordinary variables. not arrays.
  * Update: limited array support for constant indices.
  */

object GlobalCP extends Optimization {

  override def toString(): String = "GlobalCP"

  type DefId = (CFGBlock, Int)
  val gen = Map[CFG, Set[DefId]]()
  val kill = Map[CFG, Set[DefId]]()
  val cfgs = ArrayBuffer[CFG]()
  val aryBlacklist = Set[ArrayDeclaration]()

  override def init(): Unit = {
    resetChanged
    gen.clear
    kill.clear
    cfgs.clear
    aryBlacklist.clear
  }

  private def aryBlacklistAdd(loc: Location): Unit = {
    assert(loc.index.isDefined) // must be array
    aryBlacklist += loc.field.get.asInstanceOf[ArrayDeclaration]
  }

  private def aryBlacklistNotIn(loc: Location): Boolean = {
    assert(loc.index.isDefined)
    if (aryBlacklist.contains(loc.field.get.asInstanceOf[ArrayDeclaration])) {
      false
    } else {
      true
    }
  }

  /**
    * tell an ir is an array or not.
    *
    * @param ir
    * @return
    */
  def isArray(ir: IR): Boolean = {
    ir match {
      case loc: Location => loc.index.isDefined
      case _ => false
    }
  }

  def isReg(ir: IR): Boolean = {
    ir match {
      case loc: Location => loc.field.get.isReg
      case _ => false
    }
  }

  private def AssignFromReg(defn: Def): Boolean = {
    defn match {
      case asg: AssignStatement => {
        //println(s"${asg.value},  ${isReg(asg.value)}")
        isReg(asg.value)
      }
      case _ => false
    }
  }

  /**
    * add an ordinary var (ignore array defs) definition to locMap, and lastLoc
    * lastLoc keeps track of the last def of a var within a block.
    *
    * @param locMap
    * @param lastLoc
    * @param defn
    * @param id
    */
  private def add(locMap: Map[Location, Set[DefId]],
          lastLoc: Map[Location, DefId],
          defn: IR with Def,
          id: DefId): Unit = {
    if (AssignFromReg(defn)) return
    if (isArray(defn.getLoc)) { // if index is a Location, we add the array definition to blacklist
      if (defn.getLoc.index.get.isInstanceOf[Location]) {
        aryBlacklist += defn.getLoc.field.get.asInstanceOf[ArrayDeclaration]
        return
      }
    }
    if (!locMap.contains(defn.getLoc)) {
      locMap(defn.getLoc) = Set[DefId]()
    }

    locMap(defn.getLoc).add(id)
    lastLoc(defn.getLoc) = id
  }

  /**
    * this function collects all assignments in basic blocks.
    * and recognizes which definitions are killed in the block.
    * it sets gen and kill for blocks.
    */
  private def collect(): Map[Location, Set[DefId]] = {
    val locMap = Map[Location, Set[DefId]]()

    for (cfg <- cfgs) { // we only have virtual, cond, block here.
      cfg match {
        case block: CFGBlock => {
          val lastLoc = Map[Location, DefId]() //keep track of last definition
          val statements = block.statements
          for (idx <- statements.indices) {
            statements(idx) match {
              case defn: Def => {
                add(locMap, lastLoc, defn, (block, idx))
              }

              case _ =>
            }
          }

          gen(block) = Set(lastLoc.values.toVector: _*)
          //println(s"CFG:${block}\n${gen(block)}")
        }

        case other => {
          gen(other) = Set()
          kill(other) = Set()
        }
      }
    }

    for (cfg <- cfgs) {
      cfg match {
        case block: CFGBlock => {
          kill(block) = Set()
          for ((cfg, idx) <- gen(block)) {
            val defn: Def = block.statements(idx).asInstanceOf[Def]
            kill(block) = kill(block) union locMap(defn.getLoc)
          }
        }

        case _ =>

      }
    }

    locMap
  }

  /**
    * transfer a In set to a map mapping from location to value
    *
    * @param in
    * @return
    */
  private def transfer(in: Set[DefId]): MultiMap[Location, DefId] = {
    val ret = new HashMap[Location, Set[DefId]] with mutable.MultiMap[Location, DefId]
    for ((cfg, index) <- in) {
      val blk = cfg.asInstanceOf[CFGBlock]
      val stmt = blk.statements(index)
      val loc = stmt.asInstanceOf[Def].getLoc
      if (!ret.contains(loc)) {
        ret(loc) = Set[DefId]()
      }
      ret(loc).add((cfg, index))
    }
    ret
  }

  /**
    * return the common value(a literal or a location) that location equals to.
    * if there's no such value, return the location itself.
    *
    * @param ids
    * @param loc
    * @return
    */
  private def findCommon(ids: Set[DefId], loc: Location): Expression = {
    val value = Set[Expression]()
    for ((cfg, idx) <- ids) {
      val stmt = cfg.statements(idx)
      //PrintCFG.prtStmt(stmt)
      stmt match {
        case asg: AssignStatement => value += asg.value
        case _ => return loc
      }
    }

    // then their is only one value, we can just return it.
    if (value.size == 1) {
      return value.head
    }

    loc
  }

  /**
    * see if an expr could be replaced.
    * if expr is not a location, this function returns expr
    * if it's an array, we only try to replace its index var.
    *
    * @param in
    * @param lastDef
    * @param expr
    * @return
    */
  private def subExpr(in: MultiMap[Location, DefId],
              lastDef: Map[Location, DefId],
              expr: Expression,
              pos: (CFG, Int)): Expression = {
    var retExpr: Expression = expr

    // recurse first
    retExpr match {
      case loc: Location => {
        if (isArray(loc)) {
          retExpr = loc.copy(
            index = Option(subExpr(in, lastDef, loc.index.get, pos))
          )
        }
      }

      case _ =>
    }

    retExpr match {
      case loc: Location => {
        // only optimize if not an array or not blacklisted
        if (!isArray(loc) || aryBlacklistNotIn(loc)) {
          val from =
            if (lastDef.contains(loc)) {
              Set(lastDef(loc))
            }
            else if (in.contains(loc)) {
              in(loc)
            }
            else Set[DefId]()

          val res = findCommon(from, loc)
          //System.err.println(s"try sub ${loc} with ${res}")
          if (
            res != loc &&
            !isArray(res) && // prevent code generation issues, efficiency is guaranteed by RepeatOptimization
            JudgeSubstitution(from, pos, loc, res)
            ) {
            // println(s"sub ${loc} with ${res}")
            setChanged // substituted by res
            res
          }
          else
            loc
        } else {
          retExpr
        }
      }

      case _ => expr
    }
  }

  /**
    * see if a statement could be replaced by another.
    * all compound assignments and inc, dec would be replaced by unary or binary operation.
    *
    * @param in
    * @param lastDef
    * @param stmt
    * @param id
    * @return
    */
  private def subStmt(in: MultiMap[Location, DefId],
              lastDef: Map[Location, DefId],
              stmt: IR,
              id: DefId): IR = {
    var replacedStmt = stmt
    
    // replace compound statements with operations
    replacedStmt match {
      case casgStmt: CompoundAssignStatement => {
        setChanged // should be replaced in the first iteration
        val expr = subExpr(in, lastDef, casgStmt.loc, id)
        val value = subExpr(in, lastDef, casgStmt.value, id)
        val operator = casgStmt.operator
        replacedStmt = ArithmeticOperation(
          casgStmt.line,
          casgStmt.col,
          Option(casgStmt.loc),
          None,
          operator,
          expr,
          value
        )
      }

      case inc: Increment => {
        setChanged // should be replaced in the first iteration
        replacedStmt = ArithmeticOperation(
          inc.line,
          inc.col,
          Option(inc.loc),
          None,
          Add,
          subExpr(in, lastDef, inc.loc, id),
          IntLiteral(0, 0, 1)
        )
      }

      case dec: Decrement => {
        setChanged // should be replaced in the first iteration
        replacedStmt = ArithmeticOperation(
          dec.line,
          dec.col,
          Option(dec.loc),
          None,
          Subtract,
          subExpr(in, lastDef, dec.loc, id),
          IntLiteral(0, 0, 1)
        )
      }

      case _ =>
    }

    // recurse first
    replacedStmt match {
      case asgStmt: AssignmentStatements => {
        if (isArray(asgStmt.loc))
          asgStmt.loc.index = Option(subExpr(in, lastDef, asgStmt.loc.index.get, id))
      }

      case oper: Operation => {
        if (isArray(oper.eval.get))
          oper.eval.get.index = Option(subExpr(in, lastDef, oper.eval.get.index.get, id))
      }

      case _ =>
    }

    // call subExpr, use subExpr to decide whether to repeat
    replacedStmt match {
      case asgStmt: AssignStatement => {
        replacedStmt = asgStmt.copy(
          value = subExpr(in, lastDef, asgStmt.value, id)
        )
      }

      case not: Not => {
        replacedStmt = not.copy(
          expression = subExpr(in, lastDef, not.expression, id)
        )
      }

      case negate: Negate => {
        replacedStmt = negate.copy(
          expression = subExpr(in, lastDef, negate.expression, id)
        )
      }

      case arith: ArithmeticOperation => {
        replacedStmt = arith.copy(
          lhs = subExpr(in, lastDef, arith.lhs, id),
          rhs = subExpr(in, lastDef, arith.rhs, id)
        )
      }

      case logic: LogicalOperation => {
        replacedStmt = logic.copy(
          lhs = subExpr(in, lastDef, logic.lhs, id),
          rhs = subExpr(in, lastDef, logic.rhs, id)
        )
      }

      case ret: Return => {
        if (ret.value.isDefined) {
          replacedStmt = ret.copy(
            value = Option(subExpr(in, lastDef, ret.value.get, id))
          )
        }
      }

      case _ => throw new NotImplementedError()
    }

    // update lastDef
    stmt match {
      case defn: Def => {
        if (!AssignFromReg(defn)) {
          lastDef(defn.getLoc) = id
        }
      }
      case _ =>
    }

    //PrintCFG.prtStmt(replacedStmt)
    replacedStmt
  }

  /**
    * in are the sets of definitions reaching the beginning of blocks.
    *
    * @param in
    */
  private def subBlocks(in: Map[CFG, Set[DefId]]): Unit = {
    for (cfg <- cfgs) {
      //println(cfg)
      val map = transfer(in(cfg))
      cfg match {
        case block: CFGBlock => {
          val lastLoc = Map[Location, DefId]()
          for (idx <- block.statements.indices) {
            val stmt = block.statements(idx)
            block.statements(idx) = subStmt(map, lastLoc, stmt, (block, idx))
          }
        }

        case conditional: CFGConditional => {
          conditional.condition = subExpr(map, Map(), conditional.condition, (conditional, -1))
        }

        case call: CFGMethodCall => {
          for (index <- call.params.indices) {
            call.params(index) = subExpr(map, Map(), call.params(index), (call, -1))
          }
        }

        case _ =>
      }
    }
  }

  private def copyProp: Unit = {
    collect()
    // println(s"blacklist: ${aryBlacklist}")
    val (in, out) =
      WorkList[DefId](gen,
        kill,
        cfgs(0),
        Set[DefId](),
        "down",
        "union")
    JudgeSubstitution.init(cfgs.toVector)
    subBlocks(in)
  }

  def apply(cfg: CFG, isInit: Boolean=true): Unit = {
    if (isInit) { init }
    if (cfg.isOptimized(GlobalCP)) {
      return
    }
    cfg.setOptimized(GlobalCP)
    cfgs += cfg

    cfg match {
      case program: CFGProgram => {
        program.methods foreach (GlobalCP(_, false))
      }

      // we collect all blocks of a function.
      case method: CFGMethod => {
        if (method.block.isDefined) {
          gen.clear
          kill.clear
          cfgs.clear
          GlobalCP(method.block.get, false)
          JudgeSubstitution.method = Option(method)
          copyProp
        }
      }

      case cond: CFGConditional => {
        if (cond.next.isDefined) {
          GlobalCP(cond.next.get, false)
        }
        if (cond.ifFalse.isDefined) {
          GlobalCP(cond.ifFalse.get, false)
        }
      }

      case other => {
        if (other.next.isDefined) {
          GlobalCP(other.next.get, false)
        }
      }
    }
  }
}
