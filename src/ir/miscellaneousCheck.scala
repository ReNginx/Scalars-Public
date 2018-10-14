package ir

import ir.typed.{LocMethodDeclaration, VoidType}

object miscellaneousCheck {
  var noError: Boolean = true
  def apply: Unit = {
    val _main = SymbolTable.get("main")
    _main match {
      case None => {
        println("Program does not have main function")
        noError = false
      }
      case Some(x) => x match {
        case y: LocMethodDeclaration =>
          if (y.params.length > 0) {
            println("main should not take any input")
            noError = false
          }
          if (y.typ != Option(VoidType)) {
            println("main must return void")
            noError = false
          }
        case _ => {
          println("main must be a local method")
          noError = false
        }
      }
    }
  }
}
