package optimization.reg_alloc

import ir.components._
import codegen._
import optimization.Labeling.StmtId

import scala.collection.mutable.Set

case class DefUseWeb(
      varDec: FieldDeclaration,
      duChainSet: Set[DefUseChain],
      sig: Int
    ) {

  val loopSpillMultiplier: Int = 10
  var convexSet: Option[Set[StmtId]] = None

  // Constructed via WebGraphColoring
  var register: Option[Register] = None
  var isSpill: Boolean = false

  // Constructed via DUWebConstruct
  var interfereSet =  Set[DefUseWeb]()

  // An instance of DefUseWeb should be identified by its defs and uses, rather by register or isSpill
  override def hashCode: Int = varDec.hashCode + duChainSet.hashCode + sig.hashCode
  override def equals(obj: Any): Boolean = {
    obj.isInstanceOf[DefUseWeb] && obj.hashCode == this.hashCode
  }

  def getVarDec() = varDec

  def getSignature(): String = getVarDec.toString + " - " + sig.toString

  // convexSet is initialized when getConvex is called for the first time.
  def getConvex(): Set[StmtId] = {
    if (convexSet.isDefined) {
      convexSet.get
    } else {
      var workSet = Set[StmtId]()
      for (du <- duChainSet) {
        workSet = workSet.union(du.getConvex())
      }
      convexSet = Option(workSet)
      convexSet.get
    }
  }

  def getCalls(): Set[CFG] = {
    var retSet = Set[CFG]()
    for (chain <- duChainSet) {
      retSet = retSet.union(chain.getCalls)
    }
    retSet
  }

  def spillCost(): Int = {
    val defSet = Set.empty ++ duChainSet map (du => (du.defPos, du.defDepth))
    val useSet = Set.empty ++ duChainSet map (du => (du.usePos, du.useDepth))

    (defSet map (d => Math.pow(loopSpillMultiplier, d._2))).sum.toInt +
      (useSet map (u => Math.pow(loopSpillMultiplier, u._2))).sum.toInt
  }

  def isOverlap(web: DefUseWeb): Boolean = {
    for (local <- this.duChainSet) {
      for (remote <- web.duChainSet) {
        if (local.isOverlap(remote)) {
          return true
        }
      }
    }
    false
  }

  def assignRegs(): Unit = {
    if (register.isEmpty) return
    duChainSet foreach(duc => {
      duc.defLoc.reg = register
      duc.useLoc.reg = register
    })
  }

  def interfereWith() = interfereSet

  def degree(): Int = {
    interfereWith.size
  }

  override def toString: String = {
    val hdrStr = "DefUseWeb: " + getSignature + "\n"
    val decSrt = "- Declaration: " + varDec + "\n"
    val spillCostStr = "- Spill Cost: " + spillCost + "\n"
    val isSpillStr = "- Spilled?: " + isSpill + "\n"
    val regStr = if (!isSpill) "- Register: " + register.get + "\n" else ""
    val callsStr = if (getCalls.nonEmpty) "- Method Calls:\n" + (getCalls map (_.toString) reduce (_ + "\n" + _)) + "\n" else ""
    val convexStr = if (getConvex.nonEmpty) "- Convex Hull:\n" + (getConvex map (_.toString) reduce (_ + "\n" + _)) + "\n" else ""
    val confStr = if (interfereWith.nonEmpty) "- Interferes With:\n" + (interfereWith map (_.getSignature) reduce (_ + "\n" + _)) + "\n" else ""
    hdrStr + decSrt + spillCostStr + isSpillStr + regStr + callsStr + convexStr + confStr
  }
}