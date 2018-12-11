package optimization.reg_alloc

import codegen.CFG
import ir.components.Location
import optimization.Labeling.StmtId
import scala.collection.mutable.Set
/**
  * StmtId is a tuple of (CFG, Int)
  * if CFG is CFGBLOCK, Int is the index of statement in block.statement
  * otherwise, Int is always -1. representing the condition of CFGConditional
  * or params of CFGMethodCall.
  * @param defPos
  * @param usePos
  * @param defLoc
  * @param useLoc
  */
case class DefUseChain(defPos:StmtId,
                       usePos:StmtId,
                       defLoc:Location,
                       useLoc:Location) {
  assert(defLoc.field.get == useLoc.field.get) // must be the same variable
  var convexSet = Set[StmtId]()
  var functionCalls = Set[CFG]()
  var defDepth = 0
  var useDepth = 0

  def getVarDec() = defLoc.field.get

  def getCalls() = functionCalls

  def getDepth() = defDepth

  def getConvex() = convexSet

  def isOverlap(defUseChain: DefUseChain): Boolean = {
    val overlapSet = convexSet intersect defUseChain.getConvex()
    return overlapSet.nonEmpty
    // val defAndUse = Set(defPos, usePos, defUseChain.defPos, defUseChain.usePos)
    // if ((overlapSet intersect defAndUse).nonEmpty) return true
    // if (defPos == defUseChain.usePos) return true
    // if (usePos == defUseChain.defPos) return true
    false
  }

  override def toString: String = {
    val sep = "\n===================================="
    val DefUse = f"defpos:${defPos}, usepos:${usePos}\n"
    val Loc = f"defloc:${defLoc}, useloc:${useLoc}"
    val calls = if (functionCalls.nonEmpty) functionCalls map (_.toString) reduce (_ + "\n" + _) else ""
    val convex = if (convexSet.nonEmpty) convexSet map (_.toString) reduce (_ + "\n" + _) else ""
    val dep = "defDepth:" + defDepth.toString + ", useDepth:" + useDepth
    sep + "BEGIN\n" + DefUse + Loc + sep +  "CALLS\n"  + calls + sep +
      "CONVEX\n"  + convex + sep +  "DEP\n" + dep + sep +  "END\n"
  }
}
