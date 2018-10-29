package codegen

import ir.components._

import scala.collection.mutable.{ArrayBuffer, HashSet, Map, Set}

object PrintCFG {

  def doHeader(cfg: CFG): Unit = {
    println("\n\n#########START")
    println(cfg.label)
  }

  def prtNext(cfg: CFG): Unit = {
    if (cfg.next.isDefined) {
      println("Next: " + cfg.next.get.label)
      println("#########END\n\n")
    }
  }

  def goNext(cfg: CFG): Unit = {
    if (cfg.next.isDefined) {
      PrintCFG(cfg.next.get)
    }
  }

  def prtFalse(cfg: CFGConditional): Unit = {
    if (cfg.ifFalse.isDefined) {
      println("False " + cfg.ifFalse.get.label)
      println("\n\n")
    }
  }

  def goFalse(cfg: CFGConditional): Unit = {
    if (cfg.ifFalse.isDefined) {
      PrintCFG(cfg.ifFalse.get)
    }
  }


  val set:Set[CFG] = Set[CFG]()

  def apply(cfg: CFG) {
    if (set.contains(cfg))
      return

    set += cfg

    cfg match {
      case virtualCFG: VirtualCFG => {
        doHeader(virtualCFG)
        prtNext(virtualCFG)
        goNext(virtualCFG)
      }

      case block: CFGBlock => {
        doHeader(block)
        block.statements foreach {
          case _: AssignStatement =>
          case _: CompoundAssignStatement =>
          case _: Increment =>
          case _: Decrement =>
          case _: UnaryOperation =>
          case _: BinaryOperation =>
          case _ => throw new NotImplementedError()
        }
      }

      case conditional: CFGConditional => {
        doHeader(conditional)
        println(s"cond: ${conditional.condition.rep}")

        prtFalse(conditional)
        prtNext(conditional)

        goNext(conditional)
        goFalse(conditional)
      }

      case method: CFGMethod => {
        doHeader(method)
        print("param_List: ")
        method.params foreach { x => print(x.name + " ") }
        println()
        prtNext(method)
        goNext(method)
      }

      case call: CFGMethodCall => {
        doHeader(call)
        println("param_List: ")
        call.params foreach { x => print(x.rep + " ") }
        println()
        println(call.declaration)
        println()
        prtNext(call)
        goNext(call)
      }

      case program: CFGProgram => {
        program.methods foreach { x => PrintCFG(x) }
      }
    }
  }
}
