// package codegen
//
// import scala.collection.mutable.{HashSet, Set}
// import scala.collection.immutable.Map
//
// import ir.components._
// import ir.PrettyPrint
//
// object Allocate {
//   val sizeOfVar:Int = 8
//   var offset:Int = 0
//
//   def apply(cfg: CFG): Unit = {
//     if (cfg.isAllocated)
//       return
//     cfg.isAllocated = true
//
//     cfg match {
//       case VirtualCFG(_, _, next) => {
//         if (next.isDefined)
//           Allocate(next.get)
//       }
//
//       case CFGBlock(_, statements, _, next) => {
//         for (statement <- statements) {
//           statement match {
//             case variable: VariableDeclaration => {
//                 variable.offset = offset
//                 offset -= sizeOfVar
//             }
//
//             case array: ArrayDeclaration => {
//               array.offset = offset
//               offset -= array.length.value.toInt * sizeOfVar
//             }
//
//             case _ =>
//           }
//         }
//
//         if (next.isDefined)
//             Allocate(next.get)
//       }
//
//       case CFGConditional(_, _, _, next, ifFalse) => {
//         if (next.isDefined)
//           Allocate(next.get)
//         if (ifFalse.isDefined)
//           Allocate(ifFalse.get)
//       }
//
//       case CFGMethod(_, block, params, _, _) => {
//         for (param <- params) {
//           param.offset = offset
//           offset -= sizeOfVar
//         }
//
//         Allocate(block)
//       }
//
//       case CFGProgram(_, _, fields, methods) => {
//         fields foreach { _.isGlobal = true }
//         for (method <- methods) {
//           offset = -sizeOfVar // local var starts from -8
//           Allocate(method)
//           method.spaceAllocated = offset + sizeOfVar
//         }
//       }
//
//       case _ =>
//     }
//   }
// }
