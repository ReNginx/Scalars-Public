package codegen

import scala.collection.mutable.{ArrayBuffer, HashSet, Map, Set}
import ir.components._
import optimization.Optimization

/** Control Flow Graph, generalized.
 *
 * Refer back to the lecture notes about destruct and etc for extra information.
 *
 * Every block is transformed into the following structure:
 *   StartCFG -> statements -> EndCFG
 *
 * When we have multiple such structures, one example resembles the following:
 *   StartCFG -> statements -> EndCFG -> StartCFG -> statements -> EndCFG
 *
 * Notice that there can be arrows coming into StartCFG, but only one arrow can leave
 * EndCFG.
 */
trait CFG {
  def label: String
  def parents: Set[CFG]
  var isTranslated: Boolean = false
  var isAllocated: Boolean = false
  var next: Option[CFG]
  override def hashCode: Int = label.hashCode
  override def equals(obj: Any): Boolean = {
    obj.isInstanceOf[CFG] &&
    obj.hashCode == this.hashCode
  }
  var activeOpti: Set[Optimization] = Set[Optimization]()
  def isOptimized(opt: Optimization): Boolean = {
      activeOpti.contains(opt)
  }
  def setOptimized(opt: Optimization): Unit = {
      activeOpti += opt
  }
  var tmp2Var: Map[Location, SingleExpr] = Map[Location, SingleExpr]()
  var var2Set: Map[SingleExpr, Set[Location]] = Map[SingleExpr, Set[Location]]()
}
/** VirtualCFG, used to represent start and end nodes that do not contain statements.
 */
case class VirtualCFG(
    label: String,
    parents: Set[CFG]=Set(),
    var next: Option[CFG] = None) extends CFG

/** Basic Block in Control Flow Graph, which does not contain conditional statements.
 *
 * This block represents a series of 3-address statements, WITHOUT conditional statements.
 * Since there is no conditional statment in this block, there is only one outgoing arrow
 * to the next block, represented by `next`.
 *
 * @param label a string that uniquely identifies this block
 * @param statements flattened 3addr code. should only be one of the following
  *                   1. 3addr assignment which only contains arithmetic operator
  *                   2. return
 * @param parent all possible blocks that the program could have been in, prior to this block
 * @param next the next block to go to, after all statements in this block has executed
 */
case class CFGBlock(
    label: String,
    statements: ArrayBuffer[IR],
    var next: Option[CFG] = None,
    parents: Set[CFG]=Set()) extends CFG

/** Basic Block in Control Flow Graph, which represents a single conditional statement.
 *
 * Conceptually, this block only contains a single conditional statement, flattened into
 * a series of 3-address statements. If the conditional statement evaluates to true, the next
 * basic block becomes `ifTrue`, and `ifFalse` otherwise.
 *
 * @param label a string that uniquely identifies this block
 * @param condition a literal or a location.
 * @param parent all possible blocks that the program could have been in, prior to this block
 * @param next the next block to go to, if conditional evaluates to true
 * @param ifFalse the next block to go to, if conditional evaluates to false
 */
case class CFGConditional(
    label: String,
    condition: Expression,
    var next: Option[CFG] = None,
    var ifFalse: Option[CFG] = None,
    var end: Option[CFG] = None,
    parents: Set[CFG]=Set()) extends CFG

/** Basic Block in Control Flow Graph, which represents a method declaration.
 *
 * @param label a string that uniquely identifies this block
 * @param block control flow graph block, corresponding to the body of the method
 * @param params parameters of this method, must be a declaration
 * @param next not used
 * @param parents the basic block where this method was declared
 * @param typ return type
 */
case class CFGMethod(
    label: String,
    var block: Option[CFG],  // for preventing recursion
    params: Vector[FieldDeclaration],
    method: MethodDeclaration,
    var next: Option[CFG] = None,  // no meaning
    parents: Set[CFG] = Set()) extends CFG { // where declared
      var spaceAllocated: Int = 0;
    }

/**
  *
  * @param label a string that uniquely identifies this block
  * @param params could either be a location or a literal
  * @param declaration points to where it's defined
  * @param next points to CFG after the call
  * @param parents the basic block where this method was declared
  */
// `next` simply points to
// where IR can be either Literal or Location
case class CFGMethodCall(
    label: String,
    params: Vector[Expression],
    declaration: String,
    var next: Option[CFG] = None,
    parents: Set[CFG] = Set()) extends CFG

/** Basic Block in Control Flow Graph, which represents a program.
 */
case class CFGProgram(
  label: String,
  fields: Vector[FieldDeclaration],
  methods: Vector[CFGMethod],
  var next: Option[CFG] = None,  // not used
  parents: Set[CFG] = Set()) extends CFG  // not used
