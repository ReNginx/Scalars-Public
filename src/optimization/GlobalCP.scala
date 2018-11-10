package optimization

import codegen._
import ir.components._

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, HashMap, Map, MultiMap, Set}


/**
  * Note we only deal with ordinary variables. not arrays.
  */

object GlobalCP extends Optimization {
  type DefId = (CFGBlock, Int)
  val gen = Map[CFG, Set[DefId]]()
  val kill = Map[CFG, Set[DefId]]()
  val funcCfgs = ArrayBuffer[CFG]()


  /**
    * tell a location is an array or not.
    *
    * @param loc
    * @return
    */
  def isArray(loc: Location): Boolean = {
    loc.index.isDefined
  }

  /**
    * add an ordinary var(ignore array defs) definition to locMap, and lastLoc
    * lastLoc keeps track of the last def of a var within a block.
    *
    * @param locMap
    * @param lastLoc
    * @param defn
    * @param id
    */
  def add(locMap: Map[Location, Set[DefId]],
          lastLoc: Map[Location, DefId],
          defn: IR with Def,
          id: DefId): Unit = {
    if (isArray(defn.getLoc)) return
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
  def collect(): Unit = {
    val locMap = Map[Location, Set[DefId]]()

    for (cfg <- funcCfgs) { // we only have virtual, cond, block here.
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
        }

        case other => {
          gen(other) = Set()
          kill(other) = Set()
        }
      }
    }

    for (cfg <- funcCfgs) {
      cfg match {
        case block: Block => {
          kill(block) = Set()
          for ((cfg, idx) <- gen(block)) {
            val defn: Def = block.statements(idx).asInstanceOf[Def]
            kill(block) = kill(block) union locMap(defn.getLoc)
          }
        }

        case _ =>

      }
    }
  }

  /**
    * transfer a In set to a map mapping from location to value
    *
    * @param in
    * @return
    */
  def transfer(in: Set[DefId]): MultiMap[Location, DefId] = {
    val ret = new HashMap[Location, Set[DefId]] with mutable.MultiMap[Location, DefId]
    for ((cfg, index) <- in) {
      val blk = cfg.asInstanceOf[Block]
      val stmt = blk.statements(index)
      val loc = stmt.asInstanceOf[Def].getLoc
      ret(loc).add((cfg, index))
    }
    ret
  }

  /**
    * return the common value(a literal or a location) that location equals to.
    * if there's no such value, return the location itself.
    * @param ids
    * @param loc
    * @return
    */
  def findCommon(ids: Set[DefId], loc: Location): Expression = {
    val value = Set[Expression]()
    for ((cfg, idx) <- ids) {
      val stmt = cfg.statements(idx)
      stmt match {
        case asg: AssignmentStatements => value += asg.value
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
  def subExpr(in: MultiMap[Location, DefId],
              lastDef: Map[Location, DefId],
              expr: Expression): Expression = {
    expr match {
      case loc: Location => {
        if (isArray(loc)) {
          loc.copy(
            index = Option(subExpr(in, lastDef, loc.index.get))
          )
        }
        else {
          if (lastDef.contains(loc)) {
            findCommon(Set(lastDef(loc)), loc)
          }
          else {
            findCommon(in(loc), loc)
          }
        }
      }

      case _ => expr
    }
  }

  /**
    * see if a statement could be replaced by another.
    * all compound assignments and inc, dec would be replaced by unary or binary operation.
    * @param in
    * @param lastDef
    * @param stmt
    * @param id
    * @return
    */
  def subStmt(in: MultiMap[Location, DefId],
              lastDef: Map[Location, DefId],
              stmt: IR,
              id: DefId): IR = {
    var replacedStmt = stmt
    stmt match {
      case asgStmt: AssignStatement => {
        replacedStmt = asgStmt.copy(
          value = subExpr(in, lastDef, asgStmt.value)
        )
      }

      case casgStmt: CompoundAssignStatement => {
        val expr = subExpr(in, lastDef, casgStmt.loc)
        val value = subExpr(in, lastDef, casgStmt.loc)
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
        replacedStmt = ArithmeticOperation(
          inc.line,
          inc.col,
          Option(inc.loc),
          None,
          Add,
          subExpr(in, lastDef, inc.loc),
          IntLiteral(0, 0, 1)
        )
      }

      case dec: Decrement => {
        replacedStmt = ArithmeticOperation(
          dec.line,
          dec.col,
          Option(dec.loc),
          None,
          Subtract,
          subExpr(in, lastDef, dec.loc),
          IntLiteral(0, 0, 1)
        )
      }

      case not: Not => {
        replacedStmt = not.copy(
          expression = subExpr(in, lastDef, not.expression)
        )
      }

      case negate: Negate => {
        replacedStmt = negate.copy(
          expression = subExpr(in, lastDef, negate.expression)
        )
      }

      case arith: ArithmeticOperation => {
        replacedStmt = arith.copy(
          lhs = subExpr(in, lastDef, arith.lhs),
          rhs = subExpr(in, lastDef, arith.rhs)
        )
      }

      case logic: LogicalOperation => {
        replacedStmt = logic.copy(
          lhs = subExpr(in, lastDef, logic.lhs),
          rhs = subExpr(in, lastDef, logic.rhs)
        )
      }

      case ret: Return => {
        if (ret.value.isDefined) {
          replacedStmt = ret.copy(
            value = Option(subExpr(in, lastDef, ret.value.get))
          )
        }
      }

      case _ => throw new NotImplementedError()
    }

    replacedStmt match {
      case asgStmt: AssignmentStatements => {
        if (isArray(asgStmt.loc))
          asgStmt.loc.index = Option(subExpr(in, lastDef, asgStmt.loc.index.get))
      }

      case oper: Operation => {
        if (isArray(oper.eval.get))
          oper.eval.get.index = Option(subExpr(in, lastDef, oper.eval.get.index.get))
      }
    }

    stmt match {
      case defn: Def => {
        lastDef(defn.getLoc) = id
      }
      case _ =>
    }

    replacedStmt
  }

  /**
    * in are the sets of definitions reaching the beginning of blocks.
    * @param in
    */
  def subBlocks(in: Map[CFG, Set[DefId]]): Unit = {
    for (cfg <- funcCfgs) {
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
          conditional.condition = subExpr(map, Map(), conditional.condition)
        }

        case call: CFGMethodCall => {
          for (index <- call.params.indices) {
            call.params(index) = subExpr(map, Map(), call.params(index))
          }
        }

        case _ =>
      }
    }
  }

  def copyProp: Unit = {
    collect()
    val (in, out) =
      WorkList[DefId](gen,
        kill,
        funcCfgs(0),
        Set[DefId](),
        "down",
        "union")
//    subBlocks(in)
  }

  def apply(cfg: CFG): Unit = {
    if (cfg.isOptimized(GlobalCP)) {
      return
    }
    cfg.setOptimized(GlobalCP)
    funcCfgs += cfg

    cfg match {
      case program: CFGProgram => {
        program.methods foreach (GlobalCP(_))
      }

      // we collect all blocks of a function.
      case method: CFGMethod => {
        if (method.block.isDefined) {
          gen.clear
          kill.clear
          funcCfgs.clear
          GlobalCP(method.block.get)
          copyProp
        }
      }

      case cond: CFGConditional => {
        if (cond.next.isDefined) {
          GlobalCP(cond.next.get)
        }
        if (cond.ifFalse.isDefined) {
          GlobalCP(cond.ifFalse.get)
        }
      }

      case other => {
        if (other.next.isDefined) {
          GlobalCP(other.next.get)
        }
      }
    }
  }
}
