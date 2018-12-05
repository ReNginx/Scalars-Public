package optimization.reg_alloc

import ir.components._

import scala.collection.mutable.Set

case class DefUseWeb(
      var register: Option[Register] = None,
      var isSpill: Boolean = false
    ) {
  // An instance of DefUseWeb should be identified by its defs and uses, rather by register or isSpill (TODO: Implement new hash)
  def spillCost(): Int = {
    0
  }
  def interfereWith(): Set[DefUseWeb] = {
    Set[DefUseWeb]()
  }
  def degree(): Int = {
    interfereWith.size
  }
}