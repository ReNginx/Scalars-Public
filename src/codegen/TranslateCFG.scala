// package codegen
//
// import scala.collection.mutable.{HashSet, Set}
// import scala.collection.immutable.Map
// import scala.collection.mutable.Vector
// import ir.components._
// import ir.PrettyPrint
//
//
// object TranslateCFG {
//   val strs: Vector[Tuple2[String, String]] = new Vector
//
//   def output(str: String) = {
//       throw Exception
//   }
//
//   private def outputMov(from: String, to: String) = {
//     if (from(0) == '%' || to(0) == '%') {
//       output(s"movq ${from}, ${to}")
//     }
//     else {
//       output(s"movq ${from}, %rax")
//       output(s"movq %rax, ${to}")
//     }
//   }
//
//   //params stores locations
//   private def paramCopy(params: Vector[IR]): Int = { // could either be a literal or a location
//     for ((param, reg) <- params zip regs) { // first six go to regs
//       val regs = Vector("%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9")
//       param match {
//         case loc: Location => {
//           outputMov(loc.decl.offsetRep.toString, reg)
//         }
//
//         case str: StringLiteral => {
//           val name = s"str_r${str.line},c${str.col}";
//           strs += Tuple2(name, str.value)
//           outputMov("$"+name, reg)
//         }
//
//         case bool: BoolLiteral => {
//           outputMov("$"+(if bool.value "1" else "0"), reg)
//         }
//
//         case char: CharLiteral => {
//           outputMov("$"+char.value.toInt.toString, reg)
//         }
//       }
//     }
//
//     for (i <- params.length-1 to 6 by -1) { //rest goto stack in reverse order
//       val param = params(i)
//       param match {
//         case loc: Location => {
//           output(s"movq ${loc.decl.offsetRep}, %rax")
//           output(s"pushq %rax")
//         }
//
//         case str: StringLiteral => {
//           val name = s"str_r${str.line},c${str.col}";
//           strs += Tuple2(name, str.value)
//           output(s"movq $.${name}, ${regs}")
//           output(s"pushq %rax")
//         }
//
//         case bool: BoolLiteral => {
//           output(s"movq $${if (bool.value) 1 else 0}, %rax")
//           output(s"pushq %rax")
//         }
//
//         case char: CharLiteral => {
//           output(s"movq $${char.value.toInt}, %rax")
//           output(s"pushq %rax")
//         }
//       }
//     }
//
//     math.max(0, (params.length - 6)*8) // params pushed to stack
//   }
//
//   def apply(cfg: CFG, untilBlock: Option[CFG] = None) = {
//     if (cfg.isTranslated || Option(cfg) == untilBlock) {
//       output("jmp " + cfg.label)
//       return
//     }
//
//     cfg.isTranslated = true
//
//     cfg match {
//       case VirtualCFG(label, _, next) => {
//         output(label + ":")
//         if (next.isDefined)
//           TranslateCFG(next.get)
//       }
//
//       case CFGBlock(label, statements, _, next) => {
//         output(label + ":")
//         for (statement <- statements) {
//           statment match {
//             case decl: FieldDeclaration => // ignore decls.
//             case _ => output(TranslateIR(_))
//           }
//         }
//
//         if (next.isDefined)
//             TranslateCFG(next.get)
//       }
//
//       case CFGConditional(label, statements, _, next, ifFalse, end) => {
//         output(label + ":")
//         //TODO not sure about how to deal with conditional.
//
//         if (next.isDefined) {
//           TranslateCFG(next.get, end)
//         }
//         if (ifFalse.isDefined) {
//           TranslateCFG(ifFalse.get)
//         }
//       }
//
//       case CFGMethodCall(_, params, next, _) => {
//         val sizePushedToStack = paramCopy(params)
//         output(s"call ${next.get.label}")
//         // destroy used params
//         output(s"addq %rsp ${sizePushedToStack}")
//         // call should have a next block, which is not this one.
//         // for now we don't restore regs, rather we copy them to stack at beginning of a method..
//       }
//
//       case method: CFGMethod => {
//         output(method.label + ":")
//         output(s"enter $${method.spaceAllocated}, \$ 0")
//         // copy params from regs and stacks
//         val regs = Vector("%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9")
//         for ((param, i) <- method.params.zipWithIndex) { // params are decl
//           val from = param.offsetRep
//           val to = if i < 6 regs(i) else s""
//         }
//         TranslateCFG(method.block)
//         // TODO here deal with no return error.
//       }
//
//       case CFGProgram(label, _, fields, methods) => {
//         //gobal var goes here.
//         output(".bss")
//         fields foreach { output(TranslateIR(_)) }
//         //functions goes here.
//         output(".text")
//         methods foreach { output(TranslateCFG(_._2)) } // methods is a map, iterate through its value
//         //string goes here
//         output(".section .rodata")
//         strs foreach {
//           output(_._1 + ":");
//           output(s".string ${_._2}");
//         }
//       }
//
//       case _ =>
//     }
//   }
// }
