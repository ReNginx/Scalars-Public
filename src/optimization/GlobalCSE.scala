package optimization

import scala.collection.mutable.{Map, Set}
import ir.components._
import codegen._

// Note that global CSE only optimizes for Operations, rather than SingleExpr

// for creating the set of all expressions, updates retSet in place
private object GlobalCSE_ExprSet extends GenericOptimization {
  def apply(cfg: CFG, exprSet: Set[Operation], var2Expr: Map[SingleExpr, Set[Operation]]): Unit = {
    if (!cfg.isOptimized(GlobalCSE_ExprSet)) {
      cfg.setOptimized(GlobalCSE_ExprSet)
      cfg match {
        case virtualCFG: VirtualCFG => {
          if (!virtualCFG.next.isEmpty) {
            GlobalCSE_ExprSet(virtualCFG.next.get, exprSet, var2Expr)
          }
        }

        case block: CFGBlock => {
          for (statement <- block.statements) {
            statement match {
              case oper: Operation => {
                exprSet += oper
              }
              case _ =>
            }
          }
        }

        case conditional: CFGConditional => {
          conditional.condition match {
            case oper: Operation => {
              throw new NotImplementedError // oper should always be SingleExpr
            }
            case expr: SingleExpr =>
            case _ => throw new NotImplementedError
          }
          if (!conditional.next.isEmpty) {
            GlobalCSE_ExprSet(conditional, exprSet, var2Expr)
          }
          if (!conditional.ifFalse.isEmpty) {
            GlobalCSE_ExprSet(conditional, exprSet, var2Expr)
          }
          if (!conditional.end.isEmpty) {
            GlobalCSE_ExprSet(conditional, exprSet, var2Expr)
          }
        }

        case method: CFGMethod => {
          if (!method.block.isEmpty) {
            GlobalCSE_ExprSet(method.block.get, exprSet, var2Expr)
          }
        }

        case call: CFGMethodCall => {
          if (!call.next.isEmpty) {
            GlobalCSE_ExprSet(call.next.get, exprSet, var2Expr)
          }
        }

        case program: CFGProgram => {
          program.methods foreach { GlobalCSE_ExprSet(_, exprSet, var2Expr) }
        }
      }
    }
  }
}

// only for the convenience of creating GEN and KILL maps, updates genMap and killMap in place
private object GlobalCSE_GenKill extends GenericOptimization {
  def apply(cfg: CFG, genMap: Map[CFG, Set[Operation]], killMap: Map[CFG, Set[Operation]]): Unit = {
    if (!cfg.isOptimized(GlobalCSE_GenKill)) {
      cfg.setOptimized(GlobalCSE_GenKill)
      cfg match {
        case virtualCFG: VirtualCFG => {
          if (!virtualCFG.next.isEmpty) {
            GlobalCSE_GenKill(virtualCFG.next.get, genMap, killMap)
          }
        }

        case block: CFGBlock => { // only optimize for block and conditional
        }

        case conditional: CFGConditional => {
          // optimize for conditional
          if (!conditional.next.isEmpty) {
            GlobalCSE_GenKill(conditional, genMap, killMap)
          }
          if (!conditional.ifFalse.isEmpty) {
            GlobalCSE_GenKill(conditional, genMap, killMap)
          }
          if (!conditional.end.isEmpty) {
            GlobalCSE_GenKill(conditional, genMap, killMap)
          }
        }

        case method: CFGMethod => {
          if (!method.block.isEmpty) {
            GlobalCSE_GenKill(method.block.get, genMap, killMap)
          }
        }

        case call: CFGMethodCall => {
          if (!call.next.isEmpty) {
            GlobalCSE_GenKill(call.next.get, genMap, killMap)
          }
        }

        case program: CFGProgram => {
          program.methods foreach { GlobalCSE_GenKill(_, genMap, killMap) }
        }
      }
    }
  }
}

object GlobalCSE extends Optimization {

  override def toString(): String = "GlobalCSE"

  val globalTmpSuffix = "_cse_global_tmp"
  private val genMap: Map[CFG, Set[Operation]] = Map[CFG, Set[Operation]]()
  private val killMap: Map[CFG, Set[Operation]] = Map[CFG, Set[Operation]]() 
  private val exprSet: Set[Operation] = Set[Operation]() // the set of operations in the entire CFG
  private val var2Expr: Map[SingleExpr, Set[Operation]] = Map[SingleExpr, Set[Operation]]() // maps each SingleExpr to the set of Operations in which they act as an operand
  private val expr2Val: Map[Operation, Location] = Map[Operation, Location]() // maps each operation to its corresponding eval
  private var exprSetDefined: Boolean = false
  private var genKillDefined: Boolean = false

  override def init(): Unit = {
    resetChanged
    genMap.clear
    killMap.clear
    exprSet.clear
    var2Expr.clear
    exprSetDefined = false
    genKillDefined = false
  }

  private def makeExprSet(cfg: CFG): Unit = {
    if (!exprSetDefined) {
      GlobalCSE_ExprSet(cfg, exprSet, var2Expr)
      // println(exprSet)
      exprSetDefined = true
    }
  }

  private def makeGenKill(cfg: CFG): Unit = {
    if (!genKillDefined) {

    }
  }

  def apply(cfg: CFG, isInit: Boolean=true): Unit = {
    makeExprSet(cfg)
    makeGenKill(cfg)
  }
}