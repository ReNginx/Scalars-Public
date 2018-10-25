package codegen

import scala.collection.mutable.{HashSet, Set}
import scala.collection.immutable.Map

import ir.components._
import ir.PrettyPrint


object TranslateCFG {

  def output(str: String) {
      throw Exception
  }

  def apply(cfg: CFG) {
    cfg match {
      case VirtualCFG(label, _, next) => {
        output(label + ":")
        if (next.isDefined)
          TranslateCFG(next.get)
      }

      case CFGBlock(label, statements, _, next) => {
        output(label + ":")
        for (statement <- statements) {
          statment match {
            case decl: FieldDeclaration => // ignore decls. 
            case _ => output(TranslateIR(_))
          }
        }

        if (next.isDefined)
            TranslateCFG(next.get)
      }

      case CFGConditional(label, statements, _, next, ifFalse) => {
        output(label)
        //TODO not sure about how to deal with conditional.

        if (next.isDefined)
          TranslateCFG(next.get)
        if (ifFalse.isDefined)
          TranslateCFG(ifFalse.get)
      }

      case CFGMethod(label, block, _, _, _) => {
        output(label + ":")
        Allocate(block)
      }

      case CFGProgram(_, _, fields, methods) => {
        fields foreach { _.isGlobal = true }
        for (method <- methods) {
          offset = -sizeOfVar // local var starts from -8
          Allocate(method)
          method.spaceAllocated = offset + sizeOfVar
        }
      }

      case _ =>
    }
  }
}
