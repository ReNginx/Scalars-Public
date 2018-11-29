package optimization

import scala.collection.mutable.{ArrayBuffer, Map}
import ir.components._
import codegen._

// CSE is not idempotoent, so setChanged is never used here.
object CSE extends Optimization {

  val localTmpSuffix = "_cse_local_tmp"
  val var2Val: Map[SingleExpr, SymVal] = Map[SingleExpr, SymVal]()
  // operator, operand1, Some(operand2)
  val exp2ValTmp: Map[Tuple3[String, SymVal, Option[SymVal]], Tuple2[SymVal, Location]] = Map[Tuple3[String, SymVal, Option[SymVal]], Tuple2[SymVal, Location]]()
  // idx2Ary maps an index to a set of arrays indexed by such index
  val idx2Ary: Map[SingleExpr, Set[Location]] = Map[SingleExpr, Set[Location]]()
  var nextVacantVal: SymVal = SymVal(0)
  var nextVacantTmp: Int = 0

  private def getNextVal(): SymVal = {
    val retVal: SymVal = nextVacantVal
    nextVacantVal = SymVal(nextVacantVal.value + 1)
    retVal
  }

  private def getNextTmp(block: CFGBlock, typ: Type): Location = {
    val numTmp: Int = nextVacantTmp
    nextVacantTmp += 1
    val retField: FieldDeclaration = VariableDeclaration(0, 0, s"${numTmp}_${block.label}${localTmpSuffix}", Some(typ))
    val retTmp: Location = Location(0, 0, s"${numTmp}_${block.label}localTmpSuffix", None, Some(retField))
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

  def var2ValRemove(loc: Location): Unit = { // only useful for arrays
    if (var2Val.contains(loc)) {
      var2Val.remove(loc)
    }
  }

  def var2ValGet(expr: SingleExpr): Option[SymVal] = {
    if (var2Val.contains(expr)) {
      Some(var2Val(expr))
    } else None
  }
  
  // CSE only targets statements with operations, not direct assignments
  def exp2ValTmpUpdate(block: CFGBlock, oper: Operation, op1: SymVal, op2: Option[SymVal] = None): (SymVal, Location) = {
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
    val retTmp: Location = getNextTmp(block, typ)
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

  def idx2AryAdd(idx: SingleExpr, ary: Location): Set[Location] = {
    if (idx2Ary.contains(idx)) {
      idx2Ary(idx) += ary
    } else {
      idx2Ary += (idx -> Set[Location](ary))
    }
    idx2Ary(idx)
  }

  def idx2AryRemove(idx: SingleExpr): Unit = {
    // println(s"Query: ${idx}")
    // println(s"Result: ${idx2Ary.get(idx)}")
    if (idx2Ary.contains(idx)) {
      idx2Ary.remove(idx)
    }
  }

  // Returns idx2Ary(idx) or empty set
  def idx2AryGet(idx: SingleExpr): Set[Location] = {
    if (idx2Ary.contains(idx)) {
      idx2Ary(idx)
    } else {
      Set[Location]()
    }
  }

  def apply(cfg: CFG, isInit: Boolean=true): Unit = {
    if (isInit) { init }
    var2Val.clear()
    exp2ValTmp.clear()
    idx2Ary.clear()
    nextVacantVal = SymVal(0)
    nextVacantTmp = 0

    if (!cfg.isOptimized(CSE)) {
      cfg.setOptimized(CSE)
      cfg match {
        case virtualCFG: VirtualCFG => {
          if (!virtualCFG.next.isEmpty) {
            CSE(virtualCFG.next.get, false)
          }
        }
  
        case block: CFGBlock => { // only optimize for block
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
                    // if operand is an array, add it to idx2Ary
                    operand match {
                      case loc: Location => {
                        if (!loc.index.isEmpty) {
                          idx2AryAdd(loc.index.get.asInstanceOf[SingleExpr], loc)
                        }
                      }
                      case _ =>
                    }
                    // query exp2ValTmp
                    val exp2ValTmpRet: Option[(SymVal, Location)] = exp2ValTmpGet(unary, operVal, None)
                    if (!exp2ValTmpRet.isEmpty) {
                      val (retVal: SymVal, retLoc: Location) = exp2ValTmpRet.get
                      // perform elimination
                      newStatements += AssignStatement(0, 0, unary.eval.get, retLoc)
                    } else {
                      val (newVal: SymVal, newLoc: Location) = exp2ValTmpUpdate(block, unary, operVal, None)
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
                      // println(s"Retrieve ${lhs}: ${lhsVal}")
                    } else {
                      lhsVal = var2ValUpdate(lhs)
                      // println(s"Update ${lhs}: ${lhsVal}")
                    }
                    if (!var2ValGet(rhs).isEmpty) {
                      rhsVal = var2ValGet(rhs).get
                      // println(s"Retrieve ${rhs}: ${rhsVal}")
                    } else {
                      rhsVal = var2ValUpdate(rhs)
                      // println(s"Update ${rhs}: ${rhsVal}")
                    }
                    // if operand is an array, add it to idx2Ary
                    lhs match {
                      case loc: Location => {
                        if (!loc.index.isEmpty) {
                          idx2AryAdd(loc.index.get.asInstanceOf[SingleExpr], loc)
                        }
                      }
                      case _ =>
                    }
                    rhs match {
                      case loc: Location => {
                        if (!loc.index.isEmpty) {
                          idx2AryAdd(loc.index.get.asInstanceOf[SingleExpr], loc)
                        }
                      }
                      case _ =>
                    }
                    val exp2ValTmpRet: Option[(SymVal, Location)] = exp2ValTmpGet(binary, lhsVal, Some(rhsVal))
                    if (!exp2ValTmpRet.isEmpty) {
                      val (retVal: SymVal, retLoc: Location) = exp2ValTmpRet.get
                      // perform elimination
                      // println(s"Binary hit: ${binary}, ${retVal}, ${retLoc}")
                      newStatements += AssignStatement(0, 0, binary.eval.get, retLoc)
                    } else {
                      val (newVal: SymVal, newLoc: Location) = exp2ValTmpUpdate(block, binary, lhsVal, Some(rhsVal))
                      newStatements += statement
                      newStatements += AssignStatement(0, 0, newLoc, binary.eval.get)
                      var2ValUpdate(binary.eval.get)
                    }
                  }
                }
              }
              case assign: AssignmentStatements => { // create new val for variable
                val arySet: Set[Location] = idx2AryGet(assign.loc) // array fix
                arySet foreach { var2ValRemove(_) }
                idx2AryRemove(assign.loc)
                var2ValUpdate(assign.loc)
                newStatements += statement
              }
              case inc: Increment => {
                val arySet: Set[Location] = idx2AryGet(inc.loc) // array fix
                arySet foreach { var2ValRemove(_) }
                idx2AryRemove(inc.loc)
                var2ValUpdate(inc.loc)
                newStatements += statement
              }
              case dec: Decrement => {
                val arySet: Set[Location] = idx2AryGet(dec.loc) // array fix
                arySet foreach { var2ValRemove(_) }
                idx2AryRemove(dec.loc)
                var2ValUpdate(dec.loc)
                newStatements += statement
              }
              case _ => {
                newStatements += statement
              }
            }
          }
          block.statements.clear
          block.statements ++= newStatements

          if (!block.next.isEmpty) {
            CSE(block.next.get, false)
          }
        }
  
        case conditional: CFGConditional => {
          if (!conditional.next.isEmpty) {
            CSE(conditional.next.get, false)
          }
          if (!conditional.ifFalse.isEmpty) {
            CSE(conditional.ifFalse.get, false)
          }
          if (!conditional.end.isEmpty) {
            CSE(conditional.end.get, false)
          }
        }

        case method: CFGMethod => {
          if (!method.block.isEmpty) {
            CSE(method.block.get, false)
          }
        }

        case call: CFGMethodCall => {
          if (!call.next.isEmpty) {
            CSE(call.next.get, false)
          }
        }

        case program: CFGProgram => {
          program.methods foreach { CSE(_, false) }
        }
      }
    }
  }

}
