package optimization

import scala.collection.mutable.ArrayBuffer

// Symbolic value
case class SymVal (value: Int) {
  override def hashCode: Int = value.hashCode
  override def equals(obj: Any): Boolean = {
    obj.isInstanceOf[SymVal] &&
    obj.hashCode == this.hashCode
  }
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