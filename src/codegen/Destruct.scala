package codegen

import scala.collection.mutable.{Set, HashSet, ListBuffer, Map, HashMap}

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
      loopEnd: Option[CFG] = None,
      methods: Map[String, CFGMethod] = Map()): Tuple2[VirtualCFG, VirtualCFG] = {

    // (start, end) of every basic block in this Block, with third element being the original IR
    // val blocks: ListBuffer[Tuple3[VirtualCFG, VirtualCFG, Option[IR]]] = ListBuffer()

    val blocks = (declarations ++ statements) map {
      s => {
        val (start, end) = Destruct(s, loopStart, loopEnd, methods)
        Tuple3(start, end, s)
      }
    }

    // (0 to blocks.size - 2)
    blocks.zipWithIndex filter {
      case (tuple, index) => index < blocks.size - 1  // all but the last one
    } foreach {
      case ((start, end, ir), index) => {
        val (start2, end2, ir) = blocks(index + 1)
        link(end, start2)

      }
    }

    blocks foreach {
      _ match {
        case (start, end, ir) => {
          ir match {
            case b: Break =>    link(end, loopEnd.get)
            case c: Continue => link(end, loopStart.get)
          }
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
  private def destructIf(
      line: Int,
      col: Int,
      condition: Expression,
      conditionBlock: Option[Block],
      ifTrue: Block,
      ifFalse: Option[Block],
      methods: Map[String, CFGMethod] = Map()): Tuple2[VirtualCFG, VirtualCFG] = {

    // start and end of this if statement
    val (start, end) = createStartEnd(line, col)
    val (ifStart, ifEnd) = Destruct(ifTrue, methods=methods)

    var blockIfFalse: Option[CFG] = None
    if (ifFalse.isDefined) {  // an `else` block exists
      val (elseStart, elseEnd) = Destruct(ifFalse.get, methods=methods)
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
      ifTrue: Block,
      methods: Map[String, CFGMethod] = Map()): Tuple2[VirtualCFG, VirtualCFG] = {

    val (start, end) = createStartEnd(line, col)

    val (initializeStart, initializeEnd) = Destruct(initialize, methods=methods)
    val (updateStart, updateEnd) = Destruct(update, methods=methods)
    val (blockStart, blockEnd) = Destruct(ifTrue, Option(start), Option(end), methods=methods)
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
      ifTrue: Block,
      methods: Map[String, CFGMethod] = Map()): Tuple2[VirtualCFG, VirtualCFG] = {

    val (start, end) = createStartEnd(line, col)

    val (blockStart, blockEnd) = Destruct(ifTrue, Option(start), Option(end), methods=methods)
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
      block: Block,
      methods: Map[String, CFGMethod] = Map()): Tuple2[VirtualCFG, VirtualCFG] = {

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
      typ: Option[Type],
      methods: Map[String, CFGMethod] = Map()): Tuple2[VirtualCFG, VirtualCFG] = {

    val emptyBlock = Block(line, col, Vector(), Vector())
    destructMethodDeclaration(line, col, name, typ, Vector(), emptyBlock)
  }

  private def destructProgram(
      line: Int,
      col: Int,
      imports: Vector[ExtMethodDeclaration],
      fields: Vector[FieldDeclaration],
      methods: Map[String, CFGMethod] = Map()): Tuple2[VirtualCFG, VirtualCFG] = {

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
      case Block(line, col, declarations, statements) =>                       destructBlock(line, col, declarations, statements, loopStart, loopEnd, methods)
      case If(line, col, condition, conditionBlock, ifTrue, ifFalse) =>        destructIf(line, col, condition, conditionBlock, ifTrue, ifFalse, methods)
      case For(line, col, start, condition, conditionBlock, update, ifTrue) => destructFor(line, col, start, condition, conditionBlock, update, ifTrue, methods)
      case While(line, col, condition, conditionBlock, ifTrue) =>              destructWhile(line, col, condition, conditionBlock, ifTrue, methods)
      case LocMethodDeclaration(line, col, name, typ, params, block) =>        destructMethodDeclaration(line, col, name, typ, params, block, methods)
      case ExtMethodDeclaration(line, col, name, typ) =>                       destructImport(line, col, name, typ, methods)
      case Program(line, col, imports, fields, methodVec) =>                     destructProgram(line, col, imports, fields, methods)
      case MethodCall(line, col, name, params, paramBlocks, method) =>         destructMethodCall(line, col, name, params, paramBlocks, method, methods)

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
