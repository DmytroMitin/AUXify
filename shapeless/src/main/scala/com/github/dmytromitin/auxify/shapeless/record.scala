package com.github.dmytromitin.auxify.shapeless

import com.github.dmytromitin.auxify.macros.{apply, aux, instance}
import shapeless.{DepFn1, HList}
import shapeless.ops.record.UnzipFields
import shapeless.ops.hlist.ZipWithKeys

object record {
  @aux @apply @instance
  trait StringsToSymbols[L <: HList] extends DepFn1[L] {
    type Out <: HList
    def apply(l: L): Out
  }
  object StringsToSymbols {
    implicit def mkStringsToSymbols[L <: HList, K <: HList, V <: HList, K1 <: HList](implicit
      unzip: UnzipFields.Aux[L, K, V],
      sts: hlist.StringsToSymbols.Aux[K, K1],
      zip: ZipWithKeys[K1, V]
    ): Aux[L, zip.Out] = instance(l => zip(unzip.values(l)))
//    instance(l => l.asInstanceOf[zip.Out]) // more efficient
  }

  @aux @apply @instance
  trait SymbolsToStrings[L <: HList] extends DepFn1[L] {
    type Out <: HList
    def apply(l: L): Out
  }
  object SymbolsToStrings {
    implicit def mkSymbolsToStrings[L <: HList, K <: HList, V <: HList, K1 <: HList](implicit
      unzip: UnzipFields.Aux[L, K, V],
      sts: hlist.SymbolsToStrings.Aux[K, K1],
      zip: ZipWithKeys[K1, V]
    ): Aux[L, zip.Out] = instance(l => zip(unzip.values(l)))
    //    instance(l => l.asInstanceOf[zip.Out]) // more efficient
  }
}
