package optimization

import codegen._

// Given an optimization opt, reset all CFG blocks for opt
object ResetOptimization extends Optimization {

  def apply(cfg: CFG, opt: Optimization): Unit = {
    if (cfg.isOptimized(opt)) {
      cfg.resetOptimized(opt)
      cfg match {
        case virtualCFG: VirtualCFG => {
          if (!virtualCFG.next.isEmpty) {
            ResetOptimization(virtualCFG.next.get, opt)
          }
        }
  
        case block: CFGBlock => {
          if (!block.next.isEmpty) {
            ResetOptimization(block.next.get, opt)
          }
        }
  
        case conditional: CFGConditional => {
          if (!conditional.next.isEmpty) {
            ResetOptimization(conditional.next.get, opt)
          }
          if (!conditional.ifFalse.isEmpty) {
            ResetOptimization(conditional.ifFalse.get, opt)
          }
          if (!conditional.end.isEmpty) {
            ResetOptimization(conditional.end.get, opt)
          }
        }

        case method: CFGMethod => {
          if (!method.block.isEmpty) {
            ResetOptimization(method.block.get, opt)
          }
        }

        case call: CFGMethodCall => {
          if (!call.next.isEmpty) {
            ResetOptimization(call.next.get, opt)
          }
        }

        case program: CFGProgram => {
          program.methods foreach { ResetOptimization(_, opt) }
        }
      }
    }
  }

}
