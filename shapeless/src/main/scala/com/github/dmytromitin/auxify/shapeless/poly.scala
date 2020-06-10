package com.github.dmytromitin.auxify.shapeless

import shapeless.{Poly1, Witness}

object stringToSymbolPoly extends Poly1 {
  implicit def cse[S <: String, S1 <: Symbol](implicit sts: StringToSymbol.Aux[S, S1], witness: Witness.Aux[S1]): Case.Aux[S, S1] =
    at(_ => witness.value)
}

object symbolToStringPoly extends Poly1 {
  implicit def cse[S <: Symbol, S1 <: String](implicit sts: SymbolToString.Aux[S, S1], witness: Witness.Aux[S1]): Case.Aux[S, S1] =
    at(_ => witness.value)
}