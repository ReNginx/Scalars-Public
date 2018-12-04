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
          offset -= array.length.value.toInt * sizeOfVar
          array.offset = offset + sizeOfVar
        }
      }

      case reg: Register =>

      case _ => throw new NotImplementedError()
    }
  }

  def allocateExpr(expr: Expression): Unit = {
      expr match {
        case loc: Location => {
          assert(loc.field.isDefined)
          allocateDecl(loc.field.get)
          if (loc.index.isDefined) {
            allocateExpr(loc.index.get)
          }
        }

        case unary: UnaryOperation => {
          allocateExpr(unary.eval.get)
          allocateExpr(unary.expression)
        }

        case binary: BinaryOperation => {
          allocateExpr(binary.eval.get)
          allocateExpr(binary.lhs)
          allocateExpr(binary.rhs)
        }

        case literal: Literal =>
        case _ => throw new NotImplementedError()
      }
  }

  def apply(cfg: CFG): Unit = {
    if (cfg.isAllocated)
      return
    cfg.isAllocated = true

    //println(cfg.label) //DEBUG

    cfg match {
      case VirtualCFG(_, _, next) => {
        if (next.isDefined) {
          Allocate(next.get)
        }
      }

      case CFGBlock(label, statements, next, _) => {
        //println(label) //DEBUG
        for (statement <- statements) {
          statement match {
            case field: FieldDeclaration => allocateDecl(field)
            case assign: AssignStatement => {
              allocateExpr(assign.loc)
              allocateExpr(assign.value)
            }
            case compAsg: CompoundAssignStatement => {
              allocateExpr(compAsg.loc)
              allocateExpr(compAsg.value)
            }
            case inc: Increment => {
              allocateExpr(inc.loc)
            }
            case dec: Decrement => {
              allocateExpr(dec.loc)
            }

            case expr: Expression => allocateExpr(expr)
            case ret: Return => {
              if (ret.value.isDefined)
                allocateExpr(ret.value.get)
            }
            case _ => throw new NotImplementedError()
          }
        }

        if (next.isDefined)
            Allocate(next.get)
      }

      case CFGConditional(_, condition, next, ifFalse, _, _) => {
         allocateExpr(condition)
        // condition.eval.get match {
        //   case location: Location => {
        //     assert(location.field.isDefined)
        //     allocateDecl(location.field.get)
        //   }
        //   case literal: Literal =>
        //   case _ => throw new NotImplementedError()
        // }
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
