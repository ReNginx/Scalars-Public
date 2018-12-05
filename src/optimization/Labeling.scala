package optimization

import codegen.{CFG, CFGBlock, CFGMethodCall}
import ir.components.IR

import scala.collection.mutable
import scala.collection.mutable.Map
import scala.collection.mutable.Set

object Labeling {

  type StmtId = (CFG, Int)
  type Graph = Map[StmtId, Set[StmtId]]

  /**
    * labeling rule: start of a block, index = -1, end of a block, index = -2.
    *
    * @param cfgLst
    * @return
    */
  def getStmt(pos: StmtId): Option[IR] = {
    if (pos._2 >= 0) {
      // if (pos._2 >= pos._1.asInstanceOf[CFGBlock].statements.size) {
      //   System.err.println("start")
      //   pos._1.asInstanceOf[CFGBlock].statements foreach (PrintCFG.prtStmt(_))
      //   System.err.println("over")
      //   assert(false)
      // }
      Option(pos._1.asInstanceOf[CFGBlock].statements(pos._2))
    }
    else
      None
  }

  def link(cfgLst: Vector[CFG]): (Set[CFG], Graph, Graph) = {
    val map = Set[StmtId]()
    val calls = Set[CFG]()
    val lnk = mutable.Map[StmtId, Set[StmtId]]()
    val revLnk = mutable.Map[StmtId, Set[StmtId]]()

    def lnkInit(id: StmtId) {
      if (!map.contains(id)) {
        map += id
        lnk(id) = Set[StmtId]()
        revLnk(id) = Set[StmtId]()
      }
    }

    def setLnk(from: StmtId, to: StmtId) {
      lnkInit(from)
      lnkInit(to)
      lnk(from) += to
      revLnk(to) += from
    }

    def beg(cfg: CFG) = (cfg, -1)
    def end(cfg: CFG) = (cfg, -2)

    for (cfg <- cfgLst) {
      cfg match {
        case block: CFGBlock => {
          var last = beg(block)

          lnkInit(beg(block))
          lnkInit(end(block))

          for (i <- block.statements.indices) {
            setLnk((block, i - 1), (block, i))
            last = (block, i)
          }

          setLnk(last, end(block))
          if (block.next.isDefined)
            setLnk(end(block), beg(block.next.get))
        }

        case call: CFGMethodCall => {
          calls += call
          setLnk(beg(call), end(call))
          if (call.next.isDefined)
            setLnk(end(call), beg(call.next.get))
        }

        case otherCfg => {
          setLnk(beg(otherCfg), end(otherCfg))
          for (nxt <- WorkList.succ(otherCfg))
            setLnk(end(otherCfg), beg(nxt))
        }
      }
    }
    (calls, lnk, revLnk)
  }

  def apply(cfgLst: Vector[CFG]): (Set[CFG], Graph, Graph) = {
    val (callCFG, graph, revGraph) = link(cfgLst)
    (callCFG, graph, revGraph)
  }
}
