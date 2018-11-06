package optimization

import ir.components._

import scala.collection.mutable.ArrayBuffer

// Symbolic value
case class SymVal (value: Int) extends SingleExpr {
  override def hashCode: Int = value.hashCode
  override def equals(obj: Any): Boolean = {
    obj.isInstanceOf[SymVal] &&
    obj.hashCode == this.hashCode
  }

  override def eval: Option[ir.components.Expression] = None // not used
  override def typ: Option[ir.components.Type] = None // not used
}

/*
case class ValList () {
  val vacantList: ArrayBuffer[SymVal] = ArrayBuffer[SymVal]().sortWith(_.value < _.value)
  var nextVacant: SymVal = SymVal(0)

  def addVal(): SymVal = {
    var retVal: SymVal = SymVal(0)
    if vacantList.length > 0) {
      retVal = vacantList.sorted(0)
      vacantList -= retVal
    } else {
      retVal = nextVacant
      nextVacant = SymVal(nextVacant.value + 1)
    }

    retVal
  }

  def removeVal(val: SymVal): Unit = {
    assert(val.value < nextVacant.value)
    assert(!vacantList.contains(val))
    vacantList += val
  }
}
*/