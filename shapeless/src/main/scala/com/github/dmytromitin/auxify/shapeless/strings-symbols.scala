package com.github.dmytromitin.auxify.shapeless

import com.github.dmytromitin.auxify.macros.{apply, aux, instance}
import _root_.shapeless.SingletonTypeUtils
import macrocompat.bundle

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

@aux @apply @instance
trait SymbolToString[S <: Symbol] {
  type Out <: String with Singleton
}
trait LowPrioritySymbolToString {
  implicit def mkSymbolToStringUsingOut[S <: Symbol, Out <: String with Singleton]: SymbolToString.Aux[S, Out] =
    macro StringSymbolMacros.mkSymbolToString[S]
}
object SymbolToString extends LowPrioritySymbolToString {
  implicit def mkSymbolToString[S <: Symbol]: SymbolToString[S] = macro StringSymbolMacros.mkSymbolToString[S]
}

@aux @apply @instance
trait StringToSymbol[S <: String with Singleton] {
  type Out <: Symbol
}
trait LowPriorityStringToSymbol {
  implicit def mkStringToSymbolUsingOut[S <: String with Singleton, Out <: Symbol]: StringToSymbol.Aux[S, Out] =
    macro StringSymbolMacros.mkStringToSymbol[S]
}
object StringToSymbol extends LowPriorityStringToSymbol {
  implicit def mkStringToSymbol[S <: String with Singleton]: StringToSymbol[S] =
    macro StringSymbolMacros.mkStringToSymbol[S]
}

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
}