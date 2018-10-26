// package codegen
//
// import scala.collection.mutable.{HashSet, Set}
// import scala.collection.immutable.Map
//
// import ir.components._
// import ir.PrettyPrint
//
//
// object TranslateCFG {
//
//   def output(str: String) = {
//       throw Exception
//   }
//
//   def apply(cfg: CFG, untilBlock: Option[CFG] = None) = {
//     if (Option(cfg) == untilBlock) {
//       output("jmp " + cfg.label)
//       return
//     }
//
//     if (cfg.isTranslated) {
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
//         output(label)
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
//         // copy params to their place. note form here, could be var or literal???
//         output("call " + next.get.label)
//         // call should have a next block, which is not this one.
//         // for now we don't
//          restore regs, rather we copy them to stack at beginning of a method..
//       }
//
//       case method: CFGMethod => {
//         output(method.label + ":")
//         output(s"enter $${method.spaceAllocated}, \$ 0")
//         // TODO copy params from regs and stacks
//         TranslateCFG(method.block)
//         // TODO here deal with no return error.
//       }
//
//       case CFGProgram(label, _, fields, methods) => {
//         output(".bss")
//         fields foreach { output(TranslateIR(_)) }
//         output(".section .rodata")
//         // TODO string literal goes here.
//         output(".text")
//         methods foreach { output(TranslateCFG(_) }
//       }
//
//       case _ =>
//     }
//   }
// }
