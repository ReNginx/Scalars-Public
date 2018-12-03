package optimization

import scala.collection.mutable.{ArrayBuffer, Map, Set}
import ir.components._
import codegen._

object CSE extends Optimization {

  override def toString(): String = "LocalCSE"

  val localTmpSuffix = "_cse_local_tmp"
  val var2Val: Map[SingleExpr, SymVal] = Map[SingleExpr, SymVal]()
  // operator, operand1, Some(operand2)
  val exp2ValTmp: Map[Tuple3[String, SymVal, Option[SymVal]], Tuple2[SymVal, Location]] = Map[Tuple3[String, SymVal, Option[SymVal]], Tuple2[SymVal, Location]]()
  // ary2Idx maps array to viable constant indicies
  val ary2Idx: Map[ArrayDeclaration, Set[Long]] = Map[ArrayDeclaration, Set[Long]]()
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
    val retTmp: Location = Location(0, 0, s"${numTmp}_${block.label}${localTmpSuffix}", None, Some(retField))
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

  private def ary2IdxAdd(ary: Location): Unit = {
    assert(ary.index.isDefined)
    ary.index.get match {
      case intLit: IntLiteral => {
        if (ary2Idx.contains(ary.field.get.asInstanceOf[ArrayDeclaration])) {
          ary2Idx(ary.field.get.asInstanceOf[ArrayDeclaration]) += intLit.value
        } else {
          ary2Idx += (ary.field.get.asInstanceOf[ArrayDeclaration] -> Set[Long](intLit.value))
        }
      }
      case _ =>
    }
  }

  private def ary2IdxClear(ary: Location): Unit = {
    assert(ary.index.isDefined)
    if (ary2Idx.contains(ary.field.get.asInstanceOf[ArrayDeclaration])) {
      ary2Idx.remove(ary.field.get.asInstanceOf[ArrayDeclaration])
    }
  }

  private def ary2IdxGet(ary: Location): Set[Long] = {
    assert(ary.index.isDefined)
    if (ary2Idx.contains(ary.field.get.asInstanceOf[ArrayDeclaration])) {
      ary2Idx(ary.field.get.asInstanceOf[ArrayDeclaration])
    } else {
      Set[Long]()
    }
  }

  private def ary2IdxVerify(ary: Location): Boolean = {
    assert(ary.index.isDefined)
    val idxSet: Set[Long] = ary2IdxGet(ary)
    if (ary.index.get.isInstanceOf[IntLiteral] && idxSet.contains(ary.index.get.asInstanceOf[IntLiteral].value)) {
      true
    } else {
      false
    }
  }

  def apply(cfg: CFG, isInit: Boolean=true): Unit = {
    if (isInit) { init }
    var2Val.clear()
    exp2ValTmp.clear()
    ary2Idx.clear()
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
                    val elimFlag: Boolean = (
                      !unary.expression.isInstanceOf[Location] || // not a location
                      unary.expression.asInstanceOf[Location].index.isEmpty || // not an array
                      ary2IdxVerify(unary.expression.asInstanceOf[Location]) // verified array
                    )
                    // query exp2ValTmp
                    val exp2ValTmpRet: Option[(SymVal, Location)] = exp2ValTmpGet(unary, operVal, None)
                    if (!exp2ValTmpRet.isEmpty && elimFlag) {
                      val (retVal: SymVal, retLoc: Location) = exp2ValTmpRet.get
                      // perform elimination
                      newStatements += AssignStatement(0, 0, unary.eval.get, retLoc)
                      setChanged
                    } else {
                      val (newVal: SymVal, newLoc: Location) = exp2ValTmpUpdate(block, unary, operVal, None)
                      newStatements += statement
                      newStatements += AssignStatement(0, 0, newLoc, unary.eval.get)
                      var2ValUpdate(unary.eval.get)
                    }
                    // if operand is an array, add it to ary2Idx
                    operand match {
                      case loc: Location => {
                        if (!loc.index.isEmpty) {
                          ary2IdxAdd(loc)
                        }
                      }
                      case _ =>
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
                    val elimFlag: Boolean = (
                      (!binary.lhs.isInstanceOf[Location] || binary.lhs.asInstanceOf[Location].index.isEmpty || ary2IdxVerify(binary.lhs.asInstanceOf[Location])) &&
                      (!binary.rhs.isInstanceOf[Location] || binary.rhs.asInstanceOf[Location].index.isEmpty || ary2IdxVerify(binary.rhs.asInstanceOf[Location]))
                    )
                    val exp2ValTmpRet: Option[(SymVal, Location)] = exp2ValTmpGet(binary, lhsVal, Some(rhsVal))
                    if (!exp2ValTmpRet.isEmpty && elimFlag) {
                      val (retVal: SymVal, retLoc: Location) = exp2ValTmpRet.get
                      // perform elimination
                      setChanged
                      newStatements += AssignStatement(0, 0, binary.eval.get, retLoc)
                    } else {
                      val (newVal: SymVal, newLoc: Location) = exp2ValTmpUpdate(block, binary, lhsVal, Some(rhsVal))
                      newStatements += statement
                      newStatements += AssignStatement(0, 0, newLoc, binary.eval.get)
                      var2ValUpdate(binary.eval.get)
                    }
                    // if operand is an array, add it to ary2Idx
                    lhs match {
                      case loc: Location => {
                        if (!loc.index.isEmpty) {
                          ary2IdxAdd(loc)
                        }
                      }
                      case _ =>
                    }
                    rhs match {
                      case loc: Location => {
                        if (!loc.index.isEmpty) {
                          ary2IdxAdd(loc)
                        }
                      }
                      case _ =>
                    }
                  }
                }
              }
              case assign: AssignmentStatements => { // create new val for variable
                if (assign.loc.index.isDefined && assign.loc.index.get.isInstanceOf[Location]) {
                  ary2IdxClear(assign.loc)
                }
                var2ValUpdate(assign.loc)
                newStatements += statement
              }
              case inc: Increment => {
                if (inc.loc.index.isDefined && inc.loc.index.get.isInstanceOf[Location]) {
                  ary2IdxClear(inc.loc)
                }
                var2ValUpdate(inc.loc)
                newStatements += statement
              }
              case dec: Decrement => {
                if (dec.loc.index.isDefined && dec.loc.index.get.isInstanceOf[Location]) {
                  ary2IdxClear(dec.loc)
                }
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
