package com.github.dmytromitin.auxify.shapeless

import com.github.dmytromitin.auxify.macros.{apply, aux, instance, syntax}
import shapeless.{Coproduct, DepFn1, HList}
import shapeless.ops.union.UnzipFields
import shapeless.ops.coproduct.ZipWithKeys

object union {
  @aux @apply @instance @syntax
  trait StringsToSymbols[C <: Coproduct] /*extends DepFn1[C]*/ {
    type Out <: Coproduct
    def stringsToSymbols(c: C): Out
  }
  object StringsToSymbols {
    implicit def mkStringsToSymbols[C <: Coproduct, K <: HList, V <: Coproduct, K1 <: HList](implicit
      unzip: UnzipFields.Aux[C, K, V],
      sts: hlist.StringsToSymbols.Aux[K, K1],
      zip: ZipWithKeys[K1, V]
    ): Aux[C, zip.Out] = instance(l => zip(unzip.values(l)))
    //    instance(l => l.asInstanceOf[zip.Out]) // more efficient
  }

  @aux @apply @instance @syntax
  trait SymbolsToStrings[C <: Coproduct] /*extends DepFn1[C]*/ {
    type Out <: Coproduct
    def symbolsToStrings(c: C): Out
  }
  object SymbolsToStrings {
    implicit def mkSymbolsToStrings[C <: Coproduct, K <: HList, V <: Coproduct, K1 <: HList](implicit
      unzip: UnzipFields.Aux[C, K, V],
      sts: hlist.SymbolsToStrings.Aux[K, K1],
      zip: ZipWithKeys[K1, V]
    ): Aux[C, zip.Out] = instance(l => zip(unzip.values(l)))
    //    instance(l => l.asInstanceOf[zip.Out]) // more efficient
  }
}
