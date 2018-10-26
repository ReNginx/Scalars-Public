package codegen

import scala.collection.mutable.{HashSet, Set}
import scala.collection.immutable.Map

import ir.components._
import ir.PrettyPrint

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
}
/** VirtualCFG, used to represent start and end nodes that do not contain statements.
 */
case class VirtualCFG(
    label: String,
    parents: Set[CFG]=HashSet(),
    var next: Option[CFG] = None) extends CFG

/** Basic Block in Control Flow Graph, which does not contain conditional statements.
 *
 * This block represents a series of 3-address statements, WITHOUT conditional statements.
 * Since there is no conditional statment in this block, there is only one outgoing arrow
 * to the next block, represented by `next`.
 *
 * @param label a string that uniquely identifies this block
 * @param statements
 * @param parent all possible blocks that the program could have been in, prior to this block
 * @param next the next block to go to, after all statements in this block has executed
 */
case class CFGBlock(
    label: String,
    statements: Vector[IR],
    var next: Option[CFG] = None,
    parents: Set[CFG]=HashSet()) extends CFG

/** Basic Block in Control Flow Graph, which represents a single conditional statent.
 *
 * Conceptually, this block only contains a single conditional statement, flattened into
 * a series of 3-address statements. If the conditional statement evaluates to true, the next
 * basic block becomes `ifTrue`, and `ifFalse` otherwise.
 *
 * @param label a string that uniquely identifies this block
 * @param statements the conditional statemnt that will determine where this block goes to next
 * @param parent all possible blocks that the program could have been in, prior to this block
 * @param next the next block to go to, if conditional evaluates to true
 * @param ifFalse the next block to go to, if conditional evaluates to false
 */
case class CFGConditional(
    label: String,
    statements: Vector[IR],
    parents: Set[CFG]=HashSet(),
    var next: Option[CFG] = None,
    var ifFalse: Option[CFG] = None,
    var end: Option[CFG] = None) extends CFG

/** Basic Block in Control Flow Graph, which represents a method declaration.
 *
 * @param label a string that uniquely identifies this block
 * @param block control flow graph block, corresponding to the body of the method
 * @param params parameters of this method
 * @param next not used
 * @param parents the basic block where this method was declared
 * @param typ return type
 */
case class CFGMethod(
    label: String,
    var block: Option[CFG],  // for preventing recursion
    params: Vector[IR],
    method: LocMethodDeclaration,
    var next: Option[CFG] = None,  // no meaning
    parents: Set[CFG] = Set(),
    typ: Option[Type] = None) extends CFG { // where declared
      var spaceAllocated: Int = 0;
    }

// `next` simply points to
case class CFGMethodCall(
    label: String,
    params: Vector[IR],   // TODO Vector[IR], where IR can be either Literal or Location
    declaration: CFGMethod,
    var next: Option[CFG] = None,  // points to start of declration
    parents: Set[CFG] = Set()) extends CFG  // where it was called

/** Basic Block in Control Flow Graph, which represents a program.
 */
case class CFGProgram(
  label: String,
  imports: Vector[IR],
  fields: CFGBlock,
  methods: Map[String, CFGMethod],  // TODO change to vector
  var next: Option[CFG] = None,  // not used
  parents: Set[CFG] = Set()) extends CFG  // not used
