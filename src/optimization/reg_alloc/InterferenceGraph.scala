package optimization.reg_alloc

import scala.collection.mutable.Set

case class InterferenceGraph(webSet: scala.collection.immutable.Set[DefUseWeb]) {
  val spillCostVec: Vector[DefUseWeb] = webSet.toVector.sortWith(_.spillCost < _.spillCost)
  val spillSet = Set[DefUseWeb]()
  
}
