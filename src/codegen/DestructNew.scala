//package codegen
//
//import ir.components._
//
//import scala.collection.mutable.Map
//
//object DestructNew {
//
//  /**
//   * @param params identical to params of Block
//   * @return (start, end) nodes as a result of destructuring this block
//   */
//  private def destructBlock(
//      block: Block,
//      loopStart: Option[CFG] = None,
//      loopEnd: Option[CFG] = None): Tuple2[CFG, CFG] = {
//    throw new NotImplementedError()
//  }
//
//  private def destructIf(ifstmt: If): Tuple2[CFG, CFG] = {
//    throw new NotImplementedError()
//  }
//
//  /** Destruct an for loop.
//   */
//  private def destructFor(forstmt: For): Tuple2[CFG, CFG] = {
//    throw new NotImplementedError()
//  }
//
//  /** Destruct an while loop.
//   */
//  private def destructWhile(whilestmt: While): Tuple2[CFG, CFG] = {
//    throw new NotImplementedError()
//  }
//
//  private def destructMethodDeclaration(method: LocMethodDeclaration): Tuple2[CFG, CFG] = {
//    throw new NotImplementedError()
//  }
//
//  /**
//   * @return (start, end, loc) where loc holds the
//   */
//  private def destructMethodCall(call: MethodCall): Tuple2[CFG, CFG] = {
//    throw new NotImplementedError()
//  }
//
//  private def destructProgram(program: Program): Tuple2[CFG, CFG] = {
//    throw new NotImplementedError()
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
//  private def destructAssignment(assignment: Assignment): Tuple2[CFG, CFG] = {
//    throw new NotImplementedError()
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
//  private def destructLocation(loc: Location): Tuple2[CFG, CFG] = {
//    throw new NotImplementedError()
//  }
//
//  private def destructExpression(expr: Expression): Tuple2[CFG, CFG] = {
//    throw new NotImplementedError()
//  }
//  /** Destructure a given IR and return its start and end nodes.
//   * @param ir the flattened IR to destruct
//   * @param methods maps method names to declarations
//   */
////  def apply(  // when called on a program, the returned start node simply points to the CFGProgram
////      ir: IR,  // the end node of CFGProgram has no meaning
////      loopStart: Option[CFG] = None,
////      loopEnd: Option[CFG] = None,
////      methods: Map[String, CFGMethod] = Map(),
////      iter: Iterator[Int] = Stream.iterate(0)(_ + 1).iterator): Tuple2[CFG, CFG] = {
////
////    ir match {
////      // assignment
////      case Block(line, col, declarations, statements) =>                       destructBlock(line, col, declarations, statements, loopStart, loopEnd, methods)
////      case If(line, col, condition, conditionBlock, ifTrue, ifFalse) =>        destructIf(line, col, condition, conditionBlock, ifTrue, ifFalse, methods)
////      case For(line, col, start, condition, conditionBlock, update, ifTrue) => destructFor(line, col, start, condition, conditionBlock, update, ifTrue, methods)
////      case While(line, col, condition, conditionBlock, ifTrue) =>              destructWhile(line, col, condition, conditionBlock, ifTrue, methods)
////      case method: LocMethodDeclaration =>                                     destructMethodDeclaration(method, methods=methods)
////      case ext: ExtMethodDeclaration =>                                        destructImport(ext)
////      case Program(line, col, imports, fields, methodVec) =>                   destructProgram(line, col, imports, fields, methodVec, methods)
////      case MethodCall(line, col, name, params, paramBlocks, method) =>         destructMethodCall(line, col, name, params, paramBlocks, methods, iter)
////      case assignment: Assignment =>                                           destructAssignment(assignment, methods)
////
////      case s: Statement => {  // literals and etc
////        val (start, end) = createStartEnd(s.line, s.col)
////        val block = CFGBlock(start.label, Vector(s))
////        link(start, block)
////        link(block, end)
////        (start, end, None)
////      }
////      case _ => throw new NotImplementedError
////    }
////  }
//}
