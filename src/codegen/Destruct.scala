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

  private def linkAdjacent(vector: Vector[Tuple3[CFG, CFG, IR]]): Unit = {
    vector.zipWithIndex filter {
      case (tuple, index) => index < vector.size - 1  // all but the last one
    } foreach {
      case ((start, end, ir), index) => {
        val (start2, end2, ir) = vector(index + 1)
        link(end, start2)
      }
    }
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
      methods: Map[String, Option[CFGMethod]] = Map()): Tuple3[CFG, CFG, Option[Location]] = {

    val blocks = (declarations ++ statements) map {
      s => {
        val (start, end, _) = Destruct(s, loopStart, loopEnd, methods)
        Tuple3(start, end, s)
      }
    }

    // link adjacent blocks, in the order that they appear
    linkAdjacent(blocks)

    // if we run into `break` or `continue`, redirect elsewhere besides the next block
    blocks foreach {
      case (start, end, ir) => {
        ir match {
          case b: Break =>    link(end, loopEnd.get)
          case call: Continue => link(end, loopStart.get)
        }
      }
    }

    // start of first block, and end of last block
    val firstBlockStart = blocks(0)._1
    val lastBlockEnd = blocks(-1)._2

    val (start, end) = createStartEnd(line, col)
    link(start, firstBlockStart)
    link(lastBlockEnd, end)

    (start, end, None)
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
      methods: Map[String, Option[CFGMethod]] = Map()): Tuple3[CFG, CFG, Option[Location]] = {

    // start and end of this if statement
    val (start, end) = createStartEnd(line, col)
    val (ifStart, ifEnd, _) = Destruct(ifTrue, methods=methods)

    var blockIfFalse: Option[CFG] = None
    if (ifFalse.isDefined) {  // an `else` block exists
      val (elseStart, elseEnd, _) = Destruct(ifFalse.get, methods=methods)
      blockIfFalse = Option(elseStart)
      link(elseEnd, end)
    } else {  // `else` block does not exist, so blockIfFalse simply points to the end node
      blockIfFalse = Option(end)
      link(ifEnd, end)
    }

    val conditionalCFG = blockToConditionalCFG(conditionBlock.get, start.label, ifStart, blockIfFalse.get, end)
    link(start, conditionalCFG)

    (start, end, None)
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
      methods: Map[String, Option[CFGMethod]] = Map()): Tuple3[CFG, CFG, Option[Location]] = {

    val (start, end) = createStartEnd(line, col)

    val (initializeStart, initializeEnd, _) = Destruct(initialize, methods=methods)
    val (updateStart, updateEnd, _) = Destruct(update, methods=methods)
    val (blockStart, blockEnd, _) = Destruct(ifTrue, Option(start), Option(end), methods=methods)
    val conditionalCFG = blockToConditionalCFG(conditionBlock.get, start.label, blockStart, end, end)

    link(start, initializeStart)
    link(initializeEnd, updateStart)
    link(updateEnd, conditionalCFG)
    link(blockEnd, updateStart)
    (start, end, None)
  }

  /** Destruct an while loop.
   */
  private def destructWhile(  // TODO handle continue and break
      line: Int,
      col: Int,
      condition: Expression,
      conditionBlock: Option[Block],
      ifTrue: Block,
      methods: Map[String, Option[CFGMethod]] = Map()): Tuple3[CFG, CFG, Option[Location]] = {

    val (start, end) = createStartEnd(line, col)

    val (blockStart, blockEnd, _) = Destruct(ifTrue, Option(start), Option(end), methods=methods)
    val conditionalCFG = blockToConditionalCFG(conditionBlock.get, start.label, blockStart, end, end)

    link(start, conditionalCFG)
    link(blockEnd, conditionalCFG)
    (start, end, None)
  }

  private def destructMethodDeclaration(
      line: Int,
      col: Int,
      name: String,
      typ: Option[Type],
      params: Vector[FieldDeclaration],
      block: Block,
      methods: Map[String, Option[CFGMethod]] = Map()): Tuple3[CFG, CFG, Option[Location]] = {

    val (start, end) = createStartEnd(line, col)

    // made methodCFG mutable so that when destructing method call, it knows where to point to
    val methodCFG = CFGMethod(start.label, None, params)
    methods += name -> Option(methodCFG)

    val (blockStart, blockEnd, _) = Destruct(block)
    methodCFG.block = Option(blockStart)

    link(start, methodCFG)
    link(methodCFG, end)

    (start, end, None)
  }

  private def destructMethodCall(
      line: Int,
      col: Int,
      name: String,
      params: Vector[Expression],
      paramBlocks: Vector[Option[Block]],
      methods: Map[String, Option[CFGMethod]] = Map()): Tuple3[CFG, CFG, Option[Location]] = {

    val blocks = params map {
      Destruct(_, methods=methods)
    } map {  // just to be compatible with linkAdjacent
      case (start, end, _) => Tuple3(start, end, IntLiteral(0, 0, 0))
    }

    // link each block with each other
    linkAdjacent(blocks)

    val firstBlockStart = blocks(0)._1
    val lastBlockEnd = blocks(-1)._2

    val (start, end) = createStartEnd(line, col)
    val methodDeclaration = (methods get name).get.get

    // locations or literals of each parameters
    val parameters = params map {
      p => p match {
        case call: MethodCall => call
        case loc: Location => loc
        case lit: Literal => lit
        case len: Length => len
        case _ => p.eval.get
      }
    }

    val methodCFG = CFGMethodCall(start.label, parameters, methodDeclaration)
    link(start, firstBlockStart)        // start -> blocks
    link(lastBlockEnd, methodCFG)       // blocks -> method call
    link(methodCFG, end)  // method call -> end

    (start, end, None)
  }

  private def destructImport(
      line: Int,
      col: Int,
      name: String,
      typ: Option[Type],
      methods: Map[String, Option[CFGMethod]] = Map()): Tuple3[CFG, CFG, Option[Location]] = {

    val emptyBlock = Block(line, col, Vector(), Vector())
    destructMethodDeclaration(line, col, name, typ, Vector(), emptyBlock)
  }

  private def destructProgram(
      line: Int,
      col: Int,
      imports: Vector[ExtMethodDeclaration],
      fields: Vector[FieldDeclaration],
      methodVec: Vector[MethodDeclaration],
      methods: Map[String, Option[CFGMethod]] = Map()): Tuple3[CFG, CFG, Option[Location]] = {

    val importCFGs = imports   map { Destruct(_, methods=methods) }
    val fieldCFGs  = fields    map { Destruct(_, methods=methods) }
    val methodCFGs = methodVec map { Destruct(_, methods=methods) }
    throw new NotImplementedError
  }

  /**
   * Create two virtual nodes, corresponding to the start and end nodes.
   *
   * @param line
   * @param col
   * @return (startNode, endNode)
   */
  private def createStartEnd(line: Int, col: Int): Tuple2[CFG, CFG] = {
    val label = s"l${line}call${col}"
    val start = VirtualCFG(s"${label}_start")
    val end = VirtualCFG(s"${label}_end")
    (start, end)
  }

  private def destructAssignment(assignment: Assignment): Tuple3[CFG, CFG, Option[Location]] = {
    assignment match {
      case AssignStatement(line, col, loc, value, valueBlock) => throw new NotImplementedError
      case CompoundAssignStatement(line, col, loc, value, valueBlock, operator) => throw new NotImplementedError
      case Increment(line: Int, col: Int, loc: Location) => throw new NotImplementedError
      case Decrement(line: Int, col: Int, loc: Location) => throw new NotImplementedError
    }

    // case class AssignStatement(
    //     line: Int,
    //     col: Int,
    //     loc: Location,
    //     value: Expression,  // use the eval field
    //     valueBlock: Option[Block])
    //
    // case class CompoundAssignStatement(
    //     line: Int,
    //     col: Int,
    //     loc: Location,
    //     value: Expression,  // use the eval field
    //     valueBlock: Option[Block],
    //     operator: ArithmeticOperator)

    throw new NotImplementedError
  }

  // /** Destruct a location.
  //  *
  //  * @return (start, end) such that:
  //  *         if location contains an index, the CFG returned has the following format:
  //  *             start -> block -> CFGBlock -> end
  //  *             where block is the flattened code to calculate the index expression
  //  *         else
  //  *             start -> CFGBlock -> end
  //  *
  //  *         CFGBlock only contians a single IR, which is Location
  //  *         this location is the original IR that was destructured, now containing
  //  *         an updated .index field that stores the temp variable that holds the index
  //  */
  // private def destructLocation(loc: Location, methods: Map[String, Option[CFGMethod]] = Map()): Tuple3[CFG, CFG, Option[Location]] = {
  //   val (start, end) = createStartEnd(loc.line, loc.col)
  //   val blockCFG = CFGBlock(start.label, Vector(loc))
  //
  //   if (loc.index.isDefined) {
  //     val index = loc.index.get
  //     val (indexStart, indexEnd, _) = index match {
  //       case call: MethodCall => Destruct(call, methods=methods)
  //       case loc: Location => Destruct(loc, methods=methods)
  //       case lit: Literal => Destruct(lit, methods=methods)
  //       case len: Length => Destruct(len, methods=methods)
  //       case _ => Destruct(index.block.get, methods=methods)
  //     }
  //
  //     val indexLocation = index match {
  //       case call: MethodCall => call
  //       case loc: Location => loc
  //       case lit: Literal => lit
  //       case len: Length => len
  //       case _ => index.eval.get
  //     }
  //
  //     // assign it to the location of the temp var
  //     loc.index = Option(indexLocation)
  //
  //     // make sure index block is between start and blockCFG
  //     link(start, indexStart)
  //     link(indexEnd, blockCFG)
  //   } else {
  //     link(start, blockCFG)
  //   }
  //
  //   link(blockCFG, end)
  //
  //   (start, end, None)
  // }

  /** Destructure a given IR and return its start and end nodes.
   * @param ir the flattened IR to destruct
   * @param methods maps method names to declarations
   */
  def apply(  // when called on a program, the returned start node simply points to the CFGProgram
      ir: IR,  // the end node of CFGProgram has no meaning
      loopStart: Option[CFG] = None,
      loopEnd: Option[CFG] = None,
      methods: Map[String, Option[CFGMethod]] = Map()): Tuple3[CFG, CFG, Option[Location]] = {

    val middle: Tuple3[CFG, CFG, Option[Location]] = ir match {
      // assignment
      case Block(line, col, declarations, statements) =>                       destructBlock(line, col, declarations, statements, loopStart, loopEnd, methods)
      case If(line, col, condition, conditionBlock, ifTrue, ifFalse) =>        destructIf(line, col, condition, conditionBlock, ifTrue, ifFalse, methods)
      case For(line, col, start, condition, conditionBlock, update, ifTrue) => destructFor(line, col, start, condition, conditionBlock, update, ifTrue, methods)
      case While(line, col, condition, conditionBlock, ifTrue) =>              destructWhile(line, col, condition, conditionBlock, ifTrue, methods)
      case LocMethodDeclaration(line, col, name, typ, params, block) =>        destructMethodDeclaration(line, col, name, typ, params, block, methods)
      case ExtMethodDeclaration(line, col, name, typ) =>                       destructImport(line, col, name, typ, methods)
      case Program(line, col, imports, fields, methodVec) =>                   destructProgram(line, col, imports, fields, methodVec, methods)
      case MethodCall(line, col, name, params, paramBlocks, method) =>         destructMethodCall(line, col, name, params, paramBlocks, methods)
      case assignment: Assignment => destructAssignment(assignment)
      // case Not(line, col, eval, block, expression) => throw new NotImplementedError
      // case Negate(line, col, eval, block, expression) => throw new NotImplementedError
      // case ArithmeticOperation(line, col, eval, block, operator, lhs, rhs) => throw new NotImplementedError
      // case LogicalOperation(line, col, eval, block, operator, lhs, rhs) => throw new NotImplementedError
      // case TernaryOperation(line, col, eval, block, condition, ifTrue, ifFalse) => throw new NotImplementedError


      // FIXME case MethodCall(line, col, name, params, paramBlocks, method) => throw new NotImplementedError
      // FIXME don't know what to do with assignments because they are not yet flattened
      // case AssignStatement(line, col, loc, value, valueBlock) => throw new NotImplementedError
      // case CompoundAssignStatement(line, col, loc, value, valueBlock, operator) => throw new NotImplementedError
      // case Increment(line, col, loc) => throw new NotImplementedError
      // case Decrement(line, col, loc) => throw new NotImplementedError

      case s: Statement => {  // literals and etc
        val (start, end) = createStartEnd(s.line, s.col)
        val block = CFGBlock(start.label, Vector(s))
        link(start, block)
        link(block, end)
        (start, end, None)
      }
      case _ => throw new NotImplementedError
    }

    throw new NotImplementedError
  }
}
