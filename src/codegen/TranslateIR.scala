package codegen

import scala.collection.mutable.{HashSet, Set, ArrayBuffer}
import scala.collection.immutable.Map

import ir.components._
import ir.PrettyPrint

object TranslateIR {
  def apply(ir: IR): Vector[String] = { // assuming here we only have
    val res: ArrayBuffer[String] = ArrayBuffer()
    ir match {
      case assign: AssignStatement => { // assume that
        res ++= assign.loc.indexCheck
        assign.value match {
          case op: Operation => {
            op match {
              case ury: UnaryOperation => {
                res += s"movq ${ury.eval.get.rep}, %rax"

                ury match {
                  case not: Not => {
                    res += s"not %rax"
                  }

                  case neg: Negate => {
                    res += s"neg %rax"
                  }
                }
              }
              case ari: ArithmeticOperation => {
                res += s"movq ${ari.lhs.rep}, %rax"

                ari.operator match {
                  case Add => {
                    res += s"addq ${ari.rhs.rep}, %rax"
                  }

                  case Subtract => {
                    res += s"subq ${ari.rhs.rep}, %rax"
                  }

                  case Divide => {
                    res += s"idivq ${ari.rhs.rep}"
                  }

                  case Modulo => {
                    res += s"idivq ${ari.rhs.rep}"
                    res += s"movq %rdx, %rax"
                  }

                  case Multiply => {
                    res += s"imulq ${ari.rhs.rep}"
                  }
                }
              }

              case log: LogicalOperation => {
                res += s"movq ${log.lhs.rep}, %rdx"
                res += s"movq ${log.rhs.rep}, %rax"

                log.operator match {
                  case And => {
                    //TODO
                  }
                  case Or => {
                    // TODO
                  }

                  case Equal => {
                    res += s"cmpq %rax, %rdx"
                    res += s"sete %al"
                  }

                  case NotEqual => {
                    res += s"cmpq %rax, %rdx"
                    res += s"setne %al"
                  }

                  case GreaterThan => {
                    res += s"cmpq %rax, %rdx"
                    res += s"setg %al"
                  }

                  case GreaterThanOrEqual => {
                    res += s"cmpq %rax, %rdx"
                    res += s"setge %al"
                  }

                  case LessThan => {
                    res += s"cmpq %rax, %rdx"
                    res += s"setl %al"
                  }

                  case LessThanOrEqual => {
                    res += s"cmpq %rax, %rdx"
                    res += s"setle %al"
                  }
                }

                res += s"movzbl %al, %eax"
              }
            }
          }

          case loc: Location => {
            res += s"movq %rax, ${loc.rep}"
          }
          case lit: Literal => {
            res += s"movq %rax, ${lit.rep}"
          }
        }

        res += s"movq %rax, ${assign.loc.rep}"
      }

      case compAsg: CompoundAssignStatement => {
        res += s"movq ${compAsg.loc.rep}, %rax"
        compAsg.operator match {
          case Add => {
            res += s"addq ${compAsg.value.rep}, %rax"
          }
          case Subtract => {
            res += s"subq ${compAsg.value.rep}, %rax"
          }
        }
        res += s"movq %rax, ${compAsg.value.rep}"
      }

      case inc: Increment => {
        res ++= inc.loc.indexCheck
        res += s"incq ${inc.loc.rep}"
      }

      case dec: Decrement => {
        res ++= dec.loc.indexCheck
        res += s"decq ${dec.loc.rep}"
      }

      case ret: Return => {
        if (ret.value.isDefined) {
<<<<<<< HEAD
          res += s"movq, ${ret.value.get.rep}, %rax)"
        }
        res += s"leave"
        res += s"ret"
=======
          ret += s"movq, ${ret.value.get.rep}, %rax)"
        }
        ret += s"leave"
        ret += s"ret"
>>>>>>> 8de44426c48197f20ee60b9048279b047ff39240
      }
    }
    res.toVector
  }
}
