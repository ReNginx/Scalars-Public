package optimization

import codegen.{CFG, CFGBlock, CFGMethodCall}

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

  def link(cfgLst: Vector[CFG]): (Set[CFG], Graph) = {
    val map = Set[StmtId]()
    val calls = Set[CFG]()
    val lnk = mutable.Map[StmtId, Set[StmtId]]()
    val revLnk = mutable.Map[StmtId, Set[StmtId]]()

    def setHelper(id: StmtId) {
      if (!map.contains(id)) {
        map += id
        lnk(id) = Set[StmtId]()
      }
    }

    def setLnk(from: StmtId, to: StmtId) {
      setHelper(from)
      setHelper(to)
      lnk(from) += to
      revLnk(to) += from
    }

    def beg(cfg: CFG) = (cfg, -1)
    def end(cfg: CFG) = (cfg, -2)

    for (cfg <- cfgLst) {
      cfg match {
        case block: CFGBlock => {
          var last = beg(block)

          setHelper(beg(block))
          setHelper(end(block))

          for (i <- block.statements.indices) {
            setLnk((block, i), (block, i - 1))
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
    (calls, lnk)
  }

  def apply(cfgLst: Vector[CFG]): (Set[CFG], Graph) = {
    val (callCFG, graph) = link(cfgLst)
    (callCFG, graph)
  }
}
