package optimization

import codegen._
import ir.components._
import scala.collection.mutable.{ArrayBuffer, HashMap, Map, MultiMap, Set}

object Reset extends Optimization {

  val cfgs = Set[CFG]()

  private def init() {
    cfgs foreach (cfg => {
      cfg.resetOptmized(GlobalCP)
      cfg.resetOptmized(ConstantFolding)
    })
    cfgs.clear()
  }

  def apply(cfg: CFG): Unit = {
    if (cfgs.contains(cfg)) return
    cfgs += cfg

    cfg match {
      case program: CFGProgram => {
        program.methods foreach (Reset(_))
        init()
      }

      // we collect all blocks of a function.
      case method: CFGMethod => {
        if (method.block.isDefined) {
          Reset(method.block.get)
        }
      }

      case cond: CFGConditional => {
        if (cond.next.isDefined) {
          Reset(cond.next.get)
        }
        if (cond.ifFalse.isDefined) {
          Reset(cond.ifFalse.get)
        }
      }

      case other => {
        if (other.next.isDefined) {
          Reset(other.next.get)
        }
      }
    }
  }
}
