package optimization

import codegen.CFG


object WorkList {

  def apply(optGen:Map[CFG, Set[Any]],
            optKill:Map[CFG, Set[Any]],
            startFrom:CFG,
            initialization:Set[Any],
            direction:String, // either "down" or "up"
            updateOptIn:String //either "union" or "intersection"
           ) : (Map[CFG, Set[Any]], Map[CFG, Set[Any]]) = {
    throw new NotImplementedError()
  }
}
