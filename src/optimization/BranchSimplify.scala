package optimization
import ir.components._
import codegen._

object BranchSimplify extends Optimization {
  def adjustPar(par: CFG, curr: CFGConditional, nxt: Option[CFG])  {

    par match {
      case cond: CFGConditional => {
        if (curr == cond.next.get) {
          cond.next = nxt
        }
        if (curr == cond.ifFalse.get) {
          cond.ifFalse = nxt
        }
      }
      case x => {
        if (curr == x.next.get) {
          x.next = nxt
        }
      }
    }
  }

  def apply(cfg: CFG): Unit = {
    if (cfg.isOptimized(BranchSimplify)) {
      return
    }
    cfg.setOptimized(BranchSimplify)

    cfg match {
      case program: CFGProgram => {
        program.methods foreach (BranchSimplify(_))
      }

      // we collect all blocks of a function.
      case method: CFGMethod => {
        if (method.block.isDefined) {
          BranchSimplify(method.block.get)
        }
      }

      case cond: CFGConditional => {
        cond.condition match {
          case bool: BoolLiteral => {
            val nxt: Option[CFG] = {
              bool.value match {
                case true => cond.next
                case false =>
                  cond.ifFalse match {
                    case None => cond.end
                    case Some(cfg) => Option(cfg)
                  }
              }
            }
            if (nxt.isDefined) {
              nxt.get.parents.remove(cond)
            }
            for (par <- cond.parents) {
              adjustPar(par, cond, nxt)
              if (nxt.isDefined) {
                nxt.get.parents.add(par)
              }
            }
            if (nxt.isDefined) {
              BranchSimplify(nxt.get)
            }
          }

          case _ => {
            if (cond.next.isDefined) {
              BranchSimplify(cond.next.get)
            }
            if (cond.ifFalse.isDefined) {
              BranchSimplify(cond.ifFalse.get)
            }
          }
        }
      }

      case other => {
        if (other.next.isDefined) {
          BranchSimplify(other.next.get)
        }
      }
    }
  }
}
