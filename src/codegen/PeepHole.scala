package codegen

import scala.collection.mutable.Set
import ir.components._
object PeepHole {

  val set: Set[CFG] = Set[CFG]()
  val del: Set[CFG] = Set[CFG]()

  private def init(): Unit = {
    set.clear()
    del.clear()
  }

  private def adjustPar(parent: CFG, curr: CFG): Unit = {
    parent match {
      case cond: CFGConditional => {
        if (curr == cond.next.get) {
          cond.next = curr.next
        }
        if (curr == cond.ifFalse.get) {
          cond.ifFalse = curr.next
        }
      }
      case x => {
        if (curr == x.next.get) {
          x.next = curr.next
        }
      }
    }
  }

  def apply(cfg: CFG, preserveCritical: Boolean, isInit: Boolean = true): Option[CFG] = {
    if (isInit) { init() }
    if (set.contains(cfg))
      return if (del.contains(cfg)) cfg.next else Option(cfg)
    set.add(cfg)

    cfg match {
      case virtualCFG: VirtualCFG => {
        virtualCFG.next match {
          case Some(next) => {
            if (preserveCritical && virtualCFG.isCritical) {
              virtualCFG.next = PeepHole(next, preserveCritical, false)
              Option(virtualCFG)
            }
            else {
              del.add(virtualCFG)
              for (parent <- virtualCFG.parents) {
                next.parents.add(parent)
                adjustPar(parent, virtualCFG)
              }
              next.parents.remove(virtualCFG)
              virtualCFG.next = PeepHole(next, preserveCritical, false)
              virtualCFG.next
            }
          }
          case None => Option(virtualCFG)
        }
      }

      case block: CFGBlock => {
        if (block.parents.size == 1) {
          for (parent <- block.parents) {
            parent match {
              case prevBlock: CFGBlock => {
                assert(!block.isCritical) // "body" should be a singular CFGBlock
                del.add(block)
                prevBlock.statements ++= block.statements
                block.next match {
                  case Some(next) => {
                    next.parents.add(parent)
                    next.parents.remove(block)
                    adjustPar(parent, next)
                    block.next = PeepHole(next, preserveCritical, false)
                    return block.next
                  }
                  case None => None
                }
              }
              case _ =>
            }
          }
        }

        if (block.next.isDefined)
          block.next = PeepHole(block.next.get, preserveCritical, false)
        Option(block)
      }

      case cond: CFGConditional => {
        cond.condition match {
          case bool: BoolLiteral => {
            //System.err.println(s"removed ${cond.label}")
            del.add(cond)
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
            cond.next = nxt
            for (par <- cond.parents) {
              adjustPar(par, cond)
              if (nxt.isDefined) {
                nxt.get.parents.add(par)
              }
            }
            cond.next = PeepHole(nxt.get, preserveCritical, false)
            cond.next
          }

          case _ => {
            if (cond.end.isDefined)
              cond.end = PeepHole(cond.end.get, preserveCritical, false)
            if (cond.next.isDefined)
              cond.next = PeepHole(cond.next.get, preserveCritical, false)
            if (cond.ifFalse.isDefined)
              cond.ifFalse = PeepHole(cond.ifFalse.get, preserveCritical, false)
            Option(cond)
          }
        }
      }

      case method: CFGMethod => {
        if (method.block.isDefined)
          method.block = PeepHole(method.block.get, preserveCritical, false)
        Option(method)
      }

      case call: CFGMethodCall => {
        if (call.next.isDefined)
          call.next = PeepHole(call.next.get, preserveCritical, false)
        Option(call)
      }

      case program: CFGProgram => {
        program.methods foreach { x => PeepHole(x, preserveCritical, false) }
        for (cfg <- set) {
          val oldPar = cfg.parents.clone()
          for (par <- oldPar)
            if (!set.contains(par))
              cfg.parents.remove(par)
        }
        set.clear()
        del.clear()
        Option(program)
      }
    }
  }
}
