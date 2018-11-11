package optimization

import codegen.{CFG, CFGConditional}

import scala.collection.mutable.{Map, Set}

object WorkList {

  def updateIn[T](in: Set[T], out: Set[T], update: String): Set[T] = {
    update match {
      case "union" => {
        in union out
      }

      case "intersection" => {
        in intersect out
      }

      case _ => throw new NotImplementedError()
    }
  }

  def succ(cfg: CFG): Vector[CFG] = {
    cfg match {
      case cond: CFGConditional => {
        Vector(cond.next, cond.ifFalse).flatten
      }
      case x => Vector(x.next).flatten
    }
  }

  def pred(cfg: CFG): Vector[CFG] = {
    cfg.parents.toVector
  }

  /**
    * please ensure that startFrom is a key in optGen and optKill
    * and optGen, optKill contain that same set of key. opt_in, opt_out would contain the same set of key.
    * this algorithm does not check this error.
    * This algorithm would simple ignore any cfg node that's not in the key set.
    *
    * @param optGen         : generated stuff within the list
    * @param optKill        : killed stuff within the list.
    * @param startFrom      : cfg block where the algorithm begins.
    * @param initialization : how to initialize nodes.
    * @param direction      : a string either "down" or "up"
    * @param updateOptIn    : a string either "union" or "intersection"
    * @return return opt_in, opt_out
    */
  def apply[T](optGen: Map[CFG, Set[T]],
               optKill: Map[CFG, Set[T]],
               startFrom: CFG,
               initialization: Set[T],
               direction: String,
               updateOptIn: String
              ): (Map[CFG, Set[T]], Map[CFG, Set[T]]) = {
    val optIn = Map[CFG, Set[T]]()
    val optOut = Map[CFG, Set[T]]()
    val list = Set[CFG]()
    val allCfgs = Set[CFG]()

    for ((node, value) <- optGen) {
      optOut(node) = initialization.clone
      list += node
      allCfgs += node
    }

    optIn(startFrom) = Set[T]()
    optOut(startFrom) = optGen(startFrom).clone

    list.remove(startFrom)

    while (list.nonEmpty) {
      val curr = list.head
      list.remove(curr)

      optIn(curr) = initialization.clone

      val nxtLst =
        direction match {
          case "up" => succ(curr)
          case "down" => pred(curr)
          case _ => throw new NotImplementedError()
        }

      for (nxt <- nxtLst) {
        optIn(curr) = updateIn[T](optIn(curr), optOut(nxt), updateOptIn)
      }

      val oldOut = optOut(curr)
      optOut(curr) = optGen(curr) union (optIn(curr) diff optKill(curr))

      if (oldOut != optOut(curr)) {
        list ++= nxtLst
      }
    }

    (optIn, optOut)
  }
}
