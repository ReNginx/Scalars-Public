package codegen

import scala.collection.immutable.Set

import ir.components._

/** Control Flow Graph, generalized.
 */
trait CFG {
  def parent: Set[CFG]
}

/** Basic Block in Control Flow Graph, which does not contain conditional statements.
 *
 * This block represents a series of 3-address statements, WITHOUT conditional statements.
 * Since there is no conditional statment in this block, there is only one outgoing arrow
 * to the next block, represented by `next`.
 */
case class CFGBlock(
  statements: Vector[IR],
  var parent: Set[CFG],
  next: CFG) extends CFG

/** Basic Block in Control Flow Graph, which represents a single conditional statent.
 *
 * Conceptually, this block only contains a single conditional statement, flattened into
 * a series of 3-address statements. If the conditional statement evaluates to true, the next
 * basic block becomes `ifTrue`, and `ifFalse` otherwise.
 */
case class CFGConditionalBlock(
  conditional: Vector[IR],
  var parent: Set[CFG],
  ifTrue: CFG,
  ifFalse: CFG) extends CFG

/** Convert an IR to CFG.
 */
object FlatIRToCFG {
  def apply(ir: IR): CFG = {


    throw new NotImplementedError
  }
}
