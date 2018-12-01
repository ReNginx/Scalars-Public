package optimization.loop_opt

import codegen._
import ir.components._
import optimization.Labeling.StmtId
import optimization.Optimization

import scala.collection.immutable
import scala.collection.mutable.{ArrayBuffer, Map, Queue, Set, SortedSet}

/**
  * this function won't mark any node.
  * please make sure that cfg is an instance of CFGProgram.
  * In this part, we would move any loop invariant to the beginning of the loop.
  * i.e. make a new CFGblock in front of loop header, and place those invariants there.
  *
  */

object InvariantOpt extends Optimization {
  var graph = Map[StmtId, Set[StmtId]]()
  var revGraph = Map[StmtId, Set[StmtId]]()
  var dom = Map[StmtId, Set[StmtId]]()
  var cnt = 0

  def getStmt(pos: StmtId): Option[IR] = {
    if (pos._2 >= 0) {
      // if (pos._2 >= pos._1.asInstanceOf[CFGBlock].statements.size) {
      //   System.err.println("start")
      //   pos._1.asInstanceOf[CFGBlock].statements foreach (PrintCFG.prtStmt(_))
      //   System.err.println("over")
      //   assert(false)
      // }
      Option(pos._1.asInstanceOf[CFGBlock].statements(pos._2))
    }
    else
      None
  }

  def findInvariant(loop: LoopEntity[StmtId]): Vector[StmtId] = {
    val invariant = ArrayBuffer[StmtId]()
    val stmts = loop.stmts filter (_._2 >= 0)
    val definitions = (stmts
      map (x => x._1.asInstanceOf[CFGBlock].statements(x._2))
      filter (_.isInstanceOf[Def]))
    val defVars = (definitions map (_.asInstanceOf[Def].getLoc.field)).toSet
    val invariantVars = Set[FieldDeclaration]()
    val q = Queue[StmtId]() ++= stmts

    while (q.nonEmpty) {
      val head = q.dequeue()
      val stmt = head._1.asInstanceOf[CFGBlock].statements(head._2)

      //def isInvariant(): Boolean = {
      val isInvariant = {
        stmt match {
          case d: Def => {
            d.getLoc.field.get match {
              case vdecl: VariableDeclaration => {
               //System.err.println(s"in isInvariant ${d}")
                val k = d.asInstanceOf[Use]
               //System.err.println(s"convert successful")
                assert(d.isInstanceOf[Use])
                val uses = k.getUse
               //System.err.println(s"find use successful")

                if (d.asInstanceOf[Use].getUse.isEmpty)
                  true
                else {
                  val useLst = d.asInstanceOf[Use].getUse
                  useLst forall (use => {
                    use.field.get.isInstanceOf[VariableDeclaration] && {
                      /** here a location is valid iff all reaching def is outside of the loop,
                        * or reaching by only one invariant in the loop.
                        */
                      val reaching = reachingDef(use, head)
                      (reaching forall (!loop.stmts.contains(_))) ||
                        (reaching.size == 1 && invariant.contains(reaching.head))
                    }
                  })
                }
              }

              case _ => false
            }
          }
          case _ => false
        }
      }

      if (isInvariant) {
        if (!invariant.contains(head)) invariant += head
        q ++= graph(head) filter (_._2 >= 0)
      }
    }

    invariant.toVector //order matters
  }

  def reachingDef(loc: Location, pos: StmtId): Vector[StmtId] = {
    val vis = Set[StmtId](pos)
    val q = Queue[StmtId](pos)
    val reachingDef = ArrayBuffer[StmtId]()
    while (q.nonEmpty) {
      val head = q.dequeue()
      val toadd = revGraph(head) filter (prev => {
        {
          prev._1 match {
            case call: CFGMethodCall => {
              if (loc.field.get.isGlobal) {
                if (prev._2 == -1)
                  reachingDef += prev
                false
              }
              else true
            }

            case block: CFGBlock => {
              val optIr = getStmt(prev)
              optIr match {
                case Some(ir) => {
                  ir match {
                    case d: Def =>
                      if (d.getLoc == loc) {
                        reachingDef += prev
                        false
                      }
                      else true
                    case _ => true
                  }
                }
                case _ => true
              }
            }

            case _ => true
          }
        } && !vis.contains(prev)
      })
      q ++= toadd
      vis ++= toadd
    }
    reachingDef.toVector
  }

  def useHelper(pos: StmtId): immutable.Set[Location] = {
    pos._1 match {
      case block: CFGBlock => {
        if (pos._2 >= 0)
          block.statements(pos._2).asInstanceOf[Use].getUse
        else
          immutable.Set()
      }
      case call: CFGMethodCall => {
        if (pos._2 == -1 && call.params.size > 0)
          call.params map (_.getUse) reduce (_ ++ _)
        else immutable.Set()
      }
      case cond: CFGConditional => {
        if (pos._2 == -1)
          cond.condition.getUse
        else immutable.Set()
      }
      case _ => immutable.Set()
    }
  }

  def judgeMov(invariant: Vector[StmtId],
               loop: LoopEntity[StmtId]): Vector[StmtId] = {
    (invariant
      filter (invar => {
      //System.err.println(s"loops exits are ${loop.exits}")
      //System.err.println(s"dom(invar) are ${dom(invar)}")
      (loop.exits forall (dom(_).contains(invar))) || { // dom all exits or not used after the loop
       //System.err.println(s"try another move")
        //PrintCFG.prtStmt(getStmt(invar).get)

        val q = Queue[StmtId]()
        val vis = Set[StmtId]()
        val invarLoc = getStmt(invar).get.asInstanceOf[Def].getLoc.field

        loop.exits foreach (x => {
          val out = graph(x) diff loop.stmts diff vis
          q ++= out
          vis ++= out
        })
        var flag = true
        while (q.nonEmpty && flag) {
          val head = q.dequeue()
         //System.err.println(s"q contains ${q}")

          def stmtHelper(stmt:Option[IR]): Unit = {
            stmt match {
              case Some(ir) => {
                val use = ir.asInstanceOf[Use].getUse
                val fields = use map (x => x.field.get)
                if (fields.contains(invarLoc.get)) {
                  flag = false
                }
              }
              case None =>
            }
          }

          head._1 match {
            case call: CFGMethodCall => {
              call.params foreach (x => stmtHelper(Option(x)))
            }
            case block: CFGBlock => {
              val stmt = getStmt(head)
              stmtHelper(stmt)
            }
            case cond: CFGConditional => {
              stmtHelper(Option(cond.condition))
            }
            case _ =>
          }

          val toAdd = graph(head) diff loop.stmts diff vis
          q ++= toAdd
          vis ++= toAdd
        }
       //System.err.println(s"flag is ${flag}")
        flag
      }
    }) // dom all exits
      filter (invar => { //
     //System.err.println(s"second level ${invar}")
      lazy val invarLoc = getStmt(invar).get.asInstanceOf[Def].getLoc.field
      loop.stmts forall (pos => {
        pos == invar || {
          val optIr = getStmt(pos)
          optIr match {
            case Some(ir) => {
              ir match {
                case d: Def => d.getLoc.field != invarLoc
                case _ => true
              }
            }
            case _ => true
          }
        }
      })
    })
      filter (invar => {
     //System.err.println(s"third level ${invar}")
      lazy val invarLoc = getStmt(invar).get.asInstanceOf[Def].getLoc.field
      loop.stmts forall (pos => {
        val use = useHelper(pos) filter (loc => {
          loc.field == invarLoc
        })
        if (use.nonEmpty) {
          val reaching = reachingDef(use.head, pos)
          reaching.size == 1 && reaching.contains(invar)
        }
        else true
      })
    }))
  }

  def apply(cfg: CFG, isInit: Boolean = true): Unit = {
    // val field = VariableDeclaration(1,1,"t",None)
    // val loc = Location(1,1, "t", None, Option(field))
    // val arith = ArithmeticOperation(1,1, Option(loc), None, Add, IntLiteral(1,1,10), IntLiteral(1,1,20))
    // val unary = Negate(1,1,Option(loc), None, loc)
    // val asg = AssignStatement(1,1, loc, loc)
    // val k = arith.getUse
    //System.err.println(s"test arith ${k}")
    if (isInit) {
      init()
    }

    LoopConstruction(cfg)
    val loops = LoopConstruction.loops
    graph = LoopConstruction.graph
    revGraph = LoopConstruction.revGraph
    dom = LoopConstruction.dom

   //System.err.println(s"loops counts ${loops.size}")
   //System.err.println("finished construction")

    val toMove = loops map (
      loop => {
        val invariant = findInvariant(loop)
       //System.err.println(s"find potential invariant ${invariant}")
        val movable = judgeMov(invariant, loop)
       //System.err.println(s"find movable ")
        // movable foreach (x => PrintCFG.prtStmt(getStmt(x).get))
        setChanged()
        (loop, movable.sortBy(x => (x._1.label, -x._2)))
      }) filter (pair => {
        pair._2.size > 0
      })

   //System.err.println("finished tomove analysis")

    val conds = (graph.keySet map (_._1)
      filter (_.isInstanceOf[CFGConditional])
      map (_.asInstanceOf[CFGConditional]))

    toMove foreach (pair => {
      val loopHeader = pair._1.header._1
      val invariant = pair._2
      cnt += 1
      val preHeader = CFGBlock(s"_${cnt}_"+loopHeader.label + "_preheader", ArrayBuffer())
      preHeader.statements ++= (invariant flatMap (getStmt(_))).reverse

      val parToRm = Set[CFG]()
      loopHeader.parents foreach (
        _ match {
          case cond: CFGConditional => {
            if (cond.next == Option(loopHeader)) {
              if (!dom((cond, -1)).contains((loopHeader,-1))) {
                cond.next = None
                parToRm += cond
                Destruct.link(cond, preHeader)
              }
            }
            if (cond.ifFalse == Option(loopHeader)) {
              if (!dom((cond, -1)).contains((loopHeader,-1))) {
                cond.ifFalse = None
                parToRm += cond
                Destruct.linkFalse(cond, preHeader)
              }
            }
          }
          case other => {
            if (other.next == Option(loopHeader)) {
              if (!dom((other,-1)).contains((loopHeader,-1))) {
                other.next = None
                parToRm += other
                Destruct.link(other, preHeader)
              }
            }
          }
        })

      loopHeader.parents --= parToRm
      Destruct.link(preHeader, loopHeader)
      (conds filter (_.end == Option(loopHeader))
        foreach (cond => {
          if (!dom((cond, -1)).contains((loopHeader,-1)))
            cond.end = Option(preHeader)
        }))
    })

    val toRemove = Map[CFG, SortedSet[Int]]()
    toMove foreach (pair => {
      val loopHeader = pair._1.header._1
      val invariant = pair._2
      invariant foreach (invar => {
        if (!toRemove.contains(invar._1))
          toRemove(invar._1) = SortedSet()
        toRemove(invar._1) += -invar._2 // note here uses negative number
      })
    })
    toRemove foreach (x => {
      x._2 foreach (idx => x._1.asInstanceOf[CFGBlock].statements.remove(-idx)) // note here also uses nagative
    })
   //System.err.println("finished all")
  }
}
