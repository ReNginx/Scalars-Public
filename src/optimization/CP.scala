package optimization

import scala.collection.mutable.{ArrayBuffer, Map, Set}
import ir.components._
import codegen._

object CP extends Optimization {

  val localTmpSuffix = "_cse_local_tmp"
 
  def apply(cfg: CFG): Unit = {
    if (!cfg.isOptimized(CP)) {
      cfg.setOptimized(CP)
      cfg match {
        case virtualCFG: VirtualCFG => {
          if (!virtualCFG.next.isEmpty) {
            CP(virtualCFG.next.get)
          }
        }
  
        case block: CFGBlock => { // only optimize for block
          val newStatements: ArrayBuffer[IR] = ArrayBuffer[IR]()
          for (statement <- block.statements) {
            statement match {
              case assign: AssignStatement => { // only optimize for AssignStatement
                if (assign.loc.name.endsWith(localTmpSuffix)) { // t1 = a
                  block.tmp2Var += (assign.loc -> assign.value.asInstanceOf[SingleExpr]) // add t1 -> a
                  if (block.var2Set.contains(assign.value.asInstanceOf[SingleExpr])) { // add a -> {t1}
                    block.var2Set(assign.value.asInstanceOf[SingleExpr]) += assign.loc
                  } else {
                    block.var2Set += (assign.value.asInstanceOf[SingleExpr] -> Set[Location](assign.loc))
                  }
                  newStatements += statement
                } else {
                  assign.value match {
                    case rhsLoc: Location => {
                      if (rhsLoc.name.endsWith(localTmpSuffix)) { // c = t1
                        if (block.tmp2Var.contains(rhsLoc)) { // c = t1 => c = a
                          val replaceStatement: AssignStatement = AssignStatement(assign.line,  assign.col, assign.loc, block.tmp2Var(rhsLoc))
                          newStatements += replaceStatement
                        }
                        if (block.var2Set.contains(assign.loc)) { // remove c from tmp2Var and var2Set
                          val lhsSet: Set[Location] = block.var2Set(assign.loc)
                          lhsSet foreach { block.tmp2Var.remove(_) }
                          block.var2Set.remove(assign.loc)
                        }
                      } else {
                        newStatements += statement
                      }
                    }
                    case _ => {
                      newStatements += statement
                    }
                  }
                  if (block.var2Set.contains(assign.loc)) { // remove c from tmp2Var and var2Set
                    val lhsSet: Set[Location] = block.var2Set(assign.loc)
                    lhsSet foreach { block.tmp2Var.remove(_) }
                    block.var2Set.remove(assign.loc)
                  }
                }
              }
              case _ => {
                newStatements += statement
              }
            }
          }
          block.statements.clear
          block.statements ++= newStatements

          if (!block.next.isEmpty) {
            CP(block.next.get)
          }
        }
  
        case conditional: CFGConditional => {
          if (!conditional.next.isEmpty) {
            CP(conditional.next.get)
          }
          if (!conditional.ifFalse.isEmpty) {
            CP(conditional.ifFalse.get)
          }
          if (!conditional.end.isEmpty) {
            CP(conditional.end.get)
          }
        }

        case method: CFGMethod => {
          if (!method.block.isEmpty) {
            CP(method.block.get)
          }
        }

        case call: CFGMethodCall => {
          if (!call.next.isEmpty) {
            CP(call.next.get)
          }
        }

        case program: CFGProgram => {
          program.methods foreach { CP(_) }
        }
      }
    }
  }

}
