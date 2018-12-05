package optimization.reg_alloc

import codegen._
import ir.components.{Def, Location, Use}
import optimization.GlobalCP.isArray
import optimization.Labeling
import optimization.Labeling.{StmtId, getStmt}

import scala.collection.mutable.{Queue, Set}

/**
  * apply this function to a CFGProgram.
  * would find all DU chains in the program, and store them in duChains.
  * note that this function does not take function calls into account.
  * and also, this function only find du chains in vaiables, not arrays.
  */
object DUChainConstruct {
  val cfgs = Set[CFG]()
  val duChains = Set[DefUseChain]()

  def findDuChain(): Unit = {
    val (calls, graph, revGraph) = Labeling(cfgs.toVector)
    graph.keySet filter (id => {
      val stmt = getStmt(id)
      lazy val defn = stmt.get.asInstanceOf[Def]

      stmt.isDefined &&
        stmt.get.isInstanceOf[Def] &&
        !isArray(defn.getLoc)
    }) foreach (id => {
      val defn = getStmt(id).get.asInstanceOf[Def]
      val defLoc = defn.getLoc
      val q = Queue() ++ graph(id)
      val vis = Set[StmtId]() ++ graph(id)

      System.err.println(s"\n\ntrying to find defs for ${defLoc}")

      while (q.nonEmpty) {
        val h = q.dequeue()
        h._1 match {
          case call: CFGMethodCall => {
            if (h._2 == -1) {
              call.params foreach ({
                case useLoc: Location => {
                  if (useLoc.field == defLoc.field) {
                    duChains += DefUseChain(id, h, defLoc, useLoc)
                  }
                }
                case _ =>
              })
            }
          }

          case block: CFGBlock => {
            if (h._2 >= 0) {
              val uses = getStmt(h).get.asInstanceOf[Use].getUse
              PrintCFG.prtStmt(getStmt(h).get)
              System.err.println(s"trying to find uses ${uses}")
              uses filter (defLoc.field == _.field) foreach (useLoc => {
                duChains += DefUseChain(id, h, defLoc, useLoc)
              })
            }
          }

          case cond: CFGConditional => {
            if (h._2 == -1) {
              cond.condition match {
                case useLoc: Location => {
                  if (useLoc.field == defLoc.field) {
                    duChains += DefUseChain(id, h, defLoc, useLoc)
                  }
                }
                case _ =>
              }
            }
          }

          case _ =>
        }
        val toAdd = {
          getStmt(h) match {
            case Some(ir) => {
              if (ir.isInstanceOf[Def] && ir.asInstanceOf[Def].getLoc.field == defLoc.field) {
                Vector()
              }
              else {
                graph(h) diff vis
              }
            }
            case None => graph(h) diff vis
          }
        }

        q ++= toAdd
        vis ++= toAdd
      }
    })


  }

  def testOutput(): Unit = {
    System.err.println(s"Duchain Count ${duChains.size}")
    duChains foreach( chain => {
      System.err.println(s"defPos:${(chain.DefPos,chain.DefLoc.line, chain.DefLoc.col)}, " +
        s"usePos:${(chain.UsePos,chain.UseLoc.line, chain.UseLoc.col)}")
//      System.err.println(s"defPos:${()}, " +
//              s"usePos:${()}")
    })
  }

  def apply(cfg: CFG): Unit = {
    if (cfgs.contains(cfg)) return
    cfgs += cfg

    cfg match {
      case program: CFGProgram => {
        duChains.clear()
        program.methods foreach (DUChainConstruct(_))
        cfgs.clear()
      }

      // we collect all blocks of a function.
      case method: CFGMethod => {
        if (method.block.isDefined) {
          cfgs.clear
          DUChainConstruct(method.block.get)
          findDuChain()
        }
      }

      case cond: CFGConditional => {
        if (cond.next.isDefined) {
          DUChainConstruct(cond.next.get)
        }
        if (cond.ifFalse.isDefined) {
          DUChainConstruct(cond.ifFalse.get)
        }
      }

      case other => {
        if (other.next.isDefined) {
          DUChainConstruct(other.next.get)
        }
      }
    }
  }
}
