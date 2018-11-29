package optimization
import codegen._

/**
 * Repeat optimizations until reaching fixed-point
 * @param cfg the start node of CFG to optimize for
 * @param prequel a vector of optimizations that always run before condition
 * @param condition a vector of optimizations. a (prequel + condition + sequel) loop will run until all optimizations in condition report isChanged = false
 * @param sequel a vector of optimizations that always run after condition
 * @return number of iterations executed
 */

object RepeatOptimization {

  def apply(cfg: CFG, prequel: Option[Vector[Optimization]], condition: Vector[Optimization], sequel: Option[Vector[Optimization]]): Int = {
    var numIter: Int = 0
    var flagTerminate: Boolean = false

    while (!flagTerminate) {
      flagTerminate = true
      if (!prequel.isEmpty) {
        for (opt <- prequel.get) {
          // println(s"In Prequel: ${opt}")
          ResetOptimization(cfg, opt)
          opt(cfg)
        }
      }
      for (opt <- condition) {
        // println(s"In Condition: ${opt}")
        ResetOptimization(cfg, opt)
        opt(cfg)
        if (opt.isChanged) { flagTerminate = false }
      }
      if (!sequel.isEmpty) {
        for (opt <- sequel.get) {
          // println(s"In Sequel: ${opt}")
          ResetOptimization(cfg, opt)
          opt(cfg)
        }
      }
      numIter += 1
    }

    numIter
  }

}