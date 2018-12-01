package codegen

import java.io.{BufferedWriter, File, FileWriter}

import ir.components._
import scala.sys.process._
import scala.collection.mutable.{ArrayBuffer, HashSet, Map, Set}

object PrintCFG {
  var fileName: String = "cfg.dot"
  lazy val writer: BufferedWriter = new BufferedWriter(new FileWriter(new File(fileName)))

  def init(): Unit = {
    writer.write("digraph ER { \n")
  }

  def close(): Unit = {
    writer.write("label=\"cfg graph\"}")
    writer.close()
    s"dot -Tpng ${fileName} -o cfg.png".!
  }

  def doHeader(cfg: CFG): Unit = {
    System.err.println("\n\n#########START")
    System.err.println(s"parents are ${cfg.parents}")
    for (par <- cfg.parents) {
      writer.write(s"${cfg.label} -> ${par.label}[style=dotted];\n")
    }
    System.err.println(cfg.getClass.toString + ":  " + cfg.label)
  }

  def prtNext(cfg: CFG): Unit = {
    if (cfg.next.isDefined) {
      System.err.println("Next: " + cfg.next.get.label)
      writer.write(s"${cfg.label} -> ${cfg.next.get.label};\n")
    }
    System.err.println("#########END\n\n")
  }

  def goNext(cfg: CFG): Unit = {
    if (cfg.next.isDefined) {
      PrintCFG(cfg.next.get)
    }
  }

  def prtFalse(cfg: CFGConditional): Unit = {
    if (cfg.ifFalse.isDefined) {
      System.err.println("False " + cfg.ifFalse.get.label)
      System.err.println("\n\n")
      writer.write(s"${cfg.label} -> ${cfg.ifFalse.get.label};\n")
    }
  }

  def prtEnd(cfg: CFGConditional): Unit = {
    if (cfg.end.isDefined) {
      System.err.println("End " + cfg.end.get.label)
      System.err.println("\n\n")
      writer.write(s"${cfg.label} -> ${cfg.end.get.label}[style=dashed];\n")
    }
  }

  def goFalse(cfg: CFGConditional): Unit = {
    if (cfg.ifFalse.isDefined) {
      PrintCFG(cfg.ifFalse.get)
    }
  }

  def prtStmt(ir: IR) {
    ir match {
      case assign: AssignStatement => {
        assert(assign.loc.eval.isDefined)
        System.err.println(s"line:${assign.line},col:${assign.col}, Assign ${assign.loc.eval.get.cfgRep}, ${assign.value.cfgRep}")
      }
      case compoundAsg: CompoundAssignStatement => {
        assert(compoundAsg.loc.eval.isDefined)
        System.err.println(s"line:${compoundAsg.line},col:${compoundAsg.col}, CompoundAssign ${compoundAsg.loc.eval.get.cfgRep}, ${compoundAsg.value.cfgRep}")
      }
      case inc: Increment => {
        assert(inc.loc.eval.isDefined)
        System.err.println(s"line:${inc.line},col:${inc.col}, Inc ${inc.loc.eval.get.cfgRep}")
      }
      case dec: Decrement => {
        assert(dec.loc.eval.isDefined)
        System.err.println(s"line:${dec.line},col:${dec.col}, Dec ${dec.loc.eval.get.cfgRep}")
      }
      case unary: UnaryOperation => {
        assert(unary.expression.eval.isDefined)
        System.err.println(s"line:${unary.line},col:${unary.col}, Unary ${unary.eval.get.cfgRep} = ${unary.expression.eval.get.cfgRep}")
      }
      case binary: BinaryOperation => {
        assert(binary.eval.isDefined)
        System.err.println(s"line:${binary.line},col:${binary.col}, Binary ${binary.eval.get.cfgRep} = ${binary.lhs.cfgRep} op ${binary.rhs.cfgRep}")
      }
      case ret: Return => {
        if (ret.value.isDefined) {
          System.err.println(s"line:${ret.line},col:${ret.col}, Return ${ret.value.get.cfgRep}")
        }
        else {
          System.err.println(s"line:${ret.line},col:${ret.col}, Return")
        }
      }
      case _ => throw new NotImplementedError()
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
          stmt => prtStmt(stmt)
        }

        prtNext(block)
        goNext(block)
      }

      case conditional: CFGConditional => {
        doHeader(conditional)
        System.err.println(s"cond: ${conditional.condition.cfgRep}")

        prtFalse(conditional)
        prtNext(conditional)
        prtEnd(conditional)

        goNext(conditional)
        goFalse(conditional)
      }

      case method: CFGMethod => {
        doHeader(method)
        print("param_List: ")
        method.params foreach { x => print(x.name + " ") }
        System.err.println()
        if (method.block.isDefined) {
          System.err.println("Block " + method.block.get.label)
          writer.write(s"${method.label} -> ${method.block.get.label};\n")
          PrintCFG(method.block.get)
        }
      }

      case call: CFGMethodCall => {
        doHeader(call)
        System.err.println("param_List: ")
        call.params foreach { x => print(x.cfgRep + " ") }
        System.err.println()
        System.err.println(call.declaration)
        System.err.println()
        prtNext(call)
        goNext(call)
      }

      case program: CFGProgram => {
        program.methods foreach { x => PrintCFG(x) }
      }
    }
  }
}
