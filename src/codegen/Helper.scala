package codegen

import scala.collection.mutable.ArrayBuffer

object Helper {
  def outputMov(from: String, to: String): Vector[String] = {
    val array = ArrayBuffer[String]()
    if (from(0) == '%' || to(0) == '%') {
      array += s"\tmovq ${from}, ${to}"
    }
    else {
      array += s"\tmovq ${from}, %rax"
      array += s"\tmovq %rax, ${to}"
    }

    array.toVector
  }
}
