package codegen

import scala.collection.immutable.Set

import ir.components._
import ir.PrettyPrint

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
 *
 * @param statements
 * @param parent all possible blocks that the program could have been in, prior to this block
 * @param next the next block to go to, after all statements in this block has executed
 */
case class CFGBlock(statements: Vector[IR], var parent: Set[CFG], next: CFG) extends CFG

/** Basic Block in Control Flow Graph, which represents a single conditional statent.
 *
 * Conceptually, this block only contains a single conditional statement, flattened into
 * a series of 3-address statements. If the conditional statement evaluates to true, the next
 * basic block becomes `ifTrue`, and `ifFalse` otherwise.
 *
 * @param conditional the conditional statemnt that will determine where this block goes to next
 * @param parent all possible blocks that the program could have been in, prior to this block
 * @param ifTrue the next block to go to, if conditional evaluates to true
 * @param ifFalse the next block to go to, if conditional evaluates to false
 */
case class CFGConditionalBlock(conditional: Vector[IR], var parent: Set[CFG], ifTrue: CFG, ifFalse: CFG) extends CFG

/** Basic Block in Control Flow Graph, which represents a method declaration.
 *
 * @param params parameters of this method
 * @param parent the basic block where this method was declared
 */
case class CFGMethod(params: Vector[IR], parent: Set[CFG]) extends CFG

/** Convert an IR to CFG.
 */
object FlatIRToCFG {
  def apply(ir: IR, parent: Option[CFG] = None): CFG = {
    PrettyPrint(ir)

    // Program
    ir match {

      case Program(line, col, imports, fields, methods) => {
        // irModified = ir.asInstanceOf[Program].copy(
        //   imports = imports.map(IRto3Addr(_, iter)).asInstanceOf[Vector[ExtMethodDeclaration]],
        //   fields = fields.map(IRto3Addr(_, iter)).asInstanceOf[Vector[FieldDeclaration]],
        //   methods = methods.map(IRto3Addr(_, iter)).asInstanceOf[Vector[LocMethodDeclaration]]
        // )
  	  }
      // case If(line, col, condition, conditionBlock, ifTrue, ifFalse) => new NotImplementedError
      // case For(line, col, start, condition, conditionBlock, update, ifTrue) => new NotImplementedError
      // case While(line, col, condition, conditionBlock, ifTrue) => new NotImplementedError

      case _ => {
        println("Undefined for IRto3Addr!")
        new IllegalArgumentException
      }

      // case AssignStatement(line, col, loc, value, valueBlock) => new NotImplementedError
      // case CompoundAssignStatement(line, col, loc, value, valueBlock, operator) => new NotImplementedError
      // case Increment(line, col, loc) => new NotImplementedError
      // case Decrement(line, col, loc) => new NotImplementedError
      //
      // // Call
      // case MethodCall(line, col, name, params, paramBlocks, method) => new NotImplementedError
      //
      // // Expression
      // case Length(line, col, location) => new NotImplementedError
      // case Location(line, col, name, index, indexBlock, field) => new NotImplementedError
      //
      // // FieldDeclaration
      // case FieldList(line, col, typ, declarations) => new NotImplementedError
      // case VariableDeclaration(line, col, name, typ) => new NotImplementedError
      // case ArrayDeclaration(line, col, name, length, typ) => new NotImplementedError
      //
      // // Literal
      // case IntLiteral(line, col, value) => new NotImplementedError
      // case BoolLiteral(line, col, value) => new NotImplementedError
      // case CharLiteral(line, col, value) => new NotImplementedError
      // case StringLiteral(line, col, value) => new NotImplementedError
      //
      // // LogicalOperator
      //
      // // Loop
      // case For(line, col, start, condition, conditionBlock, update, ifTrue) => new NotImplementedError
      // case While(line, col, condition, conditionBlock, ifTrue) => new NotImplementedError
      //
      // // MethodDeclaration
      //
      // case LocMethodDeclaration(line, col, name, typ, params, block) => new NotImplementedError
      // case ExtMethodDeclaration(line, col, name, typ) => new NotImplementedError
      // case Not(line, col, expression) => new NotImplementedError
      // case Negate(line, col, expression) => new NotImplementedError
      // case ArithmeticOperation(line, col, operator, lhs, rhs) => new NotImplementedError
      // case LogicalOperation(line, col, operator, lhs, rhs) => new NotImplementedError
      // case TernaryOperation(line, col, condition, ifTrue, ifFalse) => new NotImplementedError
      // case Program(line, col, imports, fields, methods) => new NotImplementedError
      // case Block(line, col, declarations, statements) => new NotImplementedError
      // case Break(line, col, loop) => new NotImplementedError
      // case Continue(line, col, loop) => new NotImplementedError
      // case Return(line, col, value, valueBlock) => new NotImplementedError
      // case If(line, col, condition, conditionBlock, ifTrue, ifFalse) => new NotImplementedError
    }


    throw new NotImplementedError
  }
}
