package com.github.dmytromitin.auxify.shapeless

import com.github.dmytromitin.auxify.macros.{apply, aux, instance}
import shapeless.{Coproduct, DepFn1}
import shapeless.ops.coproduct.Mapper

object coproduct {
  @aux @apply @instance
  trait StringsToSymbols[C <: Coproduct] extends DepFn1[C] {
    type Out <: Coproduct
    def apply(c: C): Out
  }
  object StringsToSymbols {
    implicit def mkStringsToSymbols[L <: Coproduct](implicit
      mapper: Mapper[stringToSymbolPoly.type, L]): Aux[L, mapper.Out] = instance(mapper(_))
  }

  @aux @apply @instance
  trait SymbolsToStrings[C <: Coproduct] extends DepFn1[C] {
    type Out <: Coproduct
    def apply(c: C): Out
  }
  object SymbolsToStrings {
    implicit def mkSymbolsToStrings[C <: Coproduct](implicit
      mapper: Mapper[symbolToStringPoly.type, C]): Aux[C, mapper.Out] = instance(mapper(_))
  }
}
