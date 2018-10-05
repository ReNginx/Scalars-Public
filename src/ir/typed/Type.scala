package ir.typed

trait Type extends IR {
  def line: Int = 0
  def col: Int = 0
}

case object BoolType extends Type {
  def typ: Type = this
  override def toString: String = s"BoolType"
}

case object BoolArrayType extends Type {
  def typ: Type = this
  override def toString: String = s"BoolArrayType"
}

case object CharType extends Type {
  def typ: Type = this
  override def toString: String = s"CharType"
}

case object IntType extends Type {
  def typ: Type = this
  override def toString: String = s"IntType"
}

case object IntegerArrayType extends Type {
  def typ: Type = this
  override def toString: String = s"IntegerArrayType"
}

case object StringType extends Type {
  def typ: Type = this
  override def toString: String = s"StringType"
}

case object VoidType extends Type {
  def typ: Type = this
  override def toString: String = s"VoidType"
}
