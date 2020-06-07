package com.github.dmytromitin.auxify.shapeless

import com.github.dmytromitin.auxify.macros.{apply, aux, instance}
import scala.language.experimental.macros

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