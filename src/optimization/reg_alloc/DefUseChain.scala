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
  * @param DefPos
  * @param UsePos
  * @param DefLoc
  * @param UseLoc
  */
case class DefUseChain(DefPos:StmtId,
                       UsePos:StmtId,
                       DefLoc:Location,
                       UseLoc:Location) {
  var convexSet = Set[StmtId]()
  var functionCalls = Set[CFG]()
  var depth = 0

  def getCalls() = functionCalls

  def getDepth() = depth

  def getConvex() = convexSet

  def isOverlap(defUseChain: DefUseChain): Boolean = {
    val overlapSet = convexSet intersect defUseChain.getConvex()
    val defAndUse = Set(DefPos, UsePos, defUseChain.DefPos, defUseChain.UsePos)
    if ((overlapSet diff defAndUse).nonEmpty) return true
    if (DefPos == defUseChain.UsePos) return true
    if (UsePos == defUseChain.DefPos) return true
    false
  }

  override def toString: String = {
    val sep = "\n===================================="
    val DefUse = f"defpos:${DefPos}, usepos:${UsePos}\n"
    val Loc = f"defloc:${DefLoc}, useloc:${UseLoc}"
    val calls = if (functionCalls.nonEmpty) functionCalls map (_.toString) reduce (_ + "\n" + _) else ""
    val convex = if (convexSet.nonEmpty) convexSet map (_.toString) reduce (_ + "\n" + _) else ""
    val dep = depth.toString
    sep + "BEGIN\n" + DefUse + Loc + sep +  "CALLS\n"  + calls + sep +
      "CONVEX\n"  + convex + sep +  "DEP\n" + dep + sep +  "END\n"
  }
}
