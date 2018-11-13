package optimization

import codegen._
import ir.components.{Def, Expression, Location}
import optimization.GlobalCP.DefId
import optimization.Labeling._

import scala.collection.mutable.{Map, Queue, Set}

object JudgeSubstitution {
  var graph: Map[StmtId, Set[StmtId]] = Map()

  /**
    * pos is the position of res.
    * we try to substitute loc with res, where res is not identical to loc
    * to judge such replacement is valid. any path from pos to all from should not
    * contain modification of loc.
    *
    * @param from
    * @param pos
    * @param res
    * @return
    */

  def init(cfgLst: Vector[CFG]): Unit = {
    val (_, newGraph) = Labeling(cfgLst)
    graph = newGraph
  }

  def apply(from: Set[DefId],
            pos: (CFG, Int),
            loc: Location,
            res: Expression): Boolean = {

    val map = Map[StmtId, Boolean]()
    val visitied = Set[StmtId]()
    val q = Queue[StmtId](pos)

    map(pos) = true
    visitied += pos

    while (q.nonEmpty) {
      val head = q.dequeue

      for (nxt <- graph(head)) {
        if (!map.contains(nxt)) {
          map(nxt) = true
        }

        map(nxt) = map(nxt) & map(head)

        nxt._1 match {
          case call: CFGMethodCall => {
            if (loc.field.get.isGlobal)
              map(nxt) = false
          }

          case block: CFGBlock => {
            if (nxt._2 >= 0) {
              block.statements(nxt._2) match {
                case defn: Def => {
                  if (defn.getLoc == loc)
                    map(nxt) = false
                  if (from.contains((block, nxt._2)) && !map(nxt))
                    return false
                }
                case _ =>
              }
            }
          }

          case _ =>
        }

        if (!visitied.contains(nxt)) {
          q.enqueue(nxt)
          visitied += nxt
        }
      }
    }

    true
  }
}
