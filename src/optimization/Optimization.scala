package optimization

import codegen._

trait GenericOptimization

trait Optimization extends GenericOptimization {
  var isChanged: Boolean = false
  def init(): Unit = {
    resetChanged
  }
  def setChanged(): Unit = {
    isChanged = true
  }
  def resetChanged(): Unit = {
    isChanged = false
  }
  def apply(cfg: CFG, isInit: Boolean=true)
}