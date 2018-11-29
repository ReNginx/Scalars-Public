package compile

import optimization._
import scala.collection.mutable.{ArrayBuffer, Map}

object GenerateOptVec {
  def apply(str2Opts: Map[String, Map[String, Optimization]],optFlagMap: Map[String, Boolean], optVec: Vector[String], optArg: String): Vector[Optimization] = {
    val retVec: ArrayBuffer[Optimization] = ArrayBuffer[Optimization]()
    for (opt <- optVec) {
      if (optFlagMap(opt)) {
        retVec += str2Opts(opt)(optArg)
      }
    }

    retVec.toVector
  }
}