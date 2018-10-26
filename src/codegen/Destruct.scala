package codegen

import scala.collection.mutable.{Set, HashSet, ListBuffer}
import scala.collection.immutable.{Map, HashMap}

import ir.components._
import ir.PrettyPrint

object Destruct {

  /** Link the two basic blocks of CFG.
   *
   * @param source such that source.sink will be set to Option(sink)
   * @param sink such that one of its parents will be source
   */
  private def link(source: CFG, sink: CFG): Unit = {
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
      statements: Vector[IR],
      loopStart: Option[CFG] = None,
      loopEnd: Option[CFG] = None): Tuple2[VirtualCFG, VirtualCFG] = {

    // (start, end) of every basic block in this Block, with third element being the original IR
    val blocks: ListBuffer[Tuple3[VirtualCFG, VirtualCFG, Option[IR]]] = ListBuffer()

    // used to form contiguous series of commands from which to form a basic block
    val contiguousBlock: ListBuffer[IR] = declarations.to[ListBuffer]
    def destructContiguousBlock(): Unit = {
      val contiguous = contiguousBlock map { x => x }  // copy
      contiguousBlock.clear()

      val breakContinues = contiguous.zipWithIndex map {
        z => z match {
          case (_: Break, index) => index
          case (_: Continue, index) => index
          case (_, index) => -1
        }
      } filter { _ >= 0 }

      val (start, end) = createStartEnd(line, col)

      // aggregate everything before the break/continue statement as one single block
      def breakContinueIndex = breakContinues(0)
      val newContiguous = if (breakContinues.size == 0) contiguous else contiguous.slice(0, breakContinueIndex)
      val block = CFGBlock(start.label, newContiguous.toVector)
      link(start, block)
      link(block, end)
      blocks += Tuple3(start, end, None)

      // add break and continue as separate, empty blocks
      if (breakContinues.size > 0) {
        val breakContinue = contiguous(breakContinueIndex)
        val (start, end) = breakContinue match {
          case b: Break =>    createStartEnd(b.line, b.col)
          case c: Continue => createStartEnd(c.line, c.col)
          case _ => throw new IllegalArgumentException
        }
        blocks += Tuple3(start, end, Option(breakContinue))
      }
    }

    statements foreach {
      s => s match {
        // encountered conditional, so destruct the contiguous block, then the conditional
        case _: If | _: For | _: While => {
          if (contiguousBlock.size == 0) {
            destructContiguousBlock()
          }
          val (start, end) = Destruct(s)
          blocks += Tuple3(start, end, None)
        }
        // case m: MethodCall => throw new NotImplementedError
        case _ => contiguousBlock += s
      }
    }

    // link adjacent basic blocks in blocks
    (0 to blocks.size - 2) foreach {
      i => {
        val (start1, end1, ir1) = blocks(i)
        val (start2, end2, ir2) = blocks(i + 1)
        link(end1, start2)
      }
    }

    // after linking, check for breaks and continue
    blocks.zipWithIndex filter {
      _ match {
        case (tuple, index) => tuple._3.isDefined
      }
    } foreach {
      _ match {
        case ((start, end, optStatement), index) => optStatement.get match {
          case b: Break => link(end, loopEnd.get)  // the end of break block pointㄴ to the loop end
          case c: Continue => link(end, loopStart.get)  // point to start of loop
        }
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
  private def blockToConditionalCFG(block: Block, label: String, ifTrue: CFG, ifFalse: CFG, end: CFG): CFGConditional = {
    val statements = block.declarations ++ block.statements
    CFGConditional(label, statements, Set(), Option(ifTrue), Option(ifFalse), Option(end))
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

    val conditionalCFG = blockToConditionalCFG(conditionBlock.get, start.label, ifStart, blockIfFalse.get, end)
    link(start, conditionalCFG)

    (start, end)
  }

  /** Destruct an for loop.
   */
  private def destructFor(  // TODO handle continue and break
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
    val (blockStart, blockEnd) = Destruct(ifTrue, Option(start), Option(end))
    val conditionalCFG = blockToConditionalCFG(conditionBlock.get, start.label, blockStart, end, end)

    link(start, initializeStart)
    link(initializeEnd, updateStart)
    link(updateEnd, conditionalCFG)
    link(blockEnd, updateStart)
    (start, end)
  }

  /** Destruct an while loop.
   */
  private def destructWhile(  // TODO handle continue and break
      line: Int,
      col: Int,
      condition: Expression,
      conditionBlock: Option[Block],
      ifTrue: Block): Tuple2[VirtualCFG, VirtualCFG] = {

    val (start, end) = createStartEnd(line, col)

    val (blockStart, blockEnd) = Destruct(ifTrue, Option(start), Option(end))
    val conditionalCFG = blockToConditionalCFG(conditionBlock.get, start.label, blockStart, end, end)

    link(start, conditionalCFG)
    link(blockEnd, conditionalCFG)
    (start, end)
  }

  private def destructMethodDeclaration(
      line: Int,
      col: Int,
      name: String,
      typ: Option[Type],
      params: Vector[FieldDeclaration],
      block: Block): Tuple2[VirtualCFG, VirtualCFG] = {

    val (start, end) = createStartEnd(line, col)
    val (blockStart, blockEnd) = Destruct(block)

    // hmm, what to do with blockEnd
    val methodCFG = CFGMethod(start.label, blockStart, params)
    link(start, methodCFG)
    link(methodCFG, end)

    (start, end)
  }

  private def destructImport(
      line: Int,
      col: Int,
      name: String,
      typ: Option[Type]): Tuple2[VirtualCFG, VirtualCFG] = {

    val emptyBlock = Block(line, col, Vector(), Vector())
    destructMethodDeclaration(line, col, name, typ, Vector(), emptyBlock)
  }

  private def destructProgram(
      line: Int,
      col: Int,
      imports: Vector[ExtMethodDeclaration],
      fields: Vector[FieldDeclaration],
      methods: Vector[LocMethodDeclaration]): Tuple2[VirtualCFG, VirtualCFG] = {

    throw new NotImplementedError
  }

  private def destructMethodCall(
      line: Int,
      col: Int,
      name: String,
      params: Vector[Expression],
      paramBlocks: Vector[Option[Block]],
      method: Option[MethodDeclaration] = None,
      methods: Map[String, CFGMethod] = Map()): Tuple2[VirtualCFG, VirtualCFG] = {


        // case class CFGMethodCall(
        //     label: String,
        //     params: Vector[IR],
        //     var next: Option[CFG] = None,
        //     parents: Set[CFG] = Set()) extends CFG

    throw new NotImplementedError
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
   * @param methods maps method names to declarations
   */
  def apply(
      ir: IR,
      loopStart: Option[CFG] = None,
      loopEnd: Option[CFG] = None,
      methods: Map[String, CFGMethod] = Map()): Tuple2[VirtualCFG, VirtualCFG] = {

    val middle: Tuple2[VirtualCFG, VirtualCFG] = ir match {
      case Block(line, col, declarations, statements) =>                       destructBlock(line, col, declarations, statements, loopStart, loopEnd)
      case If(line, col, condition, conditionBlock, ifTrue, ifFalse) =>        destructIf(line, col, condition, conditionBlock, ifTrue, ifFalse)
      case For(line, col, start, condition, conditionBlock, update, ifTrue) => destructFor(line, col, start, condition, conditionBlock, update, ifTrue)
      case While(line, col, condition, conditionBlock, ifTrue) =>              destructWhile(line, col, condition, conditionBlock, ifTrue)
      case LocMethodDeclaration(line, col, name, typ, params, block) =>        destructMethodDeclaration(line, col, name, typ, params, block)
      case ExtMethodDeclaration(line, col, name, typ) =>                       destructImport(line, col, name, typ)
      case Program(line, col, imports, fields, methods) =>                     destructProgram(line, col, imports, fields, methods)
      case MethodCall(line, col, name, params, paramBlocks, method) =>         destructMethodCall(line, col, name, params, paramBlocks, method)

      // FIXME case MethodCall(line, col, name, params, paramBlocks, method) => throw new NotImplementedError
      // FIXME don't know what to do with assignments because they are not yet flattened
      // case AssignStatement(line, col, loc, value, valueBlock) => throw new NotImplementedError
      // case CompoundAssignStatement(line, col, loc, value, valueBlock, operator) => throw new NotImplementedError
      // case Increment(line, col, loc) => throw new NotImplementedError
      // case Decrement(line, col, loc) => throw new NotImplementedError
      case _ => throw new NotImplementedError
    }

    throw new NotImplementedError
  }
}
