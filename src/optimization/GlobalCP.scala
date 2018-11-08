//package optimization
//
//import codegen._
//import ir.components._
//
//import scala.collection.mutable
//import scala.collection.mutable.{ArrayBuffer, Map}
//
//object GlobalCP extends Optimization {
//  val gen = Map[CFG, Set[Expression]]()
//  val kill = Map[CFG, Set[Expression]]()
//  val allCfg = ArrayBuffer[CFG]()
//
//
//  def add(locMap: mutable.Map[Location, Set[IR]], loc: Location, stmt: IR): Unit = {
//
//  }
//
//  def mainProcedure: Unit = {
//    val locMap = Map[Location, Set[IR]]()
//    for (cfg <- allCfg) { // we only have virtual, cond, block here.
//      cfg match {
//        case block: CFGBlock => {
//         val hasLoc = Set[Location]()
//          for (stmt <- block.statements.reverse) {
//            stmt match {
//              case assign: Assignment => {
//                add(locMap, assign.loc, assign)
//              }
//
//              case oper: Operation => {
//                add(locMap, oper.eval.get, oper)
//              }
//
//              case ret: Return => {
//
//              }
//            }
//          }
//        }
//
//        case other => {
//          gen(other) = Set()
//          kill(other) = Set()
//        }
//      }
//    }
//  }
//
//  def apply(cfg: CFG): Unit = {
//    if (cfg.isOptimized(GlobalCP)) {
//      return
//    }
//    cfg.setOptimized(GlobalCP)
//    allCfg += cfg
//
//    cfg match {
//      case program: CFGProgram => {
//        program.methods foreach( GlobalCP(_))
//      }
//
//      case method: CFGMethod => { // cp is applied per method.
//        if (method.block.isDefined) {
//          gen.clear
//          kill.clear
//          allCfg.clear
//          GlobalCP(method.block.get)
//          mainProcedure
//        }
//      }
//
//      case cond: CFGConditional => {
//        if (cond.next.isDefined) {
//          GlobalCP(cond.next.get)
//        }
//        if (cond.ifFalse.isDefined) {
//          GlobalCP(cond.ifFalse.get)
//        }
//      }
//
//      case other => {
//        if (other.next.isDefined) {
//          GlobalCP(other.next.get)
//        }
//      }
//    }
//  }
//}
