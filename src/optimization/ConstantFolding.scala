//package optimization
//
//import codegen.{CFG, CFGConditional, CFGMethod, CFGProgram}
//import ir.components._
//
//object ConstantFolding extends Optimization {
//
//  def apply(cfg: CFG): Unit = {
//    if (cfg.isOptimized(ConstantFolding)) {
//      return
//    }
//    cfg.setOptimized(ConstantFolding)
//
//
//    cfg match {
//      case program: CFGProgram => {
//        program.methods foreach (ConstantFolding(_))
//      }
//
//      // we collect all blocks of a function.
//      case method: CFGMethod => {
//        if (method.block.isDefined) {
//          ConstantFolding(method.block.get)
//        }
//      }
//
//      case cond: CFGConditional => {
//        if (cond.next.isDefined) {
//          ConstantFolding(cond.next.get)
//        }
//        if (cond.ifFalse.isDefined) {
//          ConstantFolding(cond.ifFalse.get)
//        }
//      }
//
//      case block: Block => {
//        for (stmt <- block.statements) {
//          stmt match {
//            case assign: AssignStatement => {
//
//            }
//            case compoundAsg: CompoundAssignStatement => {
//
//            }
//            case inc: Increment => {
//
//            }
//            case dec: Decrement => {
//
//            }
//
//            case unary: UnaryOperation => {
//              unary.expression match {
//                case IntLiteral =>
//                case _ =>
//              }
//            }
//
//            case binary: BinaryOperation => {
//
//            }
//
//            case _ =>
//          }
//        }
//        if (block.next.isDefined) {
//          ConstantFolding(block.next.get)
//        }
//      }
//
//      case other => {
//        if (other.next.isDefined) {
//          ConstantFolding(other.next.get)
//        }
//      }
//    }
//  }
//}
