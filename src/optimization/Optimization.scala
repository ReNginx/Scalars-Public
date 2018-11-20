package optimization

import codegen._

trait Optimization {
  var isChanged: Boolean = false
  def init(): Unit = {
    isChanged = false
  }
  def apply(cfg: CFG, isInit: Boolean=true)
}