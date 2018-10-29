package codegen

import scala.collection.mutable.{HashSet, Set, ArrayBuffer}
import scala.collection.immutable.Map
import ir.components._
import ir.PrettyPrint
import java.io._


object TranslateCFG {
  val strs: ArrayBuffer[Tuple2[String, String]] = ArrayBuffer() // all string literals go here.
  var fileName: String = "output.s"
  lazy val writer: BufferedWriter = new BufferedWriter(new FileWriter(new File(fileName)))

  def output(str: String) = {
    writer.write(str + "\n")
    println(str)
  }

  def close() = {
    writer.close()
  }

  private def outputMov(from: String, to: String) = {
    if (from(0) == '%' || to(0) == '%') {
      output(s"\tmovq ${from}, ${to}")
    }
    else {
      output(s"\tmovq ${from}, %rax")
      output(s"\tmovq %rax, ${to}")
    }
  }

  private def outputVec(strs: Vector[String]) = {
    strs foreach {output(_)}
  }

  //params stores locations
  private def paramCopy(params: Vector[IR]): Int = { // could either be a literal or a location
    val regs = Vector("%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9")
    for ((param, reg) <- params zip regs) { // first six go to regs
      param match {
        case loc: Location => {
          outputMov(loc.rep, reg)
        }

        case str: StringLiteral => {
          val name = s"str_r${str.line},c${str.col}";
          strs += Tuple2(name, str.value)
          outputMov("$"+name, reg)
        }

        case lit: Literal => {
          outputMov("$"+lit.rep, reg)
        }

        case _ => throw new NotImplementedError()
      }
    }

    for (i <- params.length-1 to 6 by -1) { //rest goto stack in reverse order
      val param = params(i)
      param match {
        case loc: Location => {
          output(s"\tmovq ${loc.rep}, %rax")
          output(s"\tpushq %rax")
        }

        case str: StringLiteral => {
          val name = s"str_r${str.line},c${str.col}";
          strs += Tuple2(name, str.value)
          output(s"\tmovq $$.${name}, ${regs}")
          output(s"\tpushq %rax")
        }

        case lit: Literal => {
          output(s"\tmovq $$${lit.rep}, %rax")
          output(s"\tpushq %rax")
        }
      }
    }

    math.max(0, (params.length - 6)*8) // params pushed to stack
  }

  def apply(cfg: CFG, untilBlock: Option[CFG] = None): Unit = {
    if (cfg.isTranslated || Option(cfg) == untilBlock) {
      output("jmp " + cfg.label)
      return
    }

    cfg.isTranslated = true

    cfg match {
      case VirtualCFG(label, _, next) => {
        output(label + ":")
        if (next.isDefined)
          TranslateCFG(next.get)
      }

      case CFGBlock(label, statements, next, _) => {
        output(label + ":")
        for (statement <- statements) {
          if (!statement.isInstanceOf[FieldDeclaration])
            outputVec(TranslateIR(statement))
        }

        if (next.isDefined)
            TranslateCFG(next.get)
      }

      case CFGConditional(label, condition, next, ifFalse, end, _) => {
        assert(ifFalse.isDefined)

        output(label + ":")
        output(s"\tmovq ${condition.eval.get.rep}, %rax")
        output(s"\ttest %rax, %rax")
        output(s"jne ${ifFalse.get.label}")

        if (next.isDefined) {
          TranslateCFG(next.get, end)
        }
        if (ifFalse.isDefined) {
          TranslateCFG(ifFalse.get)
        }
      }

      case CFGMethodCall(_, params, declaration, next, _) => {
        val sizePushedToStack = paramCopy(params)
        //we call this function
        output(s"\tcall ${declaration}")
        // destroy used params
        output(s"\taddq %rsp ${sizePushedToStack}")

        if (next.isDefined)
          TranslateCFG(next.get)
        // for now we don't restore regs, rather we copy them to stack at beginning of a method..
      }

      case method: CFGMethod => {
        output(s".globl ${method.label}")
        output(method.label + ":")
        output(s"\tenter $$${method.spaceAllocated}, $$0")
        // copy params from regs and stacks
        val regs = Vector("%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9")
        for ((param, i) <- method.params.zipWithIndex) { // params are decl
          val from = param.asInstanceOf[VariableDeclaration].rep
          val to = if (i < 6) regs(i) else s"${16 + 8*(i-6)}(%rbp)"
          outputMov(from, to);
        }
        if (method.block.isDefined) {
          TranslateCFG(method.block.get)
          if (method.method.typ == Option(VoidType)) {
            output(s"\tleave")
            output(s"\tret")
          }
          else {
            output(s"\tjmp noReturn")
          }
        }
      }

      case CFGProgram(_, params, methods, _, _) => {
        //gobal var goes here.
        output(".bss")
        params foreach { x => outputVec(TranslateIR(x)) }
        //functions goes here.
        output(".text")
        for (mthd <- methods) {
          TranslateCFG(mthd)// methods is a map, iterate through its value
        }

        //deal with runtime check.
        output("noReturn:")
        output("\tmovq $-2, %rdi")
        output("\tcall exit")
        output("\toutOfBound:")
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
