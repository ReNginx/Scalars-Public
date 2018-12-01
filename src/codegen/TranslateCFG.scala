package codegen

import scala.collection.mutable.{HashSet, Set, ArrayBuffer}
import scala.collection.immutable.Map
import ir.components._
import ir.PrettyPrint
import java.io._


object TranslateCFG {
  val aryIdxReg1: String = "%r10" // use %r10 for array indexing
  val aryIdxReg2: String = "%r11" // use %r11 when both sides are assignments

  val strs: ArrayBuffer[Tuple2[String, String]] = ArrayBuffer() // all string literals go here.
  var fileName: Option[String] = None
  var debug: Boolean = false
  lazy val fileWriter: BufferedWriter = new BufferedWriter(new FileWriter(new File(fileName.get)))
  lazy val printWriter: PrintWriter = new PrintWriter(System.out)

  def closeOutput() = {
    if (!fileName.isEmpty) {
      fileWriter.close
    } else {
      printWriter.close
    }
  }

  private def output(str: String) = {
    if (!fileName.isEmpty) {
      fileWriter.write(str + "\n")
      if (debug) println(str)
    } else {
      printWriter.write(str + "\n")
    }
  }

  private def outputVec(strs: Vector[String]) = {
    strs foreach {output(_)}
  }

  //params stores locations
  private def paramCopy(params: ArrayBuffer[Expression]): Int = { // could either be a literal or a location
    val regs = Vector("%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9")
    for ((param, reg) <- params zip regs) { // first six go to regs
      param match {
        case loc: Location => {
          outputVec(loc.indexCheck)
          val (repVec: Vector[String], repStr: String) = loc.getRep(aryIdxReg1)
          outputVec(repVec)
          outputVec(Helper.outputMov(repStr, reg))
        }

        case str: StringLiteral => {
          val name = s"str_r${str.line}_c${str.col}";
          strs += Tuple2(name, str.value)
          outputVec(Helper.outputMov("$"+name, reg))
        }

        case lit: Literal => {
          outputVec(Helper.outputMov(lit.rep, reg)) // .rep is safe for literals
        }

        case _ => throw new NotImplementedError()
      }
    }

    for (i <- params.length-1 to 6 by -1) { //rest goto stack in reverse order
      val param = params(i)
      param match {
        case loc: Location => {
          outputVec(loc.indexCheck)
          val (repVec: Vector[String], repStr: String) = loc.getRep(aryIdxReg1)
          outputVec(repVec)
          output(s"\tmovq ${repStr}, %rax")
          output(s"\tpushq %rax")
        }

        case str: StringLiteral => {
          val name = s"str_r${str.line}_c${str.col}"
          strs += Tuple2(name, str.value)
          output(s"\tmovq $$.${name}, ${regs}")
          output(s"\tpushq %rax")
        }

        case lit: Literal => {
          output(s"\tmovq ${lit.rep}, %rax")
          output(s"\tpushq %rax")
        }
      }
    }

    math.max(0, (params.length - 6)*8) // params pushed to stack
  }

  def apply(
            cfg: CFG,
            outFile: Option[String],
            debugSwitch: Boolean,
            untilBlock: Option[CFG] = None): Unit = {

    fileName = outFile
    debug = debugSwitch

    if (cfg.isTranslated || Option(cfg) == untilBlock) {
      output("\tjmp " + cfg.label)
      return
    }

    cfg.isTranslated = true

    cfg match {
      case VirtualCFG(label, _, next) => {
        output(label + ":")
        if (next.isDefined)
          TranslateCFG(next.get, fileName, debug, untilBlock)
      }

      case CFGBlock(label, statements, next, _) => {
        output(label + ":")
        for (statement <- statements) {
          if (!statement.isInstanceOf[FieldDeclaration])
            outputVec(TranslateIR(statement))
        }

        if (next.isDefined)
            TranslateCFG(next.get, fileName, debug, untilBlock)
      }

      case CFGConditional(label, condition, next, ifFalse, end, _) => {
        assert(ifFalse.isDefined)
        output(label + ":")
        condition.eval.get match {
          case loc: Location => {
            outputVec(loc.indexCheck)
          }
          case _ => {
            //System.err.println(label)
            throw new NotImplementedError()
          }

        }
        val (repVec: Vector[String], repStr: String) = condition.eval.get.getRep(aryIdxReg1)
        outputVec(repVec)
        output(s"\tmovq ${repStr}, %rax")
        output(s"\ttest %rax, %rax")
        output(s"\tje ${ifFalse.get.label}")

        assert(end.isDefined)
        if (next.isDefined) {
          TranslateCFG(next.get, fileName, debug, end)
        }
        if (ifFalse.isDefined) {
          TranslateCFG(ifFalse.get, fileName, debug, end)
        }
        assert(end.isDefined)
        TranslateCFG(end.get, fileName, debug, untilBlock)
      }

      case CFGMethodCall(label, params, declaration, next, _) => {
        output(label + ":")
        val sizePushedToStack = paramCopy(params)
        //we call this function
        output(s"\txor %rax, %rax")
        output(s"\tcall ${declaration}")
        // destroy used params
        output(s"\taddq $$${sizePushedToStack}, %rsp")

        if (next.isDefined)
          TranslateCFG(next.get, fileName, debug, untilBlock)
        // for now we don't restore regs, rather we copy them to stack at beginning of a method..
      }

      case method: CFGMethod => {
        output(s".globl ${method.label}")
        output(method.label + ":")
        output(s"\tenter $$${method.spaceAllocated}, $$0")
        // for (i <- 1 to (-method.spaceAllocated / 8)) {
        //     output(s"\tmovq $$0, ${-i*8}(%rbp)")
        // }
        // copy params from regs and stacks
        val regs = Vector("%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9")
        for ((param, i) <- method.params.zipWithIndex) { // params are decl
          val from = if (i < 6) regs(i) else s"${16 + 8*(i-6)}(%rbp)"
          val to = param.asInstanceOf[VariableDeclaration].rep // .rep is safe for VariableDeclaration
          outputVec(Helper.outputMov(from, to))
        }
        if (method.block.isDefined) {
          TranslateCFG(method.block.get, fileName, debug)
        }
        if (method.method.name == "main") {
          output("\tmovq $0, %rax")
          output(s"\tleave")
          output(s"\tret")
        }
        else if (method.method.typ == Option(VoidType)) {
          output(s"\tleave")
          output(s"\tret")
        }
        else {
          output(s"\tjmp noReturn")
        }
      }

      case CFGProgram(_, params, methods, _, _) => {
        //gobal var goes here.
        output(".bss")
        params foreach { x => outputVec(TranslateIR(x)) }
        //functions goes here.
        output(".text")
        for (mthd <- methods) {
          TranslateCFG(mthd, fileName, debug)// methods is a map, iterate through its value
        }

        //deal with runtime check.
        output(".globl noReturn")
        output("noReturn:")
        output("\tmovq $-2, %rdi")
        output("\tcall exit")
        output(".globl outOfBound")
        output("outOfBound:")
        output("\tmovq $-1, %rdi")
        output("\tcall exit")

        //string goes here
        output(".section .rodata")
        strs foreach {
          x =>
          output("\t"+x._1 + ":");
          output(s"\t\t.string ${x._2}");
        }
      }

      case _ => throw new NotImplementedError()
    }
  }
}
