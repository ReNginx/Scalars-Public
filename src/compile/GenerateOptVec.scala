package compile

import optimization._
import scala.collection.mutable.{ArrayBuffer, Map}

object GenerateOptVec {
  def apply(str2Opts: Map[String, Map[String, Option[Optimization]]],optFlagMap: Map[String, Boolean], optVec: Vector[String], optArg: String): Vector[Optimization] = {
    val retVec: ArrayBuffer[Optimization] = ArrayBuffer[Optimization]()
    for (opt <- optVec) {
      if (optFlagMap(opt)) {
        if (!str2Opts(opt)(optArg).isEmpty) { retVec += str2Opts(opt)(optArg).get }
      }
    }

    retVec.toVector
  }
}