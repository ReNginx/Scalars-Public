package codegen

import ir.components._

import scala.collection.mutable.ArrayBuffer

object TranslateIR {
  val aryIdxPtr1: String = "%r10" // use %r10 for array indexing
  val aryIdxPtr2: String = "%r11" // use %r11 when both sides are assignment
  
  def apply(ir: IR): Vector[String] = { // assuming here we only have
    val res: ArrayBuffer[String] = ArrayBuffer()
    //println(ir.getClass.toString)

    ir match {
      case assign: AssignStatement => { // assume that
        // index checking
        res ++= assign.loc.indexCheck
        if (assign.value.isInstanceOf[Location]) {
          res ++= assign.value.asInstanceOf[Location].indexCheck
        }
        res ++= Helper.outputMov(assign.value.rep, assign.loc.rep)
      }

      case compAsg: CompoundAssignStatement => {
        res ++= compAsg.loc.indexCheck
        res ++= Helper.outputMov(compAsg.loc.rep, "%rax")
        compAsg.operator match {
          case Add => {
            res += s"\taddq ${compAsg.value.rep}, %rax"
          }
          case Subtract => {
            res += s"\tsubq ${compAsg.value.rep}, %rax"
          }
        }
        res += s"\tmovq %rax, ${compAsg.loc.rep}"
      }

      case inc: Increment => {
        res ++= inc.loc.indexCheck
        res += s"\tincq ${inc.loc.rep}"
      }

      case dec: Decrement => {
        res ++= dec.loc.indexCheck
        res += s"\tdecq ${dec.loc.rep}"
      }

      case ret: Return => {
        if (ret.value.isDefined) {
          res += s"\tmovq ${ret.value.get.rep}, %rax"
        }
        res += s"\tleave"
        res += s"\tret"
      }

      case variable: VariableDeclaration => {
        res += s"${variable.name}:"
        res += s"\t.zero 8"
      }

      case array: ArrayDeclaration => {
        res += s"${array.name}:"
        res += s"\t.zero ${8 * array.length.value}"
      }

      case op: Operation => {
        op match {
          case ury: UnaryOperation => {
            res ++= Helper.outputMov(ury.expression.rep, "%rax")

            ury match {
              case not: Not => {
                res += s"\tnot %rax"
              }

              case neg: Negate => {
                res += s"\tneg %rax"
              }
            }
          }

          case ari: ArithmeticOperation => {
            res ++= Helper.outputMov(ari.lhs.rep, "%rax")

            ari.operator match {
              case Add => {
                res += s"\taddq ${ari.rhs.rep}, %rax"
              }

              case Subtract => {
                res += s"\tsubq ${ari.rhs.rep}, %rax"
              }

              case Divide => {
                res ++= Helper.outputMov(ari.rhs.rep, "%rsi")
                res += s"\tcqto"
                res += s"\tidivq %rsi"
              }

              case Modulo => {
                res ++= Helper.outputMov(ari.rhs.rep, "%rsi")
                res += s"\tcqto"
                res += s"\tidivq %rsi"
                res += s"\tmovq %rdx, %rax"
              }

              case Multiply => {
                res += s"\timul ${ari.rhs.rep}, %rax"
              }
            }
          }

          case log: LogicalOperation => {
            res ++= Helper.outputMov(log.lhs.rep, "%rdx")
            res ++= Helper.outputMov(log.rhs.rep, "%rdi")
            res += s"\txor %rax, %rax"

            log.operator match {
              case Equal => {
                res += s"\tcmpq %rdi, %rdx"
                res += s"\tsete %al"
              }

              case NotEqual => {
                res += s"\tcmpq %rdi, %rdx"
                res += s"\tsetne %al"
              }

              case GreaterThan => {
                res += s"\tcmpq %rdi, %rdx"
                res += s"\tsetg %al"
              }

              case GreaterThanOrEqual => {
                res += s"\tcmpq %rdi, %rdx"
                res += s"\tsetge %al"
              }

              case LessThan => {
                res += s"\tcmpq %rdi, %rdx"
                res += s"\tsetl %al"
              }

              case LessThanOrEqual => {
                res += s"\tcmpq %rdi, %rdx"
                res += s"\tsetle %al"
              }

              case _ => throw new NotImplementedError()
            }

            res += s"\tmovzbl %al, %eax"
          }
        }
        res ++= Helper.outputMov("%rax", op.eval.get.rep)
      }

      case _ => throw new NotImplementedError
    }
    res.toVector
  }
}
