package ir

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import ir.typed._

/**
 * Abstract type `D` is is one of the subclasses of `Descriptor`
 * SymbolTable is mutable.
 */
class SymbolTableNode(parentTable: Option[SymbolTableNode]) {
  private val _parent = parentTable
  private val _map = HashMap[String, MemberDeclaration]()

  // parent
  def parent = _parent

  def add(symbol: MemberDeclaration): Unit = {
    if (_map contains  symbol.name) {
      println(s"line ${}, col ${}, symbol ${symbol.name} has been declared")
    }
    else {
      _map += (symbol.name -> symbol)
    }
  }

  /**
   * Get the descriptor with this name
   *
   */
  def get(name: String): Option[MemberDeclaration] = {
    if (_map contains name) {
      _map get name
    } else if (_parent.isEmpty) {
      None
    } else {
      _parent.get get name
    }
  }

  /**
   * Check if name maps to any descriptor in this table or any of the parent tables
   */
  def contains(name: String): Boolean = {
    if (_map contains name) {
      true
    } else if (_parent.isEmpty) {
      false
    } else {
      _parent contains name
    }
  }
}

object SymbolTable {
  var top = new SymbolTableNode(None)
  def apply: Unit = {

  }

  def push: Unit = {
    val newTop = new SymbolTableNode(Option(top))
    top = newTop
  }

  def get(name: String): Option[MemberDeclaration] = {
    top.get(name)
  }

  def add(symbol: MemberDeclaration): Unit = {
    top.add(symbol)
  }

  def pop(): Unit = {
    if (!top.parent.isEmpty) {
      top = top.parent.get
    }
  }
}