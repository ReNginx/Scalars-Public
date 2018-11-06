package optimization

import scala.collection.mutable.{ArrayBuffer, Map}
import ir.components._
import codegen._

object CSE extends Optimization {

  val var2Val: Map[SingleExpr, SymVal] = Map[SingleExpr, SymVal]()
  // operator, operand1, Some(operand2)
  val exp2ValTmp: Map[Tuple3[String, SymVal, Option[SymVal]], Tuple2[SymVal, Location]] = Map[Tuple3[String, SymVal, Option[SymVal]], Tuple2[SymVal, Location]]()
  var nextVacantVal: SymVal = SymVal(0)
  var nextVacantTmp: Int = 0
  val localTmpSuffix = "_cse_local_tmp"

  private def getNextVal(): SymVal = {
    val retVal: SymVal = nextVacantVal
    nextVacantVal = SymVal(nextVacantVal.value + 1)
    retVal
  }

  private def getNextTmp(typ: Type): Location = {
    val numTmp: Int = nextVacantTmp
    nextVacantTmp += 1
    val retField: FieldDeclaration = VariableDeclaration(0, 0, s"${numTmp}_cse_local_tmp", Some(typ))
    val retTmp: Location = Location(0, 0, s"${numTmp}_cse_local_tmp", None, Some(retField))
    retTmp
  }

  private def getOperString(oper: Operation): String = {
    val operString = oper match {
      case not: Not => "Not"
      case negate: Negate => "Negate"
      case arith: ArithmeticOperation => "Arithmetic" + arith.operator
      case logic: LogicalOperation => "Logical" + logic.operator
    }
    operString
  }

  def var2ValUpdate(expr: SingleExpr): SymVal = { // expr must be location or literal
    val retVal: SymVal = getNextVal
    var2Val += (expr -> retVal)
    retVal
  }

  def var2ValGet(expr: SingleExpr): Option[SymVal] = {
    if (var2Val.contains(expr)) {
      Some(var2Val(expr))
    } else None
  }
  
  // CSE only targets statements with operations, not direct assignments
  def exp2ValTmpUpdate(oper: Operation, op1: SymVal, op2: Option[SymVal] = None): (SymVal, Location) = {
    // Sanity check
    oper match {
      case unary: UnaryOperation => {
        assert(op2.isEmpty)
      }
      case binary: BinaryOperation => {
        assert(!op2.isEmpty)
      }
      case ternary: TernaryOperation => { // ternary operations should have become conditionals
        throw new NotImplementedError()
      }
    }
    val typ: Type = oper.eval.get.field.get.typ.get // type depends on the type of oper.eval
    val retVal: SymVal = getNextVal
    val retTmp: Location = getNextTmp(typ)
    val operString = getOperString(oper)
    exp2ValTmp += (Tuple3(operString, op1, op2) -> Tuple2(retVal, retTmp))

    (retVal, retTmp)
  }

  def exp2ValTmpGet(oper: Operation, op1: SymVal, op2: Option[SymVal] = None): Option[(SymVal, Location)] = {
    // Sanity check
    oper match {
      case unary: UnaryOperation => {
        assert(op2.isEmpty)
      }
      case binary: BinaryOperation => {
        assert(!op2.isEmpty)
      }
      case ternary: TernaryOperation => { // ternary operations should have become conditionals
        throw new NotImplementedError()
      }
    }
    val operString = getOperString(oper)
    if (exp2ValTmp.contains(Tuple3(operString, op1, op2))) {
      return Some(exp2ValTmp(Tuple3(operString, op1, op2)))
    } else {
      return None
    }
  }

  def apply(cfg: CFG): Unit = {
    if (!cfg.isOptimized(CSE)) {
      cfg.setOptimized(CSE)
      cfg match {
        case virtualCFG: VirtualCFG => {
          if (!virtualCFG.next.isEmpty) {
            CSE(virtualCFG.next.get)
          }
        }
  
        case block: CFGBlock => { // only optimize for block
          if (!block.next.isEmpty) {
            CSE(block.next.get)
          }
          val newStatements: ArrayBuffer[IR] = ArrayBuffer[IR]()
          for (statement <- block.statements) {
            statement match {
              case oper: Operation => { // only optimize for operation
                oper match {
                  case unary: UnaryOperation => {
                    val operand = unary.expression.asInstanceOf[SingleExpr]
                    // get val for operand
                    var operVal: SymVal = SymVal(-1)
                    if (!var2ValGet(operand).isEmpty) {
                      operVal = var2ValGet(operand).get
                    } else {
                      operVal = var2ValUpdate(operand)
                    }
                    // query exp2ValTmp
                    val exp2ValTmpRet: Option[(SymVal, Location)] = exp2ValTmpGet(unary, operVal, None)
                    if (!exp2ValTmpRet.isEmpty) {
                      val (retVal: SymVal, retLoc: Location) = exp2ValTmpRet.get
                      // perform elimination
                      newStatements += AssignStatement(0, 0, unary.eval.get, retLoc)
                    } else {
                      val (newVal: SymVal, newLoc: Location) = exp2ValTmpUpdate(unary, operVal, None)
                      newStatements += statement
                      newStatements += AssignStatement(0, 0, newLoc, unary.eval.get)
                      var2ValUpdate(unary.eval.get)
                    }
                  }
                  case binary: BinaryOperation => {
                    val lhs = binary.lhs.asInstanceOf[SingleExpr]
                    val rhs = binary.rhs.asInstanceOf[SingleExpr]
                    var lhsVal: SymVal = SymVal(-1)
                    var rhsVal: SymVal = SymVal(-1)
                    if (!var2ValGet(lhs).isEmpty) {
                      lhsVal = var2ValGet(lhs).get
                    } else {
                      lhsVal = var2ValUpdate(lhs)
                    }
                    if (!var2ValGet(rhs).isEmpty) {
                      rhsVal = var2ValGet(rhs).get
                    } else {
                      rhsVal = var2ValUpdate(rhs)
                    }
                    val exp2ValTmpRet: Option[(SymVal, Location)] = exp2ValTmpGet(binary, lhsVal, Some(rhsVal))
                    if (!exp2ValTmpRet.isEmpty) {
                      val (retVal: SymVal, retLoc: Location) = exp2ValTmpRet.get
                      // perform elimination
                      newStatements += AssignStatement(0, 0, binary.eval.get, retLoc)
                    } else {
                      val (newVal: SymVal, newLoc: Location) = exp2ValTmpUpdate(binary, lhsVal, Some(rhsVal))
                      newStatements += statement
                      newStatements += AssignStatement(0, 0, newLoc, binary.eval.get)
                      var2ValUpdate(binary.eval.get)
                    }
                  }
                }
              }
              case _ => {
                newStatements += statement
              }
            }
          }
          block.statements.clear
          block.statements ++= newStatements
        }
  
        case conditional: CFGConditional => {
          if (!conditional.end.isEmpty) {
            CSE(conditional.end.get)
          }
          if (!conditional.next.isEmpty) {
            CSE(conditional.next.get)
          }
          if (!conditional.ifFalse.isEmpty) {
            CSE(conditional.ifFalse.get)
          }
        }

        case method: CFGMethod => {
          if (!method.block.isEmpty) {
            CSE(method.block.get)
          }
        }

        case call: CFGMethodCall => {
          if (!call.next.isEmpty) {
            CSE(call.next.get)
          }
        }

        case program: CFGProgram => {
          program.methods foreach { CSE(_) }
        }
      }
    }
  }

}
