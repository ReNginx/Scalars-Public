//package optimization
//
//import codegen._
//
//import scala.collection.mutable.Set
//
//object GlobalDCE extends Optimization {
//  val cfgs = Set[CFG]()
//  val unsed = Set[(CFGBlock, Int)]()
//
//  def eliminateUnsed: Unit = {
//
//  }
//
//  def apply(cfg: CFG): Unit = {
//    if (cfg.isOptimized(GlobalDCE)) {
//      return
//    }
//    cfg.setOptimized(GlobalDCE)
//    cfgs += cfg
//
//    cfg match {
//      case program: CFGProgram => {
//        program.methods foreach (GlobalDCE(_))
//      }
//
//      // we collect all blocks of a function.
//      case method: CFGMethod => {
//        if (method.block.isDefined) {
//          cfgs.clear
//          unsed.clear
//          GlobalDCE(method.block.get)
//          eliminateUnsed
//        }
//      }
//
//      case cond: CFGConditional => {
//        if (cond.next.isDefined) {
//          GlobalDCE(cond.next.get)
//        }
//        if (cond.ifFalse.isDefined) {
//          GlobalDCE(cond.ifFalse.get)
//        }
//      }
//
//      case other => {
//        if (other.next.isDefined) {
//          GlobalDCE(other.next.get)
//        }
//      }
//    }
//  }
//}
