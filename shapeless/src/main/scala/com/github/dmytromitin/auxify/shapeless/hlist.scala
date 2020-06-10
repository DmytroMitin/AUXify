package com.github.dmytromitin.auxify.shapeless

import com.github.dmytromitin.auxify.macros.{apply, aux, instance}
import shapeless.{DepFn1, HList}
import shapeless.ops.hlist.Mapper

object hlist {
  @aux @apply @instance
  trait StringsToSymbols[L <: HList] extends DepFn1[L] {
    type Out <: HList
    def apply(l: L): Out // otherwise @instance doesn't work
  }
  object StringsToSymbols {
    implicit def mkStringsToSymbols[L <: HList](implicit
      mapper: Mapper[stringToSymbolPoly.type, L]): Aux[L, mapper.Out] = instance(mapper(_))
  }

  @aux @apply @instance
  trait SymbolsToStrings[L <: HList] extends DepFn1[L] {
    type Out <: HList
    def apply(l: L): Out
  }
  object SymbolsToStrings {
    implicit def mkSymbolsToStrings[L <: HList](implicit
      mapper: Mapper[symbolToStringPoly.type, L]): Aux[L, mapper.Out] = instance(mapper(_))
  }
}
