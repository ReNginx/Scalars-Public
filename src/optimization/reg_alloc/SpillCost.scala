package optimization.reg_alloc

object SpillCost {
  val LOOP_SPILL_COST = 10

  def getDuchains(duweb: DefUseWeb): Set[DefUseChain] = ???

  def apply(duweb: DefUseWeb): Int = {
    val duchains: Set[DefUseChain] = getDuchains(duweb)
    val defSet = Set.empty ++ duchains map (du => (du.defPos, du.defDepth))
    val useSet = Set.empty ++ duchains map (du => (du.usePos, du.useDepth))

    (defSet map (d => Math.pow(LOOP_SPILL_COST, d._2))).sum.toInt +
      (useSet map (u => Math.pow(LOOP_SPILL_COST, u._2))).sum.toInt
  }
}
