package optimization

import codegen._
import ir.components.{Def, Expression, Location}
import optimization.GlobalCP.DefId
import optimization.Labeling._

import scala.collection.mutable.{Map, Queue, Set}
import scala.util.control.Breaks._

object JudgeSubstitution {
  var method:Option[CFGMethod] = None

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
    val (_, _, revGraph) = Labeling(cfgLst)
    graph = revGraph
  }

  def apply(from: Set[DefId],
            pos: (CFG, Int),
            loc: Location,
            res: Expression): Boolean = {
    if (loc.field.get.isReg)
      return false
    if (method.isDefined && method.get.params.contains(loc.field.get))
      return false

    val map = Map[StmtId, Boolean]()
    val visited = Set[StmtId]()
    val q = Queue[StmtId](pos)

    map(pos) = true
    visited += pos

    //assert(loc.field.get.isGlobal)
    // for (key <- graph.keySet) {
    //   println(s"from ${key}")
    //   for (to <- graph(key)) {
    //     println(s"\t to ${to}")
    //   }
    // }

    while (q.nonEmpty) {
      val head = q.dequeue
      //System.err.println(s"node from queue ${head}, value is ${map(head)}")
      for (nxt <- graph(head)) {
        breakable {
          if (!map.contains(nxt)) {
            map(nxt) = true
          }

          map(nxt) = map(nxt) && map(head)

          nxt._1 match {
            case call: CFGMethodCall => {
              //System.err.println(s"${loc}")
              if (loc.field.get.isGlobal)
                map(nxt) = false
              res match {
                case isLoc: Location => {
                  if (isLoc.field.get.isGlobal)
                    map(nxt) = false
                }

                case _ =>
              }
            }

            case block: CFGBlock => {
              if (nxt._2 >= 0) {
                block.statements(nxt._2) match {
                  case defn: Def => {
                    if (from.contains((block, nxt._2))) {
                      if (!map(nxt))
                        return false
                      break
                    }
                    if (defn.getLoc == loc)
                      map(nxt) = false
                  }
                  case _ =>
                }
              }
            }

            case _ =>
          }

          if (!visited.contains(nxt)) {
            // println(nxt)
            // if (nxt._1.label == "_24_r11_c3_call_call")
            //   assert(nxt._2 < 0)
            q.enqueue(nxt)
            visited += nxt
          }
        }
      }
    }

    true
  }
}
