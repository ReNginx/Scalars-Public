package codegen

import ir.components._

import scala.collection.mutable.ArrayBuffer

object TranslateIR {
  def apply(ir: IR): Vector[String] = { // assuming here we only have
    val res: ArrayBuffer[String] = ArrayBuffer()
    //println(ir.getClass.toString)

    ir match {
      case assign: AssignStatement => { // assume that
        res ++= assign.loc.indexCheck
        res ++= Helper.outputMov(assign.loc.rep, assign.value.rep)
      }

      case compAsg: CompoundAssignStatement => {
        res ++= Helper.outputMov(compAsg.loc.rep, "%rax")
        compAsg.operator match {
          case Add => {
            res += s"\taddq ${compAsg.value.rep}, %rax"
          }
          case Subtract => {
            res += s"\tsubq ${compAsg.value.rep}, %rax"
          }
        }
        res += s"\tmovq %rax, ${compAsg.value.rep}"
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
          res += s"\tmovq, ${ret.value.get.rep}, %rax)"
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
                res += s"\tidivq ${ari.rhs.rep}"
              }

              case Modulo => {
                res += s"\tidivq ${ari.rhs.rep}"
                res += s"\tmovq %rdx, %rax"
              }

              case Multiply => {
                res += s"\timulq ${ari.rhs.rep}"
              }
            }
          }

          case log: LogicalOperation => {
            res ++= Helper.outputMov(log.lhs.rep, "%rdx")
            res ++= Helper.outputMov(log.rhs.rep, "%rax")

            log.operator match {
              case Equal => {
                res += s"\tcmpq %rax, %rdx"
                res += s"\tsete %al"
              }

              case NotEqual => {
                res += s"\tcmpq %rax, %rdx"
                res += s"\tsetne %al"
              }

              case GreaterThan => {
                res += s"\tcmpq %rax, %rdx"
                res += s"\tsetg %al"
              }

              case GreaterThanOrEqual => {
                res += s"\tcmpq %rax, %rdx"
                res += s"\tsetge %al"
              }

              case LessThan => {
                res += s"\tcmpq %rax, %rdx"
                res += s"\tsetl %al"
              }

              case LessThanOrEqual => {
                res += s"\tcmpq %rax, %rdx"
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
