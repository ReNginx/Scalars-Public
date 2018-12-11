package ir.components

trait Def {
  def getLoc: Location
}

trait Use {
  def getUse: Set[Location] = {
    assert(this.isInstanceOf[Use])
    //System.err.println(s"in function getUse ${this.toString}")
    def getIndexUse(location: Location): Set[Location] = {
      if (location.index.isDefined)
        location.index.get.getUse
      else
        Set()
    }

    this match {
      case loc: Location => {
        Set(loc) ++ getIndexUse(loc)
      }

      case asgStmt: AssignmentStatements => {
        getIndexUse(asgStmt.loc) ++ asgStmt.value.getUse
      }

      case inc: Increment => {
        getIndexUse(inc.loc)
      }

      case dec: Decrement => {
        getIndexUse(dec.loc)
      }

      case unary: UnaryOperation => {
        getIndexUse(unary.eval.get) ++ unary.expression.getUse
      }

      case bin: BinaryOperation => {
        getIndexUse(bin.eval.get) ++ bin.lhs.getUse ++ bin.rhs.getUse
      }

      case ret: Return => {
        if (ret.value.isDefined)
          ret.value.get.getUse
        else
          Set()
      }

      case _ => Set()
    }
  }

  def getUseLst: Vector[Location] = {
    def getIndexUse(location: Location): Vector[Location] = {
      if (location.index.isDefined)
        location.index.get.getUseLst
      else
        Vector()
    }

    this match {
      case loc: Location => {
        Vector(loc) ++ getIndexUse(loc)
      }

      case asgStmt: AssignmentStatements => {
        getIndexUse(asgStmt.loc) ++ asgStmt.value.getUse
      }

      case inc: Increment => {
        getIndexUse(inc.loc)
      }

      case dec: Decrement => {
        getIndexUse(dec.loc)
      }

      case unary: UnaryOperation => {
        getIndexUse(unary.eval.get) ++ unary.expression.getUse
      }

      case bin: BinaryOperation => {
        getIndexUse(bin.eval.get) ++ bin.lhs.getUse ++ bin.rhs.getUse
      }

      case ret: Return => {
        if (ret.value.isDefined)
          ret.value.get.getUseLst
        else
          Vector()
      }

      case _ => Vector()
    }
  }
}
