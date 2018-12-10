package optimization.reg_alloc

import ir.components._
import scala.collection.mutable.{Map, Set}

object DUWebConstruct {
  val duWebSet = Set[DefUseWeb]()
  val webCandMap = Map[FieldDeclaration, Set[Set[DefUseChain]]]()

  /**
   * Add chain to an existing Set in webCandSet.
   * If no suitable Set is found, create a new Set
   * Selection criteria:
   * If a set (targSet) of the same field declaration is found:
   * - If targSet and chain overlap, then add chain to targSet
   * - If they don't overlap, create new Set for chain
   *   and add it under the same field declaration
   * Else, create a new MultiMap entry from the field declaration
   *   of chain to Set(chain)
   */
  private def addChain2Web(chain: DefUseChain): Unit = {
    if (webCandMap.contains(chain.getVarDec)) {
      for (set <- webCandMap(chain.getVarDec)) {
        if (isSetChainOverlap(set, chain)) {
          set += chain
          return
        }
      }
      webCandMap(chain.getVarDec) += Set[DefUseChain](chain)
    } else {
      webCandMap += (chain.getVarDec -> Set[Set[DefUseChain]](Set[DefUseChain](chain)))
    }
  }

  private def genWeb(): Unit = {
    for (dec <- webCandMap.keys) {
      for (set <- webCandMap(dec)) {
        duWebSet += DefUseWeb(dec, set)
      }
    }
  }

  private def genInterfereSet(): Unit = {
    for (web <- duWebSet) {
      for (targWeb <- duWebSet) {
        if (targWeb != web) {
          if (web.isOverlap(targWeb)) {
            web.interfereSet += targWeb
          }
        }
      }
    }
  }

  private def isSetChainOverlap(set: Set[DefUseChain], chain: DefUseChain): Boolean = {
    for (c <- set) {
      if (chain.isOverlap(c)) { return true }
    }
    false
  }

  def apply(duChainSet: Set[DefUseChain]): Unit = {
    for (chain <- duChainSet) {
      addChain2Web(chain)
    }
    genWeb()
    genInterfereSet()
  }

  def testOutput(): Unit = {
    for (web <- duWebSet) {
      System.err.println(web)
    }
  }
}