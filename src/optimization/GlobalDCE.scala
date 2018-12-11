package optimization

import codegen._
import ir.components._
import ir.PrettyPrint

import codegen._
import scala.util.control.Breaks._
import Labeling.StmtId

import scala.collection.generic.Growable
import scala.collection.mutable.{ ArrayBuffer, Queue, Set }

/**
 * before using this function, be sure that each function has one common end node.
 */
object GlobalDCE extends Optimization {

  override def toString(): String = "GlobalDCE"

  type Place = (FieldDeclaration, CFG, Int)
  val cfgs = Set[CFG]()
  val unused = Set[(CFGBlock, Int)]()
  var globalVars = Vector[FieldDeclaration]()
  val dumbDecl = VariableDeclaration(0,0,"__dumb__",None)
  val vis = Set[CFG]()

  private def addHelper(iter: Growable[Place], cfg: CFG): (Expression, Int) => Unit = {
    def addExpr(expr: Expression, pos: Int): Unit = {
      expr match {
        case loc: Location => {
          val plc = (loc.field.get, cfg, pos)
          iter += plc
          if (loc.index.isDefined)
            addExpr(loc.index.get, pos)
        }
        case _ =>
      }
    }

    addExpr
  }

  private def getRequired(mthd: CFGMethod): Set[Place] = {
    val required = Set[Place]()

    for (cfg <- cfgs) {
      val addExpr = addHelper(required, cfg)
      cfg match {
        case block: CFGBlock => {
          for (idx <- block.statements.indices) {
            val stmt = block.statements(idx)
            stmt match {
              case ret: Return => {
                if (ret.value.isDefined) {
                  addExpr(ret.value.get, idx)
                }
                val placeHolder = (dumbDecl, cfg, idx)
                required += placeHolder
              }
              case _ =>
            }
          }
        }
        case cond: CFGConditional => {
          addExpr(cond.condition, -1)
        }

        case call: CFGMethodCall => {
          for (param <- call.params) {
            addExpr(param, -1)
          }
        }

        case virtual: VirtualCFG => {
          if (virtual.next.isEmpty) {
            for (decl <- globalVars) {
              val loc = Location(0, 0, decl.name, None, Option(decl))
              addExpr(loc, -1)
            }
          }
        }
      }
    }
    required
  }

  private def eliminateUnused(mthd: CFGMethod): Unit = {
    val (_, _, graph) = Labeling(cfgs.toVector)
    val requiredPlaces = getRequired(mthd)
    val queue = Queue[Place]() ++ requiredPlaces
    val requiredStmts = Set[StmtId]() ++ (requiredPlaces flatMap (x => Vector((x._2, x._3))))

    while (queue.nonEmpty) {
      val head = queue.dequeue
      //System.err.println(head)
      def expand: Unit = {
        val decl = head._1
        val pos = (head._2, head._3)
        val expandQueue = Queue[StmtId](pos)
        val vis = Set[StmtId](pos)

        while (expandQueue.nonEmpty) {
          val head = expandQueue.dequeue

            for (nxt <- graph(head)) {
              breakable {
                if (nxt._2 >= 0) {
                  val stmt = nxt._1.asInstanceOf[CFGBlock].statements(nxt._2)

                  if (stmt.isInstanceOf[Def]) {
                    val stmtLoc = stmt.asInstanceOf[Def].getLoc
                    val addExpr = addHelper(queue, nxt._1)
                    // this is a required definition
                    // mark this stmt as required, and put it's use into the queue.
                    if (decl == stmtLoc.field.get) {
                      if (!requiredStmts.contains(nxt)) {
                        requiredStmts += nxt
                        if (stmtLoc.index.isDefined) {
                          addExpr(stmtLoc.index.get, nxt._2)
                        }

                        stmt match {
                          case asgStmt: AssignmentStatements => {
                            addExpr(asgStmt.value, nxt._2)
                          }

                          case unary: UnaryOperation => {
                            addExpr(unary.expression, nxt._2)
                          }

                          case binary: BinaryOperation => {
                            addExpr(binary.lhs, nxt._2)
                            addExpr(binary.rhs, nxt._2)
                          }

                          case _ =>
                        }
                      }
                      if (!decl.isInstanceOf[ArrayDeclaration])
                        break
                    }
                  }
                }

                if (!vis.contains(nxt)) {
                  vis += nxt
                  expandQueue.enqueue(nxt)
                }
            }
          }
        }
      }

      expand
    }

    for (cfg <- cfgs) {
      cfg match {
        case block: CFGBlock => {
          val newStmt = ArrayBuffer[IR]()
          for (i <- block.statements.indices) {
            val pos = (block, i)
            if (requiredStmts.contains(pos)) {
              newStmt += block.statements(i)
            } else {
              setChanged // statement is removed
              // PrintCFG.prtStmt(block.statements(i))
              // PrettyPrint(block.statements(i), 0)
            }
          }

          block.statements = newStmt
        }

        case _ =>
      }
    }
  }

  def apply(cfg: CFG, isInit: Boolean=true): Unit = {
    if (isInit) { init() }
    if (vis.contains(cfg)) {
      return
    }
    vis += cfg
    cfgs += cfg

    cfg match {
      case program: CFGProgram => {
        globalVars = program.fields
        program.methods foreach (GlobalDCE(_, isInit = false))
        vis.clear
      }

      // we collect all blocks of a function.
      case method: CFGMethod => {
        if (method.block.isDefined) {
          cfgs.clear
          unused.clear
          GlobalDCE(method.block.get, isInit = false)
          eliminateUnused(method)
        }
      }

      case cond: CFGConditional => {
        if (cond.next.isDefined) {
          GlobalDCE(cond.next.get, isInit = false)
        }
        if (cond.ifFalse.isDefined) {
          GlobalDCE(cond.ifFalse.get, isInit = false)
        }
      }

      case other => {
        if (other.next.isDefined) {
          GlobalDCE(other.next.get, isInit = false)
        }
      }
    }
  }
}
