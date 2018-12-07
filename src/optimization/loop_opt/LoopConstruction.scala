package optimization.loop_opt

import codegen.{CFG, CFGConditional, CFGMethod, CFGProgram}
import optimization.Labeling
import optimization.Labeling.StmtId

import scala.collection.mutable.{ArrayBuffer, Map, Queue, Set}

case class LoopEntity[T](header: T,
                         exits: Set[T],
                         stmts: Set[T])

object LoopConstruction {
  val cfgs = ArrayBuffer[CFG]()
  var loops = Vector[LoopEntity[StmtId]]()
  val visited = Set[CFG]()
  var graph = Map[StmtId, Set[StmtId]]()
  var dom = Map[StmtId, Set[StmtId]]()
  var revGraph = Map[StmtId, Set[StmtId]]()
  var calls = Set[CFG]()

  def construct(cfgs: Vector[CFG]): Unit = {
    //System.err.println(s"total cfgs is ${cfgs.size}")
    val (calls, graph, revGraph) = Labeling(cfgs.toVector)
    val keySet = Set() ++= revGraph.keySet
    dom = Map[StmtId, Set[StmtId]]()
    val modified = Queue[StmtId]()

    for (key <- keySet) {
      if (revGraph(key).isEmpty) {
        dom(key) = Set(key)
      }
      else {
        dom(key) = keySet.clone()
        modified.enqueue(key)
      }
    }

    while (modified.nonEmpty) {
      val head = modified.dequeue()
      val oldHead = dom(head)
      val prev = revGraph(head) map (dom(_))
      //System.err.println(s"header ${head} has prevs of ${revGraph(head)}\n")
      // System.err.println(s"combo of header ${head}'s prevs is ${nxt}\n")
      dom(head) = Set(head) union (prev reduce (_ intersect _))
      if (oldHead != dom(head)) {
        modified ++= graph(head)
        // System.err.println(s"put ${head} into queue again")
      }
    }

    // for (stmt <- dom.keySet) {
    //   System.err.println(stmt)
    //   for (to <- dom(stmt)) {
    //     System.err.println(s"\t${to}")
    //   }
    // }

    val loops = ArrayBuffer[LoopEntity[StmtId]]()
    for (key <- keySet) {
      val backNodes = revGraph(key) filter (dom(_).contains(key))

      if (backNodes.nonEmpty) { // build up loops
        val header = key
        val stmts = Set[StmtId](key)
        val inBetween = Queue() ++= backNodes

        // System.err.println(s"we have a header node here ${header}, first backnode is ${backNodes.head}")

        while (inBetween.nonEmpty) {
          val head = inBetween.dequeue()
          stmts += head
          revGraph(head) foreach (prev =>
            if (!stmts.contains(prev)) {
              stmts += prev
              inBetween.enqueue(prev)
            })
        }

        val exits = stmts filter (graph(_) exists (!stmts.contains(_)))
        loops.append(LoopEntity(header, exits, stmts))
      }
    }
    //System.err.println(loops.size)
    this.loops = loops.toVector
    this.graph = graph
    this.revGraph = revGraph
    this.calls = calls
  }


  def apply(cfg: CFG): Unit = {
    if (visited.contains(cfg)) {
      return
    }
    visited.add(cfg)

    cfg match {
      case program: CFGProgram => {
        cfgs.clear()
        program.methods foreach (LoopConstruction(_))
        construct(cfgs.toVector)
        visited.clear()
      }

      // we collect all blocks of a function.
      case method: CFGMethod => {
        if (method.block.isDefined) {
          LoopConstruction(method.block.get)
        }
      }

      case cond: CFGConditional => {
        cfgs += cond
        if (cond.next.isDefined) {
          LoopConstruction(cond.next.get)
        }
        if (cond.ifFalse.isDefined) {
          LoopConstruction(cond.ifFalse.get)
        }
      }

      case other => {
        cfgs += other
        if (other.next.isDefined) {
          LoopConstruction(other.next.get)
        }
      }
    }
  }
}
