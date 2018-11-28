package optimization.loop_opt

import codegen.{CFG, CFGConditional, CFGMethod, CFGProgram}
import optimization.Labeling.StmtId
import optimization.{Labeling, Optimization}
import scala.collection.mutable.Queue
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import scala.collection.mutable.ArrayBuffer

case class LoopEntity[T](header: T,
                        exits: Set[T],
                        stmts: Set[T])

object LoopConstruction {
 val cfgs = ArrayBuffer[CFG]()
 var loops = Vector[LoopEntity[StmtId]]()
 val visited = Set[CFG]()

 def construct(): Unit = {
   val (_, graph, revGraph) = Labeling(cfgs.toVector)
   val keySet = Set() ++= revGraph.keySet
   val dom = Map[StmtId, Set[StmtId]]()
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
     val nxt = revGraph(head) map (dom(_))
     // System.err.println(s"header ${head} has prevs of ${revGraph(head)}\n")
     // System.err.println(s"combo of header ${head}'s prevs is ${nxt}\n")
     dom(head) = Set(head) union (nxt reduce (_ intersect _))
     if (oldHead != dom(head)) {
       modified ++= graph(head)
       //System.err.println(s"put ${head} into queue again")
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

       //System.err.println(s"we have a header node here ${header}, first backnode is ${backNodes.head}")

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
       construct()
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
