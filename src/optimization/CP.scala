package optimization

import scala.collection.mutable.{ArrayBuffer, Map, Set}
import ir.components._
import codegen._

object CP extends Optimization {

  val localTmpSuffix = "_cse_local_tmp"
  val tmp2Var: Map[Location, SingleExpr] = Map[Location, SingleExpr]()
  val var2Set: Map[SingleExpr, Set[Location]] = Map[SingleExpr, Set[Location]]()
 
  def apply(cfg: CFG, isInit: Boolean=true): Unit = {
    if (isInit) { init }
    tmp2Var.clear
    var2Set.clear

    if (!cfg.isOptimized(CP)) {
      cfg.setOptimized(CP)
      cfg match {
        case virtualCFG: VirtualCFG => {
          if (!virtualCFG.next.isEmpty) {
            CP(virtualCFG.next.get, false)
          }
        }
  
        case block: CFGBlock => { // only optimize for block
          val newStatements: ArrayBuffer[IR] = ArrayBuffer[IR]()
          for (statement <- block.statements) {
            statement match {
              case assign: AssignStatement => { // only optimize for AssignStatement
                if (assign.loc.name.endsWith(localTmpSuffix)) { // t1 = a
                  tmp2Var += (assign.loc -> assign.value.asInstanceOf[SingleExpr]) // add t1 -> a
                  if (var2Set.contains(assign.value.asInstanceOf[SingleExpr])) { // add a -> {t1}
                    var2Set(assign.value.asInstanceOf[SingleExpr]) += assign.loc
                  } else {
                    var2Set += (assign.value.asInstanceOf[SingleExpr] -> Set[Location](assign.loc))
                  }
                  newStatements += statement
                } else {
                  assign.value match {
                    case rhsLoc: Location => {
                      if (rhsLoc.name.endsWith(localTmpSuffix)) { // c = t1
                        isChanged = true // changed
                        if (tmp2Var.contains(rhsLoc)) { // c = t1 => c = a
                          val replaceStatement: AssignStatement = AssignStatement(assign.line,  assign.col, assign.loc, tmp2Var(rhsLoc))
                          newStatements += replaceStatement
                        } else {
                          throw new NotImplementedError // should be impossible
                        }
                        if (var2Set.contains(assign.loc)) { // remove c from tmp2Var and var2Set
                          val lhsSet: Set[Location] = var2Set(assign.loc)
                          lhsSet foreach { tmp2Var.remove(_) }
                          var2Set.remove(assign.loc)
                        }
                      } else {
                        newStatements += statement
                      }
                    }
                    case _ => {
                      newStatements += statement
                    }
                  }
                  if (var2Set.contains(assign.loc)) { // remove c from tmp2Var and var2Set
                    val lhsSet: Set[Location] = var2Set(assign.loc)
                    lhsSet foreach { tmp2Var.remove(_) }
                    var2Set.remove(assign.loc)
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
            CP(block.next.get, false)
          }
        }
  
        case conditional: CFGConditional => {
          if (!conditional.next.isEmpty) {
            CP(conditional.next.get, false)
          }
          if (!conditional.ifFalse.isEmpty) {
            CP(conditional.ifFalse.get, false)
          }
          if (!conditional.end.isEmpty) {
            CP(conditional.end.get, false)
          }
        }

        case method: CFGMethod => {
          if (!method.block.isEmpty) {
            CP(method.block.get, false)
          }
        }

        case call: CFGMethodCall => {
          if (!call.next.isEmpty) {
            CP(call.next.get, false)
          }
        }

        case program: CFGProgram => {
          program.methods foreach { CP(_, false) }
        }
      }
    }
  }

}
