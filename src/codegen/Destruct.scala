//package codegen
//
//import scala.collection.mutable.{Set, HashSet, ListBuffer, Map, HashMap}
//
//import ir.components._
//import ir.PrettyPrint
//
//object Destruct {
//
//  /** Link the two basic blocks of CFG.
//   *
//   * @param parent such that parent.child will be set to Option(child)
//   * @param child such that one of its parents will be parent
//   */
//  private def link(parent: CFG, child: CFG): Unit = {
//    parent.next = Option(child)
//    child.parents += parent
//  }
//
//  /** Link adjacent CFG's in the provided vectors
//   *
//   * @param Vector[(start,end,ir)] such that v[i].end will be linked to v[i+1].start
//   */
//  private def linkAdjacent(vector: Vector[Tuple3[CFG, CFG, Option[IR]]]): Unit = {
//    vector.zipWithIndex filter {
//      case (_, index) => index < vector.size - 1  // all but the last one
//    } foreach {
//      case ((_, end, ir), index) => {
//        val (start2, _, ir) = vector(index + 1)
//        link(end, start2)
//      }
//    }
//  }
//
//  /**
//   * @param params identical to params of Block
//   * @return (start, end) nodes as a result of destructuring this block
//   */
//  private def destructBlock(
//      line: Int,
//      col: Int,
//      declarations: Vector[IR],
//      statements: Vector[IR],
//      loopStart: Option[CFG] = None,
//      loopEnd: Option[CFG] = None,
//      methods: Map[String, CFGMethod] = Map()): Tuple3[CFG, CFG, Option[Location]] = {
//
//    val blocks = (declarations ++ statements) map {
//      s => {
//        val (start, end, _) = Destruct(s, loopStart, loopEnd, methods)
//        Tuple3(start, end, Option(s))
//      }
//    }
//
//    // link adjacent blocks, in the order that they appear
//    linkAdjacent(blocks)
//
//    val (start, end) = createStartEnd(line, col)
//
//    // if return statement is encountered, set this to the temp var that contians the result
//    var returnLocation: Option[Location] = None
//
//    // if we run into `break` or `continue`, redirect elsewhere besides the next block
//    blocks foreach {
//      case (_, statementEnd, ir) => {
//        ir.get match {
//          case b: Break =>       link(statementEnd, loopEnd.get)
//          case call: Continue => link(statementEnd, loopStart.get)
//          case ret: Return => {
//            link(statementEnd, end)
//            returnLocation = ret.value.get.eval
//          }
//        }
//      }
//    }
//
//    // start of first block, and end of last block
//    if (blocks.size > 0) {
//      val firstBlockStart = blocks(0)._1
//      val lastBlockEnd = blocks(-1)._2
//
//      link(start, firstBlockStart)
//      link(lastBlockEnd, end)
//    }
//
//    (start, end, returnLocation)
//  }
//
//  // returns (cfg, CFGConditional) wherre cfg is the start node
//  // CFGConditional contains all the ifTrue/ifFalse/end and good stuff
//  private def shortCircuit(condition: Expression, label: String, ifTrue: CFG, ifFalse: CFG, end: CFG): Tuple2[CFG, CFGConditional] = {
//    // case class CFGConditional(
//    //     label: String,
//    //     statements: Vector[IR],
//    //     parents: Set[CFG]=Set(),
//    //     var next: Option[CFG] = None,
//    //     var ifFalse: Option[CFG] = None,
//    //     var end: Option[CFG] = None) extends CFG
//
//    // set condition and next and iffalse appropriately
//    val (start, end) = createStartEnd(0, 0, Option(label))
//    condition match {
//      case Not(line, col, eval, block, expression) => {
//
//      }
//      case LogicalOperation(line, col, eval, block, operator, lhs, rhs) => operator match {
//        case And => {
//          // evaluate lhs, and jump to end if flase
//          val lhsBlock = lhs.block.get  // if destruct block
//          val lhsLocation = lhs.eval.get
//          val rhsBlock = rhs.block.get
//          val rhsLocation = rhs.eval.get
//        }
//        case Or => {}
//        case _ => {}  // other logical operators
//      }
//      case _ => {  // literalks
//        // condition
//      }
//    }
//
//    // val statements = block.declarations ++ block.statements
//    // CFGConditional(label, statements, Set(), Option(ifTrue), Option(ifFalse), Option(end))
//    throw new NotImplementedError
//  }
//
//  /** Destruct an if/else statement.
//   *
//   * @param params identical to params of If
//   * @return (start, end) whose internal structure is the following:
//   *   start - the start node, s.t. start.next points to CFGConditional
//   *   end - the end node, s.t. exiting either if/else block leads to this node
//   *   CFGConditional - has two blocks:
//   *       blockIfTrue - one of its parents is this conditional block
//   *       blockIfFalse - one of its parents is this conditional block
//   */
//  private def destructIf(
//      line: Int,
//      col: Int,
//      condition: Expression,
//      conditionBlock: Option[Block],
//      ifTrue: Block,
//      ifFalse: Option[Block],
//      methods: Map[String, CFGMethod] = Map()): Tuple3[CFG, CFG, Option[Location]] = {
//
//    // start and end of this if statement
//    val (start, end) = createStartEnd(line, col)
//    val (ifStart, ifEnd, _) = Destruct(ifTrue, methods=methods)
//
//    var blockIfFalse: Option[CFG] = None
//    if (ifFalse.isDefined) {  // an `else` block exists
//      val (elseStart, elseEnd, _) = Destruct(ifFalse.get, methods=methods)
//      blockIfFalse = Option(elseStart)
//      link(elseEnd, end)
//    } else {  // `else` block does not exist, so blockIfFalse simply points to the end node
//      blockIfFalse = Option(end)
//      link(ifEnd, end)
//    }
//
//    val (condStart, condCFG) = shortCircuit(condition, start.label, ifStart, blockIfFalse.get, end)
//    link(start, condStart)
//
//    (start, end, None)
//  }
//
//  /** Destruct an for loop.
//   */
//  private def destructFor(  // TODO handle continue and break
//      line: Int,
//      col: Int,
//      initialize: AssignStatement,
//      condition: Expression,
//      conditionBlock: Option[Block],
//      update: Assignment,
//      ifTrue: Block,
//      methods: Map[String, CFGMethod] = Map()): Tuple3[CFG, CFG, Option[Location]] = {
//
//    val (start, end) = createStartEnd(line, col)
//
//    val (initializeStart, initializeEnd, _) = Destruct(initialize, methods=methods)
//    val (updateStart, updateEnd, _) = Destruct(update, methods=methods)
//    val (blockStart, blockEnd, _) = Destruct(ifTrue, Option(start), Option(end), methods=methods)
//    val (condStart, condCFG) = shortCircuit(condition, start.label, blockStart, end, end)
//
//    link(start, initializeStart)
//    link(initializeEnd, updateStart)
//    link(updateEnd, condStart)
//    link(blockEnd, updateStart)
//    (start, end, None)
//  }
//
//  /** Destruct an while loop.
//   */
//  private def destructWhile(  // TODO handle continue and break
//      line: Int,
//      col: Int,
//      condition: Expression,
//      conditionBlock: Option[Block],
//      ifTrue: Block,
//      methods: Map[String, CFGMethod] = Map()): Tuple3[CFG, CFG, Option[Location]] = {
//
//    val (start, end) = createStartEnd(line, col)
//
//    val (blockStart, blockEnd, _) = Destruct(ifTrue, Option(start), Option(end), methods=methods)
//    val (condStart, condCFG) = shortCircuit(condition, start.label, blockStart, end, end)
//
//    link(start, condStart)
//    link(blockEnd, condStart)
//    (start, end, None)
//  }
//
//  private def destructMethodDeclaration(
//      method: LocMethodDeclaration,
//      methods: Map[String, CFGMethod] = Map()): Tuple3[CFG, CFG, Option[Location]] = {
//
//    val (start, end) = createStartEnd(method.line, method.col)
//
//    if (methods contains method.name) {
//      val methodCFG = (methods get method.name).get
//      link(start, methodCFG)
//      link(methodCFG, end)
//      return (start, end, None)
//    }
//
//    // made methodCFG mutable so that when destructing method call, it knows where to point to
//    val methodCFG = CFGMethod(start.label, None, method.params, method)
//    methods += method.name -> methodCFG
//
//    val (blockStart, blockEnd, _) = Destruct(method.block)
//    methodCFG.block = Option(blockStart)
//
//    link(start, methodCFG)
//    link(methodCFG, end)
//
//    (start, end, None)
//  }
//
//  /**
//   * @return (start, end, loc) where loc holds the
//   */
//  private def destructMethodCall(
//      line: Int,
//      col: Int,
//      name: String,
//      params: Vector[Expression],
//      paramBlocks: Vector[Option[Block]],
//      methods: Map[String, CFGMethod] = Map(),
//      iter: Iterator[Int]): Tuple3[CFG, CFG, Option[Location]] = {
//
//    val blocks = params map {
//      exp => Destruct(exp.block.get, methods=methods)
//    } map {  // just to be compatible with linkAdjacent
//      case (start, end, _) => Tuple3(start, end, None)
//    }
//
//    // link each block with each other
//    linkAdjacent(blocks)
//
//    val firstBlockStart = blocks(0)._1
//    val lastBlockEnd = blocks(-1)._2
//
//    val (start, end) = createStartEnd(line, col)
//    val methodDeclaration = (methods get name).get
//
//    // locations or literals of each parameters
//    val paramLocations = params map {
//      p => p match {
//        case call: MethodCall => call
//        case loc: Location => loc
//        case lit: Literal => lit
//        case len: Length => len
//        case _ => p.eval.get
//      }
//    }
//
//    val methodCFG = CFGMethodCall(start.label, paramLocations, methodDeclaration)
//    link(start, firstBlockStart)        // start -> blocks
//    link(lastBlockEnd, methodCFG)       // blocks -> method call
//
//    // get the return location of the method body, and assign it to some temporary variable
//    val tempResult = Location(line, col, s"${iter.next}_tmp_call_result", None, None)
//    val (_, _, retLoc) = Destruct(methodDeclaration.block, methods=methods)
//    val tempAssignment = AssignStatement(0, 0, tempResult, retLoc.get, None)
//
//    val assignBlock = CFGBlock("none", Vector(tempAssignment))
//
//    link(methodCFG, assignBlock)  // method call -> end
//    link(assignBlock, end)
//
//    (start, end, Option(tempResult))
//  }
//
//  private def destructImport(ext: ExtMethodDeclaration): Tuple3[CFG, CFG, Option[Location]] = {
//    val (start, end) = createStartEnd(ext.line, ext.col)
//    val assignBlock = CFGBlock(start.label, Vector(ext))
//
//    link(start, assignBlock)  // method call -> end
//    link(assignBlock, end)
//
//    (start, end, None)
//  }
//
//  private def destructProgram(
//      line: Int,
//      col: Int,
//      imports: Vector[ExtMethodDeclaration],
//      fields: Vector[FieldDeclaration],
//      methodVec: Vector[MethodDeclaration],
//      methods: Map[String, CFGMethod] = Map()): Tuple3[CFG, CFG, Option[Location]] = {
//
//    val importCFGs = imports   map { Destruct(_, methods=methods) }
//    val methodCFGs = methodVec map { Destruct(_, methods=methods) }
//
//    val (start, end) = createStartEnd(line, col)
//    val fieldsCFG = CFGBlock(start.label, fields)
//    val programCFG = CFGProgram(start.label, imports, fieldsCFG, methods)
//    link(programCFG, end)
//    (programCFG, end, None)
//  }
//
//  /**
//   * Create two virtual nodes, corresponding to the start and end nodes.
//   *
//   * @param line
//   * @param col
//   * @return (startNode, endNode)
//   */
//  private def createStartEnd(line: Int, col: Int, labelT: Option[String] = None): Tuple2[CFG, CFG] = {
//    val label = if (labelT.isDefined) labelT.get else s"l${line}call${col}"
//    val start = VirtualCFG(s"${label}_start")
//    val end = VirtualCFG(s"${label}_end")
//    (start, end)
//  }
//
//  /** Destruct an Assignment.
//   *
//   * Destructs an assignment statement to the following structure:
//   *     start -> block -> CFGBlock -> end
//   * Where `block` contains the flattened code to calculate the location to assign to
//   * as well as the expression to assign there.
//   * CFGBlock contains one of Increment, Decrement, AssignStatement, CompoundAssignStatement.
//   */
//  private def destructAssignment(
//      assignment: Assignment,
//      methods: Map[String, CFGMethod] = Map()): Tuple3[CFG, CFG, Option[Location]] = {
//
//    def getExpLoc(value: Expression): Expression = value match {
//      case call: MethodCall => call
//      case loc: Location => loc
//      case lit: Literal => lit
//      case len: Length => len
//      case _ => value.eval.get
//    }
//
//    def getExpBlock(value: Expression): Tuple3[CFG, CFG, Option[Location]] = value match {
//      case call: MethodCall => Destruct(call, methods=methods)
//      case loc: Location => Destruct(loc, methods=methods)
//      case lit: Literal => Destruct(lit, methods=methods)
//      case len: Length => Destruct(len, methods=methods)
//      case _ => Destruct(value.eval.get, methods=methods)
//    }
//
//    val (start, end) = createStartEnd(assignment.line, assignment.col)
//
//    val (expStart, expEnd) = assignment match {
//      // see spec of destructLocation for reasons why we destruct location here
//      // the location of the actual value is stored under value.eval
//      case _: Increment | _: Decrement => {
//        val (start, end) = createStartEnd(assignment.line, assignment.col)
//        Destruct(assignment.loc, methods=methods)
//        val block = CFGBlock(start.label, Vector(assignment))
//        link(end, block)
//        (start, block)
//      }
//      case ass: AssignmentStatements => {
//        val (indexStart, indexEnd, _) = Destruct(ass.loc, methods=methods)
//        val expressionLocation = getExpLoc(ass.value)
//        val (blockStart, blockEnd, blockLocation) = getExpBlock(ass.value)
//        val assignBlock = CFGBlock(start.label, Vector(ass))
//        link(indexEnd, blockStart)
//        link(blockEnd, assignBlock)
//        (indexStart, assignBlock)
//      }
//    }
//
//    link(start, expStart)
//    link(expEnd, end)
//
//    (start, end, None)
//  }
//
//  /** Destruct a location.
//   *
//   * MORE IMPORTANTLY, updates `loc` such that its `index` field now has the
//   * location of the temporary variable that contains the result of flattened code.
//   *
//   * @return (start, end, loc) such that:
//   *         loc is the original location that was destructed, this is mainly for convenience
//   */
//  private def destructLocation(loc: Location, methods: Map[String, CFGMethod] = Map()): Tuple3[CFG, CFG, Option[Location]] = {
//    val (start, end) = createStartEnd(loc.line, loc.col)
//    val blockCFG = CFGBlock(start.label, Vector(loc))
//
//    if (loc.index.isDefined) {  // location is an array
//      val indexExpression = loc.index.get
//      val (indexStart, indexEnd, _) = indexExpression match {
//        case call: MethodCall => Destruct(call, methods=methods)
//        case loc: Location    => Destruct(loc,  methods=methods)
//        case lit: Literal     => Destruct(lit,  methods=methods)
//        case len: Length      => Destruct(len,  methods=methods)
//        case _ => Destruct(indexExpression.block.get, methods=methods)
//      }
//
//      // location that actually holds the final result
//      val indexLocation = indexExpression match {
//        case call: MethodCall => call
//        case loc: Location => loc
//        case lit: Literal => lit
//        case len: Length => len
//        case _ => indexExpression.eval.get
//      }
//
//      // assign it to the location of the temp var
//      loc.index = Option(indexLocation)
//
//      // start -> indexBlock -> indexCFG -> end
//      link(start, indexStart)
//      link(indexEnd, blockCFG)
//    } else {
//      link(start, blockCFG)
//    }
//
//    link(blockCFG, end)
//
//    (start, end, Option(loc))
//  }
//
//  /** Destructure a given IR and return its start and end nodes.
//   * @param ir the flattened IR to destruct
//   * @param methods maps method names to declarations
//   */
//  def apply(  // when called on a program, the returned start node simply points to the CFGProgram
//      ir: IR,  // the end node of CFGProgram has no meaning
//      loopStart: Option[CFG] = None,
//      loopEnd: Option[CFG] = None,
//      methods: Map[String, CFGMethod] = Map(),
//      iter: Iterator[Int] = Stream.iterate(0)(_ + 1).iterator): Tuple3[CFG, CFG, Option[Location]] = {
//
//    ir match {
//      // assignment
//      case Block(line, col, declarations, statements) =>                       destructBlock(line, col, declarations, statements, loopStart, loopEnd, methods)
//      case If(line, col, condition, conditionBlock, ifTrue, ifFalse) =>        destructIf(line, col, condition, conditionBlock, ifTrue, ifFalse, methods)
//      case For(line, col, start, condition, conditionBlock, update, ifTrue) => destructFor(line, col, start, condition, conditionBlock, update, ifTrue, methods)
//      case While(line, col, condition, conditionBlock, ifTrue) =>              destructWhile(line, col, condition, conditionBlock, ifTrue, methods)
//      case method: LocMethodDeclaration =>                                     destructMethodDeclaration(method, methods=methods)
//      case ext: ExtMethodDeclaration =>                                        destructImport(ext)
//      case Program(line, col, imports, fields, methodVec) =>                   destructProgram(line, col, imports, fields, methodVec, methods)
//      case MethodCall(line, col, name, params, paramBlocks, method) =>         destructMethodCall(line, col, name, params, paramBlocks, methods, iter)
//      case assignment: Assignment =>                                           destructAssignment(assignment, methods)
//
//      case s: Statement => {  // literals and etc
//        val (start, end) = createStartEnd(s.line, s.col)
//        val block = CFGBlock(start.label, Vector(s))
//        link(start, block)
//        link(block, end)
//        (start, end, None)
//      }
//      case _ => throw new NotImplementedError
//    }
//  }
//}
