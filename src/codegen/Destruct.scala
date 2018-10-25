package codegen

import scala.collection.mutable.{HashSet, Set, ListBuffer}
import scala.collection.immutable.{Map}

import ir.components._
import ir.PrettyPrint

object Destruct {

  // returns start, end
  def conditional(block: Block): Tuple2[VirtualCFG, VirtualCFG] = {
    throw new Exception
  }

  /** Destructure a given IR and return its start and end nodes.
   * @param ir the flattened IR to destruct
   */
  def apply(ir: IR): Tuple2[VirtualCFG, VirtualCFG] = {
    val start = VirtualCFG()
    val end = VirtualCFG()

    val middle = ir match {
      case Block(line, col, declarations, statements) => {
        // list of (start, end) of basic blocks
        val allStartEnd = declarations.map(Destruct(_)).to[ListBuffer]

        // find basic blocks
        val contiguousBlock: ListBuffer[IR] = ListBuffer()
        statements foreach {
          statement => statement match {
            case m: MethodCall => new Exception
            case _:If | _:For | _:While => {
              if (contiguousBlock.size > 0) {  // gather them into a basic CFG
                val contiguous = contiguousBlock.map(x => x).toVector
                contiguousBlock.clear()

                val start = VirtualCFG()
                val end = VirtualCFG()
                val block = CFGBlock(s"l${line}c${col}", contiguous)
                start.next = Option(block)
                end.parents += block
                block.parents += start
                block.next = Option(end)
                allStartEnd += Tuple2(start, end)
              }
              allStartEnd += Destruct(statement)
            }
            case _ => contiguousBlock += statement
          }
        }

        // link every adjacent basic blocks in allStartEnd
        (0 to allStartEnd.size - 2) foreach {
          i => {
            val (start1, end1) = allStartEnd(i)
            val (start2, end2) = allStartEnd(i + 1)
            end1.next = Option(start2)
            start2.parents += end1
          }
        }

        // start of first block, and end of last block
        (allStartEnd(0)._1, allStartEnd(-1)._2)
      }

      case If(line, col, condition, conditionBlock, ifTrue, ifFalse) => {
        // val (conditionalStart, conditionalEnd) = Destruct.conditional(conditionBlock.get)
        val (ifStart, ifEnd) = Destruct(ifTrue)
        lazy val (elseStart, elseEnd) = Destruct(ifFalse.get)

        val statements = conditionBlock.get.declarations ++ conditionBlock.get.statements

        CFGConditional(
          s"l${line}c${col}",
          statements,
          Set(start),
          Option(ifStart),
          if (ifFalse.isDefined) Option(elseStart) else Option(end))
      }

      case For(line, col, start, condition, conditionBlock, update, ifTrue) => new Exception
      case While(line, col, condition, conditionBlock, ifTrue) => new Exception

      case Program(line, col, imports, fields, methods) => new Exception
      case LocMethodDeclaration(line, col, name, typ, params, block) => new Exception
      case ExtMethodDeclaration(line, col, name, typ) => new Exception
      case MethodCall(line, col, name, params, paramBlocks, method) => new Exception
      case _ => ir
    }


    Tuple2(start, end)
  }
}
