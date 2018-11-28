package optimization.loop_opt

import codegen._
import ir.components._
import optimization.Labeling.StmtId
import optimization.Optimization

import scala.collection.immutable
import scala.collection.mutable.{ArrayBuffer, Map, Queue, Set}

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

  def getStmt(pos: StmtId): Option[IR] = {
    if (pos._2 >= 0)
      Option(pos._1.asInstanceOf[CFGBlock].statements(pos._2))
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

      def isInvariant(): Boolean = {
        stmt match {
          case d: Def => {
            d.getLoc.field.get match {
              case vdecl: VariableDeclaration => {
                if (d.asInstanceOf[Use].getUse.isEmpty) return true
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

              case _ => false
            }
          }
          case _ => false
        }
      }

      if (isInvariant()) {
        invariant += head
        q ++= graph(head)
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

  def useHelper(pos:StmtId): immutable.Set[Location] = {
    pos._1 match {
      case block:CFGBlock => {
        block.statements(pos._2).asInstanceOf[Use].getUse
      }
      case call:CFGMethodCall => {
        if (pos._2 == -1)
          call.params map (_.getUse) reduce(_ ++ _)
        else immutable.Set()
      }
      case cond:CFGConditional => {
        if (pos._2 == -1)
          cond.condition.getUse
        else immutable.Set()
      }
      case _ => immutable.Set()
    }
  }

  def judgeMov(invariant: Vector[StmtId], loop: LoopEntity[StmtId]): Vector[StmtId] = {
    (invariant
      filter (invar => loop.exits forall (dom(invar).contains(_)))
      filter (invar => {
      lazy val invarLoc = getStmt(invar).get.asInstanceOf[Def]
      loop.stmts forall (pos => {
        pos != invar || {
          val optIr = getStmt(pos)
          optIr match {
            case Some(ir) => {
              ir match {
                case d: Def => d.getLoc.field != invarLoc.getLoc.field
                case _ => true
              }
            }
            case _ => true
          }
        }
      })
    })
      filter (invar => {
      lazy val invarLoc = getStmt(invar).get.asInstanceOf[Def]
      loop.stmts forall (pos => {
        val use = useHelper(pos).filter(loc => {
          loc.field == invarLoc.getLoc.field})
        if (use.nonEmpty) {
          val reaching = reachingDef(use.head, pos)
          reaching.size == 1 && reaching.contains(invar)
        }
        else true
      })
    }))
  }

  def apply(cfg: CFG, isInit: Boolean = true): Unit = {
    if (isInit) {
      init()
    }

    LoopConstruction(cfg)
    val loops = LoopConstruction.loops
    graph = LoopConstruction.graph
    revGraph = LoopConstruction.revGraph
    dom = LoopConstruction.dom

    val toMove = loops map (
      loop => {
        val invariant = findInvariant(loop)
        val movable = judgeMov(invariant, loop)
        (loop, movable.sortBy(x => (x._1.label, -x._2)))
      })

    val conds = graph.keySet map (_._1) filter(_.isInstanceOf[CFGConditional]) map (_.asInstanceOf[CFGConditional])

    toMove foreach (pair => {
      val loopHeader = pair._1.header._1
      val invariant = pair._2
      val preHeader = CFGBlock(loopHeader.label+"_preheader", ArrayBuffer())
      preHeader.statements ++= invariant flatMap (getStmt(_))
      loopHeader.parents foreach (
        _ match {
          case cond: CFGConditional => {
            if (cond.next == Option(loopHeader)) {
              cond.next = None
              Destruct.link(cond, preHeader)
            }
            if (cond.ifFalse == Option(loopHeader)) {
              cond.ifFalse = None
              Destruct.linkFalse(cond, preHeader)
            }
          }
          case other => {
            if (other.next == Option(loopHeader)) {
              other.next = None
              Destruct.link(other, preHeader)
            }
          }
        })
      loopHeader.parents.clear()
      Destruct.link(preHeader, loopHeader)
      (conds filter (_.end == Option(loopHeader))
        foreach (_.end = Option(preHeader)))
    })
  }
}
