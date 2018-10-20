package ir

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import ir.components._

/** Mutable ADT representing an item in SymbolTable stack.
 *
 * Do not use this class directly.
 */
protected class SymbolTableNode(parentTable: Option[SymbolTableNode]) {

  private val _map = HashMap[String, MemberDeclaration]()

  def parent = parentTable

  /** Add a symbol to this symbol table.
   *
   * @param symbol the symbol to add
   * @return true if this symbol already exists in this scope, false otherwise
   *         if true, the previous symbol is not overwritten
   */
  def add(symbol: MemberDeclaration): Boolean = {
    if (_map contains symbol.name) {
      val prev = (_map get symbol.name).get
      println(s"line ${symbol.line}, col ${symbol.col}, symbol ${symbol.name} has been declared at line ${prev.line}, col ${prev.col}")
      true
    } else {
      _map += (symbol.name -> symbol)
      false
    }
  }

  /** Get the symbol with the given name.
   *
   * If the symbol is not found in this immediate table, recursively
   * searches its parent tables.
   *
   * @param name symbol name to get
   * @return symbol with the given name if it exists in this scope, None otherwise
   */
  def get(name: String): Option[MemberDeclaration] = {
    if (_map contains name) {
      _map get name
    } else if (parent.isEmpty) {
      None
    } else {
      parent.get get name
    }
  }

  /** Check if name maps to any descriptor in this scope.
   *
   * @param name symbol name to check
   * @return true if a symbol with the given name exists in this scope, false otherwise
   */
  def contains(name: String): Boolean = {
    if (_map contains name) {
      true
    } else if (parent.isEmpty) {
      false
    } else {
      parent contains name
    }
  }
}

/** Mutable ADT representing a stack of SymbolTableNode's.
 *
 * Conceptually, this object is a stack whose items are SymbolTableNode.
 * The item on top represents the "current" environmental scope, with its
 * parent scopes placed below it.
 *
 * A recommended usage pattern is the following:
 *   1. Create a new scope with push()
 *   2. Add/get symbols to it with add() and get()
 *   3. Remove the current scope with pop() when exiting the scope
 *   4. Repeat
 */
object SymbolTable {
  private var top = new SymbolTableNode(None)

  /** Create a new symbol table and push it to the top of the stack.
   *
   * The parent table of the newly-created table is the one that
   * was previously at the top of the stack, which is now below the
   * new table.
   */
  def push(): Unit = {
    val newTop = new SymbolTableNode(Option(top))
    top = newTop
  }

  /** Identical to SymbolTableNode.get
   */
  def get(name: String): Option[MemberDeclaration] = {
    top.get(name)
  }

  /** Add a symbol to the table of the current scope.
   *
   * Identical to SymbolTableNode.add
   */
  def add(symbol: MemberDeclaration): Boolean = {
    top.add(symbol)
  }

  /** Remove the current table, which is at the top of the stack.
   *
   * The table that becomes the top of the stack after this operation
   * is the parent of the removed table.
   *
   * If the table to be removed doesn't contain a parent, then nothing happens.
   */
  def pop(): Unit = {
    if (top.parent.isDefined) {
      top = top.parent.get
    }
  }
}
