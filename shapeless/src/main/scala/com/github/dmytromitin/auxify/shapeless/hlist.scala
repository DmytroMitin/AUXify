package com.github.dmytromitin.auxify.shapeless

import com.github.dmytromitin.auxify.macros.{apply, aux, instance, syntax}
import shapeless.{::, DepFn1, HList, HNil, Poly, poly}
import shapeless.ops.hlist.Mapper

object hlist {
  @aux @apply @instance @syntax
  trait StringsToSymbols[L <: HList] {
    type Out <: HList
    def stringsToSymbols(l: L): Out
  }
  object StringsToSymbols {
    //also ok
//    implicit def mkStringsToSymbols[L <: HList, Out <: HList](implicit
//      mapper: Mapper.Aux[stringToSymbolPoly.type, L, Out]): Aux[L, Out] = instance(mapper(_))

    implicit def mkStringsToSymbols[L <: HList](implicit
      mapper: Mapper[stringToSymbolPoly.type, L]): Aux[L, mapper.Out] = instance(mapper(_))
  }

  @aux @apply @instance @syntax
  trait SymbolsToStrings[L <: HList] {
    type Out <: HList
    def symbolsToStrings(l: L): Out
  }
  object SymbolsToStrings {
    //also ok
//    implicit def mkSymbolsToStrings[L <: HList, Out <: HList](implicit
//      mapper: Mapper.Aux[symbolToStringPoly.type, L, Out]): Aux[L, Out] = instance(mapper(_))

    implicit def mkSymbolsToStrings[L <: HList](implicit
      mapper: Mapper[symbolToStringPoly.type, L]): Aux[L, mapper.Out] = instance(mapper(_))
  }

//  @aux @apply @instance
//  trait Mapper[P, L <: HList] extends DepFn1[L] with Serializable {
//    type Out <: HList
//    def apply(t: L): Out
//  }
//  object Mapper {
//    implicit def hnil[P]: Aux[P, HNil, HNil] = instance(_ => HNil)
//
//    //doesn't work in 2.10
////    implicit def hcons[P <: Poly, H, T <: HList, P_H, Out <: HList]
////    (implicit cse: poly.Case1.Aux[P, H, P_H], mapper : Mapper.Aux[P, T, Out]): Aux[P, H :: T, P_H :: Out] =
////      instance(l => cse(l.head) :: mapper(l.tail))
//
//    //also ok, like standard Mapper
////    implicit def hcons[P <: Poly, H, T <: HList, Out <: HList]
////    (implicit cse: poly.Case1[P, H], mapper : Mapper.Aux[P, T, Out]): Aux[P, H :: T, cse.Result :: Out] =
////      instance(l => cse(l.head) :: mapper(l.tail))
//
//    //ok
//    implicit def hcons[P <: Poly, H, T <: HList]
//    (implicit cse: poly.Case1[P, H], mapper : Mapper[P, T]): Aux[P, H :: T, cse.Result :: mapper.Out] =
//      instance(l => cse(l.head) :: mapper(l.tail))
//  }
}
