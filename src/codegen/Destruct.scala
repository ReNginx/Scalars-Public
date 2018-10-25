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

  /**
   * @return the start and the end nodes of a block
   */
  private def destructBlock(
      line: Int,
      col: Int,
      declarations: Vector[IR],
      statements: Vector[IR]): Tuple2[VirtualCFG, VirtualCFG] = {

    // (start, end) of every basic block within this Block
    val blocks = declarations.map(Destruct(_)).to[ListBuffer]

    // gets populated with statements that form a contiguous block
    // cleared when conditionals are encountered
    val contiguousBlock: ListBuffer[IR] = ListBuffer()

    // form a CFGBlock with everything in contiguousBlock, clear contiguousBlock, push to blocks
    def destructContiguousBlock(): Unit = {
      val contiguous = contiguousBlock.map(x => x).toVector
      contiguousBlock.clear()

      val (start, end) = createStartEnd(line, col)
      val block = CFGBlock(start.label, contiguous)

      start.next = Option(block)
      block.parents += start
      block.next = Option(end)
      end.parents += block

      blocks += Tuple2(start, end)
    }

    statements foreach {
      s => s match {
        case m: MethodCall => throw new NotImplementedError

        // encountered conditional, so destruct the contiguous block, then the conditional
        case _: If => {
          if (contiguousBlock.size > 0) destructContiguousBlock()
          blocks += Destruct(s)
        }
        case _: For => {
          if (contiguousBlock.size > 0) destructContiguousBlock()
          blocks += Destruct(s)
        }
        case _: While => {
          if (contiguousBlock.size > 0) destructContiguousBlock()
          blocks += Destruct(s)
        }
        case _ => contiguousBlock += s
      }
    }

    // link every adjacent basic blocks in blocks
    // given [a, b, c, d], links the end of a to start of b, end of b to start of c, and etc
    (0 to blocks.size - 2) foreach {
      i => {
        val (start1, end1) = blocks(i)
        val (start2, end2) = blocks(i + 1)
        end1.next = Option(start2)
        start2.parents += end1
      }
    }

    // start of first block, and end of last block
    val firstBlockStart = blocks(0)._1
    val lastBlockEnd = blocks(-1)._2

    val (start, end) = createStartEnd(line, col)
    start.next = Option(firstBlockStart)
    firstBlockStart.parents += start

    lastBlockEnd.next = Option(end)
    end.parents += lastBlockEnd

    (start, end)
  }

  // same story
  private def destructIf(line: Int,
      col: Int,
      condition: Expression,
      conditionBlock: Option[Block],
      ifTrue: Block,
      ifFalse: Option[Block]): Tuple2[VirtualCFG, VirtualCFG] = {

    val (start, end) = createStartEnd(line, col)

    // val (conditionalStart, conditionalEnd) = Destruct.conditional(conditionBlock.get)
    val (ifStart, ifEnd) = Destruct(ifTrue)
    val statements = conditionBlock.get.declarations ++ conditionBlock.get.statements

    val parents: Set[CFG] = Set(start)
    val blockIfTrue = Option(ifStart)
    var blockIfFalse: Option[CFG] = None

    // an `else` block exists, so blockIfFalse points to the start node of the else block
    if (ifFalse.isDefined) {
      val (elseStart, elseEnd) = Destruct(ifFalse.get)
      ifEnd.next = Option(elseStart)
      elseStart.parents += ifEnd

      elseStart.next = Option(end)
      end.parents += elseStart

      blockIfFalse = Option(elseStart)
    } else { // `else` block does not exist, so blockIfFalse simply points to the end node
      ifEnd.next = Option(end)
      end.parents += ifEnd

      blockIfFalse = Option(end)
    }

    val conditionalCFG = CFGConditional(start.label, statements, parents, blockIfTrue, blockIfFalse)
    start.next = Option(conditionalCFG)
    conditionalCFG.parents += start

    conditionalCFG.next = Option(end)
    end.parents += conditionalCFG

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

      case For(line, col, start, condition, conditionBlock, update, ifTrue) => throw new NotImplementedError
      case While(line, col, condition, conditionBlock, ifTrue) => throw new NotImplementedError

      case Program(line, col, imports, fields, methods) => throw new NotImplementedError
      case LocMethodDeclaration(line, col, name, typ, params, block) => throw new NotImplementedError
      case ExtMethodDeclaration(line, col, name, typ) => throw new NotImplementedError
      case MethodCall(line, col, name, params, paramBlocks, method) => throw new NotImplementedError
      case _ => throw new NotImplementedError
    }

    throw new NotImplementedError
  }
}
