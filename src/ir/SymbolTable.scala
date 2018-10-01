package ir

import scala.collection.immutable.HashMap
import scala.collection.mutable.ListBuffer

/**
 * Abstract type `T` is is one of the subclasses of `Descriptor`
 */
abstract class SymbolTable[T](parentTable: SymbolTable[Descriptor], symbols: Vector[T]) {
  private val _parent = parentTable
  private val _map = createMap(symbols)

  // parent
  def parent = _parent

  /**
   * Create a name->descriptor map out of symbols, to be used for symbol lookups
   * Preserve the order in which the symbols show, for parameters and what not
   */
  private def createMap(symbols: Vector[T]): HashMap[String, T] = {
    throw new Exception()
  }

  /**
   * Get the descriptor with this name
   */
  def get(name: String): T = throw new Exception()

  /**
   * Check if name maps to any descriptor in this table or any of the parent tables
   */
  def in(name: String): Boolean = false
}
