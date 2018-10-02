package ir

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

/**
 * Abstract type `D` is is one of the subclasses of `Descriptor`
 * SymbolTable is mutable.
 */
abstract class SymbolTable[D<:Descriptor](parentTable: Option[SymbolTable[D]], symbols: Vector[D]) {
  private val _parent = parentTable
  private val _map = HashMap(symbols map { s => (s.name, s) }: _*)

  // parent
  def parent = _parent

  def add(symbol: D): Unit = {
    _map += (symbol.name -> symbol)
  }

  /**
   * Get the descriptor with this name
   *
   */
  def get(name: String): Option[Descriptor] = {
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
