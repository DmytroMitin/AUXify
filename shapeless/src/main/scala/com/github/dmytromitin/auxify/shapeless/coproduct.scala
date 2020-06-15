package com.github.dmytromitin.auxify.shapeless

import com.github.dmytromitin.auxify.macros.{apply, aux, instance, syntax}
import shapeless.{:+:, CNil, Coproduct, DepFn1, Inl, Inr, Poly, poly}
//import shapeless.ops.coproduct.Mapper

object coproduct {
  @aux @apply @instance @syntax
  trait StringsToSymbols[C <: Coproduct] {
    type Out <: Coproduct
    def stringsToSymbols(c: C): Out
  }
  object StringsToSymbols {
    implicit def mkStringsToSymbols[L <: Coproduct](implicit
      mapper: Mapper[stringToSymbolPoly.type, L]): Aux[L, mapper.Out] = instance(mapper(_))

    //also ok
//    implicit def mkStringsToSymbols[L <: Coproduct, Out <: Coproduct](implicit
//      mapper: Mapper.Aux[stringToSymbolPoly.type, L, Out]): Aux[L, Out] = instance(mapper(_))
  }

  @aux @apply @instance @syntax
  trait SymbolsToStrings[C <: Coproduct] {
    type Out <: Coproduct
    def symbolsToStrings(c: C): Out
  }
  object SymbolsToStrings {
    implicit def mkSymbolsToStrings[C <: Coproduct](implicit
      mapper: Mapper[symbolToStringPoly.type, C]): Aux[C, mapper.Out] = instance(mapper(_))

    //also ok
//    implicit def mkSymbolsToStrings[C <: Coproduct, Out <: Coproduct](implicit
//      mapper: Mapper.Aux[symbolToStringPoly.type, C, Out]): Aux[C, Out] = instance(mapper(_))
  }

  @aux @apply @instance
  trait Mapper[P <: Poly, C <: Coproduct] extends DepFn1[C] with Serializable {
    type Out <: Coproduct
    def apply(c: C): Out
  }

  object Mapper {
    implicit def cnilMapper[P <: Poly]: Aux[P, CNil, CNil] = instance(identity)

    //ok
    implicit def cconsMapper[P <: Poly, H, T <: Coproduct]
    (implicit cse: poly.Case1[P, H], mapper: Mapper[P, T]): Aux[P, H :+: T, cse.Result :+: mapper.Out] =
      instance(_.eliminate(h => Inl(cse(h)), t => Inr(mapper(t))))

    // doesn't work in 2.10, like standard Mapper
//    implicit def cconsMapper[P <: Poly, H, P_H, T <: Coproduct]
//    (implicit cse: poly.Case1.Aux[P, H, P_H], mapper: Mapper[P, T]): Aux[P, H :+: T, P_H :+: mapper.Out] =
//      instance(_.eliminate(h => Inl(cse(h)), t => Inr(mapper(t))))

    //also ok
//    implicit def cconsMapper[P <: Poly, H, P_H, T <: Coproduct, Out <: Coproduct]
//    (implicit cse: poly.Case1.Aux[P, H, P_H], mapper: Mapper.Aux[P, T, Out]): Aux[P, H :+: T, P_H :+: Out] =
//      instance(_.eliminate(h => Inl(cse(h)), t => Inr(mapper(t))))
  }
}
