package com.github.dmytromitin.auxify

import _root_.shapeless.Witness

package object shapeless {
  import scala.language.experimental.macros

//  def stringToSymbolHlp[S <: String /*with Singleton*/, S1 <: Symbol](s: S)(implicit
//                                                                             sts: StringToSymbol.Aux[S, S1],
//                                                                             witness: Witness.Aux[S1]): S1 = witness.value

  // doesn't work in 2.12-, have to use macro + .narrow instead of <: Singleton
//  def stringToSymbol[S <: String with Singleton, S1 <: Symbol](s: S)(implicit
//                                                                     sts: StringToSymbol.Aux[S, S1],
//                                                                     witness: Witness.Aux[S1]): S1 = witness.value

  def stringToSymbol(s: String): Symbol = macro StringSymbolMacros.stringToSymbolImpl

//   doesn't work in 2.10 (S1 is not inferred), have to resolve implicits manually
//    def symbolToStringHlp[S <: Symbol, S1 <: String /*with Singleton*/](s: S)(implicit
//                                                                          sts: SymbolToString.Aux[S, S1],
//                                                                          witness: Witness.Aux[S1]
//    ): S1 = witness.value

//  def symbolToStringHlp[S <: Symbol](s: S): String = macro StringSymbolMacros.symbolToStringHlpImpl[S]

  def symbolToString(s: Symbol): String = macro StringSymbolMacros.symbolToStringImpl
}
