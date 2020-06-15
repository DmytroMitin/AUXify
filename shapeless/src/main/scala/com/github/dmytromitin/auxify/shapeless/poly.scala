package com.github.dmytromitin.auxify.shapeless

import shapeless.tag.@@
import shapeless.{::, HNil, Poly1, Witness, poly}

import scala.language.experimental.macros

object stringToSymbolPoly extends Poly1 {
  implicit def cse[S <: String, S1 <: Symbol](implicit sts: StringToSymbol.Aux[S, S1], witness1: Witness.Aux[S], witness: Witness.Aux[S1]): Case.Aux[S, S1] =
    at(_ => witness.value)

  //Information: shapeless.this.StringToSymbol.mkStringToSymbol is not a valid implicit value for
  // com.github.dmytromitin.auxify.shapeless.StringToSymbol.Aux[s.type,S1] because:
  //hasMatchingSymbol reported error: s.type=RefinedType(List(SingleType(NoPrefix, TermName("s"))), Scope())
  // is not string singleton type
  //    at((s: S) => stringToSymbol(s))
  //  implicit def cse[S <: String](implicit sts: StringToSymbol[S]): Case.Aux[S, sts.Out] = macro StringSymbolMacros.stringToSymbolPolyCseImpl[S]
  //    at((s: S) => stringToSymbol(s))
}

//for 2.10
trait LowPrioritySymbolToStringPoly2 extends Poly1 {
  implicit def cse4[S <: Symbol, S1 <: String](implicit sts: SymbolToString.Aux[S, S1], witness1: Witness.Aux[S], witness: Witness.Aux[S1]): poly.Case.Aux[symbolToStringPoly.type, S :: HNil, S1] =
    poly.Case(_ => witness.value)
}
trait LowPrioritySymbolToStringPoly1 extends LowPrioritySymbolToStringPoly2 {
  implicit def cse2[S <: Symbol, S1 <: String](implicit sts: SymbolToString.Aux[S, S1], witness1: Witness.Aux[S], witness: Witness.Aux[S1]): poly.Case1.Aux[symbolToStringPoly.type, S, S1] =
    poly.Case(_ => witness.value)
}
trait LowPrioritySymbolToStringPoly extends LowPrioritySymbolToStringPoly1 {
  implicit def cse5[S <: Symbol, S1 <: String](implicit sts: SymbolToString.Aux[S, S1], witness1: Witness.Aux[S], witness: Witness.Aux[S1]): ProductCase.Aux[S :: HNil, S1] =
    at(_ => witness.value)
}

object symbolToStringPoly extends LowPrioritySymbolToStringPoly {
  //  implicit def cse[S <: Symbol]: Case[S] = macro StringSymbolMacros.symbolToStringPolyCseImpl[S]
  //
  //  implicit def cse3[S <: Symbol]: poly.Case1[symbolToStringPoly.type, S] = macro StringSymbolMacros.symbolToStringPolyCseImpl3[S]

  implicit def cse1[S <: Symbol, S1 <: String](implicit sts: SymbolToString.Aux[S, S1], witness1: Witness.Aux[S], witness: Witness.Aux[S1]): Case.Aux[S, S1] =
    at(_ => witness.value)
}