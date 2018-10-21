package ir

import ir.components.{LocMethodDeclaration, VoidType}

object MiscCheck {
  var error: Boolean = false
  def apply: Unit = {
    val _main = SymbolTable.get("main")
    _main match {
      case None => {
        println("Program does not have main function")
        error = true
      }
      case Some(x) => x match {
        case y: LocMethodDeclaration =>
          if (y.params.length > 0) {
            println("main should not take any input")
            error = true
          }
          if (y.typ != Option(VoidType)) {
            println("main must return void")
            error = true
          }
        case _ => {
          println("main must be a local method")
          error = true
        }
      }
    }
  }
}
