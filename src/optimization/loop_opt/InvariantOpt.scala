//package optimization.loop_opt
//
//import codegen.CFG
//import optimization.Optimization
//
///**
//  *
//  */
//
//object InvariantOpt extends Optimization{
//  def apply(cfg: CFG, isInit: Boolean = true): Unit = {
//    if (isInit) { init() }
//    if (cfg.isOptimized(InvariantOpt)) { return }
//    cfg.setOptimized(InvariantOpt)
//
//    LoopConstruction(cfg)
//    val loops = LoopConstruction.loops
//
//
//  }
//}
