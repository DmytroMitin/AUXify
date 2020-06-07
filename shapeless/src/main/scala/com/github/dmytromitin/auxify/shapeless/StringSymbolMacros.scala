package com.github.dmytromitin.auxify.shapeless

import macrocompat.bundle
import shapeless.SingletonTypeUtils
import scala.reflect.macros.whitebox

@bundle
class StringSymbolMacros(val c: whitebox.Context) extends SingletonTypeUtils {
  import c.universe._

  def mkStringToSymbol[S <: String with Singleton : WeakTypeTag]: Tree = {
    val typ = weakTypeOf[S]
    val symbolType = SingletonSymbolType.unrefine(typ) match {
      case ConstantType(Constant(s: String)) => SingletonSymbolType(s)
      case _ => c.abort(c.enclosingPosition, s"$typ=${showRaw(typ)} is not string singleton type")
    }
    q"_root_.com.github.dmytromitin.auxify.shapeless.StringToSymbol.instance[$typ, $symbolType]()"
  }

  def mkSymbolToString[S <: scala.Symbol : WeakTypeTag]: Tree = {
    val typ = weakTypeOf[S]
    val stringType = typ match {
      case SingletonSymbolType(s) => internal.constantType(Constant(s))
      case _ => c.abort(c.enclosingPosition, s"$typ=${showRaw(typ)} is not symbol singleton type")
    }
    q"_root_.com.github.dmytromitin.auxify.shapeless.SymbolToString.instance[$typ, $stringType]()"
  }

  def stringToSymbol(s: Tree): Tree = {
    q"""
      import _root_.shapeless.syntax.singleton._
      _root_.com.github.dmytromitin.auxify.shapeless.`package`.stringToSymbolHlp($s.narrow)
    """
  }

  def symbolToString(s: Tree): Tree = {
    q"""
      import _root_.shapeless.syntax.singleton._
      _root_.com.github.dmytromitin.auxify.shapeless.`package`.symbolToStringHlp($s.narrow)
    """
  }
}