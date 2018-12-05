package optimization.reg_alloc

import ir.components.Location
import optimization.Labeling.StmtId

/**
  * StmtId is a tuple of (CFG, Int)
  * if CFG is CFGBLOCK, Int is the index of statement in block.statement
  * otherwise, Int is always -1. representing the condition of CFGConditional
  * or params of CFGMethodCall.
  * @param DefPos
  * @param UsePos
  * @param DefLoc
  * @param UseLoc
  */
case class DefUseChain(DefPos:StmtId,
                       UsePos:StmtId,
                       DefLoc:Location,
                       UseLoc:Location) {

}
