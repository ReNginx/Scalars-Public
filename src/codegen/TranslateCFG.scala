package codegen

import scala.collection.mutable.{HashSet, Set}
import scala.collection.immutable.Map
import scala.collection.mutable.ArrayBuffer
import ir.components._
import ir.PrettyPrint


object TranslateCFG {
  val strs: ArrayBuffer[Tuple2[String, String]] = ArrayBuffer() // all string literals go here.
  var fileName: String = ""

  def output(str: String) = {
      //TODO
  }

  private def outputMov(from: String, to: String) = {
    if (from(0) == '%' || to(0) == '%') {
      output(s"movq ${from}, ${to}")
    }
    else {
      output(s"movq ${from}, %rax")
      output(s"movq %rax, ${to}")
    }
  }

  private def outputVec(strs: Vector[String]) = {
    //TODO
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

        case bool: BoolLiteral => {
          outputMov("$"+(if (bool.value) "1" else "0"), reg)
        }

        case char: CharLiteral => {
          outputMov("$"+char.value.toInt.toString, reg)
        }
      }
    }

    for (i <- params.length-1 to 6 by -1) { //rest goto stack in reverse order
      val param = params(i)
      param match {
        case loc: Location => {
          output(s"movq ${loc.rep}, %rax")
          output(s"pushq %rax")
        }

        case str: StringLiteral => {
          val name = s"str_r${str.line},c${str.col}";
          strs += Tuple2(name, str.value)
          output(s"movq $$.${name}, ${regs}")
          output(s"pushq %rax")
        }

        case bool: BoolLiteral => {
          output(s"movq $$${if (bool.value) 1 else 0}, %rax")
          output(s"pushq %rax")
        }

        case char: CharLiteral => {
          output(s"movq $$${char.value.toInt}, %rax")
          output(s"pushq %rax")
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

      case CFGConditional(label, statements, _, next, ifFalse, end) => {
        output(label + ":")
        //TODO not sure about how to deal with conditional.

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
        output(s"call ${declaration.label}")
        // destroy used params
        output(s"addq %rsp ${sizePushedToStack}")

        if (next.isDefined)
          TranslateCFG(next.get)
        // for now we don't restore regs, rather we copy them to stack at beginning of a method..
      }

      case method: CFGMethod => {
        output(method.label + ":")
        output(s"enter $$${method.spaceAllocated}, $$0")
        // copy params from regs and stacks
        val regs = Vector("%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9")
        for ((param, i) <- method.params.zipWithIndex) { // params are decl
          val from = param.asInstanceOf[VariableDeclaration].rep
          val to = if (i < 6) regs(i) else s"${16 + 8*(i-6)}(%rbp)"
          outputMov(from, to);
        }
        if (method.block.isDefined)
          TranslateCFG(method.block.get)
        // TODO here deal with no return error.
      }

      case CFGProgram(_, _, fields, methods, _, _) => {
        //gobal var goes here.
        output(".bss")
        fields.statements foreach { x => outputVec(TranslateIR(x)) }
        //functions goes here.
        output(".text")
        methods foreach {x => TranslateCFG(x._2) } // methods is a map, iterate through its value
        //string goes here
        output(".section .rodata")
        strs foreach {
          x =>
          output(x._1 + ":");
          output(s".string ${x._2}");
        }
      }

      case _ =>
    }
  }
}
