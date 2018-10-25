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

    def createLabel(line: Int, col: Int) = s"l${line}c${col}"

    val middle: Tuple2[VirtualCFG, VirtualCFG] = ir match {
      case Block(line, col, declarations, statements) => {
        val label = createLabel(line, col)
        val start = VirtualCFG(s"${label}start")
        val end = VirtualCFG(s"${label}end")
        // list of (start, end) of basic blocks
        val allStartEnd = declarations.map(Destruct(_)).to[ListBuffer]

        // find basic blocks
        val contiguousBlock: ListBuffer[IR] = ListBuffer()
        statements foreach {
          statement => statement match {
            case m: MethodCall => throw new Exception
            case _:If | _:For | _:While => {
              if (contiguousBlock.size > 0) {  // gather them into a basic CFG
                val contiguous = contiguousBlock.map(x => x).toVector
                contiguousBlock.clear()

                val start = VirtualCFG("")
                val end = VirtualCFG("")
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
        val firstStart = allStartEnd(0)._1
        val lastEnd = allStartEnd(-1)._2

        start.next = Option(firstStart)
        firstStart.parents += start

        lastEnd.next = Option(end)
        end.parents += lastEnd

        (start, end)
      }

      case If(line, col, condition, conditionBlock, ifTrue, ifFalse) => {
        val label = createLabel(line, col)

        val start = VirtualCFG(s"${label}start")
        val end = VirtualCFG(s"${label}end")
        // val (conditionalStart, conditionalEnd) = Destruct.conditional(conditionBlock.get)
        val (ifStart, ifEnd) = Destruct(ifTrue)
        val statements = conditionBlock.get.declarations ++ conditionBlock.get.statements


        val parents: Set[CFG] = Set(start)
        val blockIfTrue = Option(ifStart)

        var blockIfFalse: Option[CFG] = None
        // if an `else` block exists
        //   blockIfFalse points to the start node of the else block
        // if `else` block does not exist
        //   blockIfFalse simply points to the end node
        if (ifFalse.isDefined) {
          val (elseStart, elseEnd) = Destruct(ifFalse.get)
          // if block -> else block
          ifEnd.next = Option(elseStart)
          elseStart.parents += ifEnd

          // else block -> end
          elseStart.next = Option(end)
          end.parents += elseStart

          blockIfFalse = Option(elseStart)
        } else {
          ifEnd.next = Option(end)
          end.parents += ifEnd

          blockIfFalse = Option(end)
        }

        val conditionalCFG = CFGConditional(label, statements, parents, blockIfTrue, blockIfFalse)
        throw new Exception
      }

      case For(line, col, start, condition, conditionBlock, update, ifTrue) => throw new Exception
      case While(line, col, condition, conditionBlock, ifTrue) => throw new Exception

      case Program(line, col, imports, fields, methods) => throw new Exception
      case LocMethodDeclaration(line, col, name, typ, params, block) => throw new Exception
      case ExtMethodDeclaration(line, col, name, typ) => throw new Exception
      case MethodCall(line, col, name, params, paramBlocks, method) => throw new Exception
      case _ => throw new Exception
    }

    throw new NotImplementedError
  }
}
