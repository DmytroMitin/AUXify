package com.github.dmytromitin.auxify.shapeless

import shapeless.{::, HNil, Poly1, Witness, poly}
import scala.language.experimental.macros

object stringToSymbolPoly extends Poly1 {
  implicit def cse[S <: String, S1 <: Symbol](implicit sts: StringToSymbol.Aux[S, S1], /*witness1: Witness.Aux[S],*/ witness: Witness.Aux[S1]): Case.Aux[S, S1] =
    at(_ => witness.value)

//  implicit def cse[S <: String, S1 <: Symbol]/*(implicit sts: StringToSymbol.Aux[S, S1], /*witness1: Witness.Aux[S],*/ witness: Witness.Aux[S1])*/: Case.Aux[S, S1] =
//  //  at((s: S) => stringToSymbol(s))
//    macro StringSymbolMacros.stringToSymbolPolyCseImpl[S, S1]
//  //stringToSymbolPoly.cse is not a valid implicit value for stringToSymbolPoly.Case.Aux[String("a"),Symbol with shapeless.tag.Tagged[String("a")]] because:
//  //hasMatchingSymbol reported error: type mismatch;
//  //found   : String("s1")
//  //required: <notype>
}

//low-priority implicits for 2.10
trait LowPrioritySymbolToStringPoly2 extends Poly1 {
  implicit def polyCase[S <: Symbol, S1 <: String](implicit sts: SymbolToString.Aux[S, S1], /*witness1: Witness.Aux[S],*/ witness: Witness.Aux[S1]): poly.Case.Aux[symbolToStringPoly.type, S :: HNil, S1] =
    poly.Case(_ => witness.value)
}
trait LowPrioritySymbolToStringPoly1 extends LowPrioritySymbolToStringPoly2 {
  implicit def polyCase1[S <: Symbol, S1 <: String](implicit sts: SymbolToString.Aux[S, S1], /*witness1: Witness.Aux[S],*/ witness: Witness.Aux[S1]): poly.Case1.Aux[symbolToStringPoly.type, S, S1] =
    poly.Case(_ => witness.value)
}
trait LowPrioritySymbolToStringPoly extends LowPrioritySymbolToStringPoly1 {
  implicit def productCase[S <: Symbol, S1 <: String](implicit sts: SymbolToString.Aux[S, S1], /*witness1: Witness.Aux[S],*/ witness: Witness.Aux[S1]): ProductCase.Aux[S :: HNil, S1] =
    at(_ => witness.value)
}
object symbolToStringPoly extends LowPrioritySymbolToStringPoly {
  implicit def cse[S <: Symbol, S1 <: String](implicit sts: SymbolToString.Aux[S, S1], /*witness1: Witness.Aux[S],*/ witness: Witness.Aux[S1]): Case.Aux[S, S1] =
    at(_ => witness.value)
}