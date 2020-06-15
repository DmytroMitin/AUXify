package com.github.dmytromitin.auxify.shapeless

import com.github.dmytromitin.auxify.macros.{apply, aux, instance}
import shapeless.Witness
import shapeless.tag.@@

import scala.language.experimental.macros

@aux @apply @instance
trait StringToSymbol[S <: String /*with Singleton*/] {
  type Out <: Symbol
}
trait LowPriorityStringToSymbol {
  //necessary if using macro mkStringToSymbol
//  implicit def mkStringToSymbolUsingOut[S <: String /*with Singleton*/, Out <: Symbol]: StringToSymbol.Aux[S, Out] =
//    macro StringSymbolMacros.mkStringToSymbolImpl[S]
}
object StringToSymbol extends LowPriorityStringToSymbol {
  //also ok
//  implicit def mkStringToSymbol[S <: String /*with Singleton*/]: StringToSymbol[S] =
//    macro StringSymbolMacros.mkStringToSymbolImpl[S]
  implicit def mkStringToSymbol[S <: String](implicit witness: Witness.Aux[S]/*, witness1: Witness.Aux[Symbol @@ S]*/): StringToSymbol.Aux[S, Symbol @@ S] = null
}

@aux @apply @instance
trait SymbolToString[S <: Symbol] {
  type Out <: String /*with Singleton*/
}
trait LowPrioritySymbolToString {
  //necessary if using macro mkSymbolToString
//  implicit def mkSymbolToStringUsingOut[S <: Symbol, Out <: String /*with Singleton*/]: SymbolToString.Aux[S, Out] =
//    macro StringSymbolMacros.mkSymbolToStringImpl[S]
}
object SymbolToString extends LowPrioritySymbolToString {
  //also ok
//  implicit def mkSymbolToString[S <: Symbol]: SymbolToString[S] = macro StringSymbolMacros.mkSymbolToStringImpl[S]
  implicit def mkSymbolToString[S <: String](implicit witness: Witness.Aux[S]/*, witness1: Witness.Aux[Symbol @@ S]*/): SymbolToString.Aux[Symbol @@ S, S] = null
}