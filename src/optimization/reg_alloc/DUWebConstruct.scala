package optimization.reg_alloc

import ir.components._
import scala.collection.mutable.{Map, Set, ArrayBuffer}
import scala.util.control.Breaks._
import codegen._

object DUWebConstruct {
  val duWebSet = Set[DefUseWeb]()
  val webCandMap = Map[FieldDeclaration, ArrayBuffer[Set[DefUseChain]]]()
  val regSaveAtCall = Map[CFG, Set[Register]]()
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
      webCandMap += (chain.getVarDec -> ArrayBuffer[Set[DefUseChain]](Set[DefUseChain](chain)))
    }
  }

  private def consolidate(): Unit = {
    for (dec <- webCandMap.keys) {
      val newBuffer = ArrayBuffer[Set[DefUseChain]]()
      var isChanged: Boolean = true
      while (isChanged) {
        isChanged = false
        newBuffer.clear
        for (web <- webCandMap(dec)) { // web is Set[DefUseChain]
          breakable {
            for (comp <- newBuffer) {
              if (isSetSetOverlap(web, comp)) { // if can be merged into comp
                comp ++= web
                isChanged = true
                break
              }
            }
            newBuffer += web
          }
        }
        webCandMap(dec).clear
        webCandMap(dec) ++= newBuffer
      }
    }
  }

  private def genWeb(): Unit = {
    for (dec <- webCandMap.keys) {
      for (i <- 0 to webCandMap(dec).length - 1) {
        duWebSet += DefUseWeb(dec, webCandMap(dec)(i), i)
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

  private def isSetSetOverlap(set1: Set[DefUseChain], set2: Set[DefUseChain]): Boolean = {
    for (c <- set1) {
      if (isSetChainOverlap(set2, c)) { return true }
    }
    false
  }

  def apply(duChainSet: Set[DefUseChain]): Unit = {
    for (chain <- duChainSet) {
      addChain2Web(chain)
    }
    consolidate()
    genWeb()
    genInterfereSet()
  }

  def testOutput(): Unit = {
    for (web <- duWebSet) {
      System.err.println(web)
    }
  }

  def assignRegs(): Unit = {
    duWebSet foreach(_.assignRegs())
    duWebSet foreach(duw => {
      if (duw.register.isEmpty) return
      duw.getCalls foreach (call => {
        if (!regSaveAtCall.contains(call)) {
          regSaveAtCall(call) = Set()
        }
        regSaveAtCall(call) += duw.register.get
      })
    })
  }
}
