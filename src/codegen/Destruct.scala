package codegen

import scala.collection.mutable.{HashSet, Set, ListBuffer}
import scala.collection.immutable.{Map}

import ir.components._
import ir.PrettyPrint

object Destruct {

  // returns start, end
  def conditional(block: Block): Tuple2[VirtualCFG, VirtualCFG] = {
    throw new NotImplementedError
  }

  /** Link the two basic blocks of CFG.
   *
   * @param source such that source.sink will be set to Option(sink)
   * @param sink such that one of its parents will be source
   */
  def link(source: CFG, sink: CFG): Unit = {
    source.next = Option(sink)
    sink.parents += source
  }

  /**
   * @param params identical to params of Block
   * @return (start, end) nodes as a result of destructuring this block
   */
  private def destructBlock(
      line: Int,
      col: Int,
      declarations: Vector[IR],
      statements: Vector[IR]): Tuple2[VirtualCFG, VirtualCFG] = {

    // (start, end) of every basic block in this Block
    val blocks: ListBuffer[Tuple2[VirtualCFG, VirtualCFG]] = ListBuffer()

    // used to form contiguous series of commands from which to form a basic block
    val contiguousBlock: ListBuffer[IR] = declarations.to[ListBuffer]
    def destructContiguousBlock(): Unit = {
      val contiguous = contiguousBlock map { x => x }  // copy
      contiguousBlock.clear()

      val (start, end) = createStartEnd(line, col)
      val block = CFGBlock(start.label, contiguous.toVector)

      link(start, block)
      link(block, end)
      blocks += Tuple2(start, end)
    }

    statements foreach {
      statement => statement match {
        // encountered conditional, so destruct the contiguous block, then the conditional
        case _: If | _: For | _: While => {
          if (contiguousBlock.size == 0) {
            destructContiguousBlock()
          }
          blocks += Destruct(statement)
        }
        case m: MethodCall => throw new NotImplementedError
        case _ => contiguousBlock += statement
      }
    }

    // link adjacent basic blocks in blocks
    (0 to blocks.size - 2) foreach {
      i => {
        val (start1, end1) = blocks(i)
        val (start2, end2) = blocks(i + 1)
        link(end1, start2)
      }
    }

    // start of first block, and end of last block
    val firstBlockStart = blocks(0)._1
    val lastBlockEnd = blocks(-1)._2

    val (start, end) = createStartEnd(line, col)
    link(start, firstBlockStart)
    link(lastBlockEnd, end)

    (start, end)
  }

  // parents is set to empty set initially
  private def blockToConditionalCFG(block: Block, label: String, ifTrue: CFG, ifFalse: CFG): CFGConditional = {
    val statements = block.declarations ++ block.statements
    CFGConditional(label, statements, Set(), Option(ifTrue), Option(ifFalse))
  }

  /** Destruct an if/else statement.
   *
   * @param params identical to params of If
   * @return (start, end) whose internal structure is the following:
   *   start - the start node, s.t. start.next points to CFGConditional
   *   end - the end node, s.t. exiting either if/else block leads to this node
   *   CFGConditional - has two blocks:
   *       blockIfTrue - one of its parents is this conditional block
   *       blockIfFalse - one of its parents is this conditional block
   */
  private def destructIf(line: Int,
      col: Int,
      condition: Expression,
      conditionBlock: Option[Block],
      ifTrue: Block,
      ifFalse: Option[Block]): Tuple2[VirtualCFG, VirtualCFG] = {

    // start and end of this if statement
    val (start, end) = createStartEnd(line, col)
    val (ifStart, ifEnd) = Destruct(ifTrue)

    var blockIfFalse: Option[CFG] = None
    if (ifFalse.isDefined) {  // an `else` block exists
      val (elseStart, elseEnd) = Destruct(ifFalse.get)
      blockIfFalse = Option(elseStart)
      link(elseEnd, end)
    } else {  // `else` block does not exist, so blockIfFalse simply points to the end node
      blockIfFalse = Option(end)
      link(ifEnd, end)
    }

    val conditionalCFG = blockToConditionalCFG(conditionBlock.get, start.label, ifStart, blockIfFalse.get)
    link(start, conditionalCFG)

    (start, end)
  }

  /** Destruct an for loop.
   *
   * @param params identical to params of For
   * @return (start, end) whose internal structure is the following:
   *   start - the start node, s.t. start.next points to CFGConditional
   *   end - the end node, s.t. exiting either if/else block leads to this node
   *   CFGConditional - has two blocks:
   *       blockIfTrue - one of its parents is this conditional block
   *       blockIfFalse - one of its parents is this conditional block
   */
  private def destructFor(
      line: Int,
      col: Int,
      initialize: AssignStatement,
      condition: Expression,
      conditionBlock: Option[Block],
      update: Assignment,
      ifTrue: Block): Tuple2[VirtualCFG, VirtualCFG] = {

    val (start, end) = createStartEnd(line, col)

    val (initializeStart, initializeEnd) = Destruct(initialize)
    val (updateStart, updateEnd) = Destruct(update)
    val (blockStart, blockEnd) = Destruct(ifTrue)
    val conditionalCFG = blockToConditionalCFG(conditionBlock.get, start.label, blockStart, end)

    link(start, initializeStart)
    link(initializeEnd, updateStart)
    link(updateEnd, conditionalCFG)
    link(blockEnd, updateStart)
    (start, end)
  }

  /** Destruct an while loop.
   *
   * @param params identical to params of While
   * @return (start, end) whose internal structure is the following:
   *   start - the start node, s.t. start.next points to CFGConditional
   *   end - the end node, s.t. exiting either if/else block leads to this node
   *   CFGConditional - has two blocks:
   *       blockIfTrue - one of its parents is this conditional block
   *       blockIfFalse - one of its parents is this conditional block
   */
  private def destructWhile(
      line: Int,
      col: Int,
      condition: Expression,
      conditionBlock: Option[Block],
      ifTrue: Block): Tuple2[VirtualCFG, VirtualCFG] = {

    val (start, end) = createStartEnd(line, col)

    val (blockStart, blockEnd) = Destruct(ifTrue)
    val conditionalCFG = blockToConditionalCFG(conditionBlock.get, start.label, blockStart, end)

    link(start, conditionalCFG)
    link(blockEnd, conditionalCFG)
    (start, end)
  }

  /**
   * Create two virtual nodes, corresponding to the start and end nodes.
   *
   * @param line
   * @param col
   * @return (startNode, endNode)
   */
  def createStartEnd(line: Int, col: Int): Tuple2[VirtualCFG, VirtualCFG] = {
    val label = s"l${line}c${col}"
    val start = VirtualCFG(s"${label}_start")
    val end = VirtualCFG(s"${label}_end")
    (start, end)
  }

  /** Destructure a given IR and return its start and end nodes.
   * @param ir the flattened IR to destruct
   */
  def apply(ir: IR): Tuple2[VirtualCFG, VirtualCFG] = {

    val middle: Tuple2[VirtualCFG, VirtualCFG] = ir match {
      case Block(line, col, declarations, statements) => destructBlock(line, col, declarations, statements)
      case If(line, col, condition, conditionBlock, ifTrue, ifFalse) => destructIf(line, col, condition, conditionBlock, ifTrue, ifFalse)
      case For(line, col, start, condition, conditionBlock, update, ifTrue) => destructFor(line, col, start, condition, conditionBlock, update, ifTrue)
      case While(line, col, condition, conditionBlock, ifTrue) => destructWhile(line, col, condition, conditionBlock, ifTrue)

      case Program(line, col, imports, fields, methods) => throw new NotImplementedError
      case LocMethodDeclaration(line, col, name, typ, params, block) => throw new NotImplementedError
      case ExtMethodDeclaration(line, col, name, typ) => throw new NotImplementedError
      case MethodCall(line, col, name, params, paramBlocks, method) => throw new NotImplementedError
      case AssignStatement(line, col, loc, value, valueBlock) => throw new NotImplementedError
      case CompoundAssignStatement(line, col, loc, value, valueBlock, operator) => throw new NotImplementedError
      case Increment(line, col, loc) => throw new NotImplementedError
      case Decrement(line, col, loc) => throw new NotImplementedError
      case _ => throw new NotImplementedError
    }

    throw new NotImplementedError
  }
}
