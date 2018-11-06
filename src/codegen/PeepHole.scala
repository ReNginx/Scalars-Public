package codegen

import scala.collection.mutable.Set

object PeepHole {

  val set: Set[CFG] = Set[CFG]()

  def apply(cfg: CFG): Option[CFG] = {
    if (set.contains(cfg))
      return Option(cfg)
    set.add(cfg)

    cfg match {
      case virtualCFG: VirtualCFG => {
        virtualCFG.next match {
          case Some(next) => {
            for (parent <- virtualCFG.parents) {
              next.parents.add(parent)
            }
            next.parents.remove(virtualCFG)
            virtualCFG.next = PeepHole(next)
            virtualCFG.next
          }
          case None => Option(virtualCFG)
        }
      }

      case block: CFGBlock => {
        if (block.parents.size == 1) {
          for (parent <- block.parents) {
            parent match {
              case prevBlock: CFGBlock => {
                prevBlock.statements ++= block.statements
                block.next match {
                  case Some(next) => {
                    next.parents.add(parent)
                    next.parents.remove(block)
                    block.next = PeepHole(next)
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
          block.next = PeepHole(block.next.get)
        Option(block)
      }

      case conditional: CFGConditional => {
        if (conditional.end.isDefined)
          conditional.end = PeepHole(conditional.end.get)
        if (conditional.next.isDefined)
          conditional.next = PeepHole(conditional.next.get)
        if (conditional.ifFalse.isDefined)
          conditional.ifFalse = PeepHole(conditional.ifFalse.get)
        Option(conditional)
      }

      case method: CFGMethod => {
        if (method.block.isDefined)
          method.block = PeepHole(method.block.get)
        Option(method)
      }

      case call: CFGMethodCall => {
        if (call.next.isDefined)
          call.next = PeepHole(call.next.get)
        Option(call)
      }

      case program: CFGProgram => {
        program.methods foreach { x => PeepHole(x) }
        Option(program)
      }
    }
  }
}
