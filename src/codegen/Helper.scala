package codegen

import scala.collection.mutable.ArrayBuffer
import ir.components._

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

  /**
    * judge if a expression is a location whose name ends with suffix.
  */
  def NameEndsWith(expr:Expression, suffix: String): Boolean = {
    if (!expr.isInstanceOf[Location]) return false
    assert(expr.asInstanceOf[Location].field.isDefined)
    val name = expr.asInstanceOf[Location].field.get.name
    if (name.length < 4) return false
    if (name.slice(name.length-4, name.length) == "_tmp") return true
    return false
  }
}
