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
        if (!variable.isGlobal && !variable.isReg && variable.offset == 0) {
          variable.offset = offset
          offset -= sizeOfVar
        }
      }

      case array: ArrayDeclaration => {
        assert(!array.isReg)
        if (!array.isGlobal && !array.isReg && array.offset == 0) {
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

    //println(cfg.label) //DEBUG

    cfg match {
      case VirtualCFG(_, _, next) => {
        if (next.isDefined)
          Allocate(next.get)
      }

      case CFGBlock(label, statements, next, _) => {
        //println(label) //DEBUG
        for (statement <- statements) {
          statement match {
            case field: FieldDeclaration => allocateDecl(field)
            case assign: Assignment => allocateDecl(assign.loc.field.get)
            case oper: Operation => {
              oper.eval.get match {
                case location: Location => {
                  assert(location.field.isDefined)
                  allocateDecl(location.field.get)
                }
                case _ => throw new NotImplementedError()
              }
            }
            case _: Return =>
            case _ => throw new NotImplementedError()
          }
        }

        if (next.isDefined)
            Allocate(next.get)
      }

      case CFGConditional(_, condition, next, ifFalse, _, _) => {
        condition.eval.get match {
          case location: Location => {
            assert(location.field.isDefined)
            allocateDecl(location.field.get)
          }
          case literal: Literal =>
          case _ => throw new NotImplementedError()
        }
        if (next.isDefined) {
          //println(next.get.label) //DEBUG
          Allocate(next.get)
        }
        if (ifFalse.isDefined) {
          //println(ifFalse.get.label) //DEBUG
          Allocate(ifFalse.get)
        }
      }

      case CFGMethod(_, block, params, _, _, _) => {
        for (param <- params) {
          allocateDecl(param.asInstanceOf[VariableDeclaration])
        }

        Allocate(block.get)
      }

      case CFGMethodCall(_, params, _, next, _) => {
        for (param <- params) {
          param match {
            case location: Location => allocateDecl(location.field.get)
            case _: Literal =>
            case _ => throw new NotImplementedError()
          }
        }
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

      case _ => throw new NotImplementedError()
    }
  }
}
