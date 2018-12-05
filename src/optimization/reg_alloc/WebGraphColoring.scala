package optimization.reg_alloc

import ir.components._

import scala.collection.mutable.{ArrayBuffer, Map, Set, Stack}

object WebGraphColoring {
  val validWebSet = Set[DefUseWeb]()
  val validWebMap = Map[DefUseWeb, Set[DefUseWeb]]()
  val activeWebMap = Map[DefUseWeb, Set[DefUseWeb]]()
  val webStack = Stack[DefUseWeb]()
  
  private def getWebSizeVec(): Vector[DefUseWeb] = {
    val seq = activeWebMap.toVector.sortBy(_._2.size)
    val retVec: Vector[DefUseWeb] = for (i <- seq) yield i._1
    retVec
  }

  private def getLeastSpill(ws: scala.collection.immutable.Set[DefUseWeb]): DefUseWeb = {
    val spillVec: Vector[DefUseWeb] = ws.toVector.sortWith(_.spillCost < _.spillCost)
    spillVec(0)
  }

  // push web to stack and remove it and its edges from activeWebMap
  private def pushWeb(web: DefUseWeb): Unit = {
    for (neigh <- activeWebMap(web)) {
      if (activeWebMap.contains(neigh)) { // if neighbor is in activeWebMap
        assert(activeWebMap(neigh).contains(web)) // the active interference of neighbor should contain web
        activeWebMap(neigh) -= web
      }
    }
    activeWebMap -= web // remove web
    webStack.push(web) // push web on stack
  }

  // pop a web from stack and restore its edges to the existing webs in activeWebMap
  private def popWeb(): DefUseWeb = {
    val newWeb: DefUseWeb = webStack.pop
    val newWebSet: Set[DefUseWeb] = validWebMap(newWeb).intersect(activeWebMap.keys.toSet)
    for (i <- activeWebMap.keys) { // restore edges
      assert(!activeWebMap(i).contains(newWeb)) // should have been removed
      if (validWebMap(i).contains(newWeb)) {
        activeWebMap(i) += newWeb
      }
    }
    activeWebMap += (newWeb -> newWebSet) // add newWeb to activeWebMap
    newWeb
  }

  // private def 

  // Assigns the registers to the DefUseWebs by setting register and isSpill for each DefUseWeb
  def apply(webSet: Set[DefUseWeb], regVector: Vector[Register], isInit: Boolean = true): Unit = {
    validWebSet.clear
    validWebMap.clear
    activeWebMap.clear
    webStack.clear
    // filter out the spilled DefUseWebs
    validWebSet ++= { for (i <- webSet if (!i.isSpill)) yield i }
    validWebSet foreach { i => validWebMap += (i -> i.interfereWith.intersect(validWebSet)) }
    // initialize activeWebMap
    activeWebMap ++= validWebMap

    // push all webs to stack
    while (!activeWebMap.isEmpty) {
      val currWeb: DefUseWeb = getWebSizeVec()(0) // get the web with smallest degree
      if (activeWebMap(currWeb).size >= regVector.size) { // not colorable
        val spillWeb: DefUseWeb = getLeastSpill(activeWebMap.keys.toSet) // spill a remaining web with the smallest spillCost
        spillWeb.isSpill = true
        WebGraphColoring(webSet, regVector) // retry coloring
        return
      } else { // colorable
        pushWeb(currWeb)
      }
    }

    // pop stack
    while (!webStack.isEmpty) {
      val currWeb: DefUseWeb = popWeb()
      for (i <- activeWebMap(currWeb)) assert(i.register.isDefined)
      val candidateArray = ArrayBuffer[Register](regVector : _*)
      for (i <- activeWebMap(currWeb)) {
        candidateArray -= i.register.get // remove neighbor colors
      }
      assert(!candidateArray.isEmpty)
      currWeb.register = Option(candidateArray(0)) // assign first register
    }
  }
}
