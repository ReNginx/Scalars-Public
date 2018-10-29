package codegen

import scala.collection.mutable.{HashSet, Set}
import scala.collection.immutable.Map

import ir.components._
import ir.PrettyPrint

object Allocate {
  val sizeOfVar:Int = 8
  var offset:Int = 0

  def allocateDecl(decl: FieldDeclaration): Unit = {
    decl match {
      case variable: VariableDeclaration => {
        if (!variable.isGlobal && variable.offset != 0) {
          variable.offset = offset
          offset -= sizeOfVar
        }
      }

      case array: ArrayDeclaration => {
        if (!array.isGlobal && array.offset != 0) {
          array.offset = offset
          offset -= array.length.value.toInt * sizeOfVar
        }
      }
    }
  }

  def apply(cfg: CFG): Unit = {
    if (cfg.isAllocated)
      return
    cfg.isAllocated = true

    cfg match {
      case VirtualCFG(_, _, next) => {
        if (next.isDefined)
          Allocate(next.get)
      }

      case CFGBlock(_, statements, next, _) => {
        for (statement <- statements) {
          statement match {
            case field: FieldDeclaration => allocateDecl(field)
            case assign: Assignment => allocateDecl(assign.loc.field.get)
            case _ =>
          }
        }

        if (next.isDefined)
            Allocate(next.get)
      }

      case CFGConditional(_, condition, _, next, ifFalse, _) => {
        condition.eval.get match {
          case variable: VariableDeclaration => {
            variable.offset = offset
            offset -= sizeOfVar
          }
          case literal: Literal =>
          case _ => throw new NotImplementedError()
        }
        if (next.isDefined)
          Allocate(next.get)
        if (ifFalse.isDefined)
          Allocate(ifFalse.get)
      }

      case CFGMethod(_, block, params, _, _, _) => {
        for (param <- params) {
          param.asInstanceOf[VariableDeclaration].offset = offset
          offset -= sizeOfVar
        }

        Allocate(block.get)
      }

      case CFGMethodCall(_, _, _, next, _) => {
        if (next.isDefined)
          Allocate(next.get)
      }

      case CFGProgram(_, fields, methods, _, _) => {
        fields foreach {
          case array: ArrayDeclaration => array.isGlobal = true
          case variable: VariableDeclaration => variable.isGlobal = true
        }

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
