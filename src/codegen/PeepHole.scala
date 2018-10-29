package codegen

import scala.collection.mutable.Set

object PeepHole {

  val set: Set[CFG] = Set[CFG]()

  def apply(cfg: CFG) {
    if (set.contains(cfg))
      return
    set.add(cfg)

    cfg match {
      case virtualCFG: VirtualCFG => {
        if (virtualCFG.next.isDefined) {
          for (parent <- virtualCFG.parents) {
            parent.next = virtualCFG.next
            virtualCFG.next.get.parents.add(parent)
          }
          PeepHole(virtualCFG.next.get)
        }
      }

      case block: CFGBlock => {
        if (block.parents.size == 1) {
          for (parent <- block.parents) {
            if (parent.isInstanceOf[CFGBlock]) {
              parent.next = block.next
              if (block.next.isDefined)
                block.next.get.parents.add(parent)
              parent.asInstanceOf[CFGBlock].statements ++= block.statements
            }
          }
        }

        if (block.next.isDefined)
          PeepHole(block.next.get)
      }

      case conditional: CFGConditional => {
        if (conditional.next.isDefined)
          PeepHole(conditional.next.get)
        if (conditional.ifFalse.isDefined)
          PeepHole(conditional.ifFalse.get)
        assert(conditional.end.isDefined)
        for (parent <- conditional.end.get.parents) {
          conditional.end = parent.next
        }
      }

      case method: CFGMethod => {
        if (method.block.isDefined)
          PeepHole(method.block.get)
      }

      case call: CFGMethodCall => {
        if (call.next.isDefined)
          apply(call.next.get)
      }

      case program: CFGProgram => {
        program.methods foreach { x => PeepHole(x) }
      }
    }
  }
}
