package com.github.dmytromitin.auxify.shapeless

import com.github.dmytromitin.auxify.macros.{apply, aux, instance}
import shapeless.labelled.{FieldType, field}
import shapeless.{::, DepFn1, HList, HNil, Witness}
import shapeless.ops.record.UnzipFields
import shapeless.ops.hlist.ZipWithKeys

object record {
  @aux @apply @instance
  trait StringsToSymbols[L <: HList] extends DepFn1[L] {
    type Out <: HList
    def apply(l: L): Out
  }
  object StringsToSymbols {
    implicit def mkStringsToSymbols[L <: HList, K <: HList, V <: HList, K1 <: HList, Out <: HList](implicit
      unzip: UnzipFields.Aux[L, K, V],
      sts: hlist.StringsToSymbols.Aux[K, K1],
      zip: ZipWithKeys.Aux[K1, V, Out]
    ): Aux[L, /*zip.Out*/Out] = instance(l => zip(unzip.values(l)))
//    instance(l => l.asInstanceOf[zip.Out]) // more efficient
  }

  @aux @apply @instance
  trait SymbolsToStrings[L <: HList] extends DepFn1[L] {
    type Out <: HList
    def apply(l: L): Out {}
  }
  object SymbolsToStrings {
//    implicit def mkSymbolsToStrings[L <: HList, K <: HList, V <: HList, K1 <: HList, Out <: HList](implicit
//      unzip: UnzipFields.Aux[L, K, V],
//      sts: hlist.SymbolsToStrings.Aux[K, K1],
//      zip: ZipWithKeys.Aux[K1, V, Out]
//    ): Aux[L, /*zip.Out*/Out {}] = instance(l => zip(unzip.values(l)))
//    //    instance(l => l.asInstanceOf[zip.Out]) // more efficient

    implicit val hnilSymbolsToStrings: Aux[HNil, HNil] = instance(_ => HNil)

    implicit def hconsSymbolsToStrings[K <: Symbol, V, T <: HList, K1 <: String, Out <: HList]
    (implicit sts: SymbolToString.Aux[K, K1], sts1: SymbolsToStrings.Aux[T, Out]): Aux[FieldType[K, V] :: T, FieldType[K1, V] :: Out] =
      instance(l => field[K1](l.head: V) :: sts1(l.tail))
  }

//  @aux @apply @instance
//  trait UnzipFields[L <: HList] extends Serializable {
//    type Keys <: HList
//    type Values <: HList
//
//    def keys(): Keys
//    def values(l: L): Values
//  }
//  object UnzipFields {
//    implicit def hnilUnzipFields[L <: HNil]: Aux[L, HNil, L] = instance(() => HNil, identity)
//
//    implicit def hconsUnzipFields[K, V, T <: HList, OutK <: HList, OutV <: HList](implicit
//                                                    key: Witness.Aux[K],
//                                                    tailUF: UnzipFields.Aux[T, OutK, OutV]
//                                                   ): Aux[FieldType[K, V] :: T, K :: OutK/*tailUF.Keys*/, V :: OutV/*tailUF.Values*/] =
//      instance(() => key.value :: tailUF.keys(), l => l.head :: tailUF.values(l.tail))
//  }

//  @aux @apply @instance
//  trait ZipWithKeys[K <: HList, V <: HList] extends DepFn1[V] with Serializable {
//    type Out <: HList
//    def apply(t: V): Out
//  }
//  object ZipWithKeys {
//    implicit val hnilZipWithKeys: Aux[HNil, HNil, HNil] = instance(_ => HNil)
//
//    implicit def hconsZipWithKeys[KH, VH, KT <: HList, VT <: HList, ZwkOut <: HList]
//    (implicit zipWithKeys: ZipWithKeys.Aux[KT, VT, ZwkOut], wkh: Witness.Aux[KH])
//    : Aux[KH :: KT, VH :: VT, FieldType[KH, VH] :: ZwkOut] =
//      instance(v => field[wkh.T](v.head) :: zipWithKeys(v.tail))
//  }
}
