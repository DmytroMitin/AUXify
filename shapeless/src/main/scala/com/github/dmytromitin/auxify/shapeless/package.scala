package com.github.dmytromitin.auxify

package object shapeless {
  import scala.language.experimental.macros

  def stringToSymbol(s: String): Symbol = macro StringSymbolMacros.stringToSymbolImpl
  def symbolToString(s: Symbol): String = macro StringSymbolMacros.symbolToStringImpl
}
