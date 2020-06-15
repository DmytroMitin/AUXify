package com.github.dmytromitin.auxify.shapeless

import shapeless.{Witness, the => s_the}
import shapeless.test.sameTyped
import shapeless.syntax.singleton._
import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec
import shapeless.tag.{@@, Tagged}

class StringSymbolTest extends AnyFlatSpec with Matchers {

  type Str = Witness.`"a"`.T
  type Symb = Witness.`'a`.T

  implicitly[StringToSymbol.Aux[Str, Symb]]
  val strToSymb = s_the[StringToSymbol[Str]]
  implicitly[strToSymb.Out =:= Symb]
  implicitly[Symb =:= strToSymb.Out]

  sameTyped[Symb](stringToSymbol("a"))(Symbol("a").narrow)
  sameTyped(stringToSymbol("a"))(Symbol("a").narrow)

  "stringToSymbol" should "work" in {
    stringToSymbol("a") === Symbol("a") should be (true)
  }

  implicitly[Witness.Aux[Symb]]
  implicitly[Witness.Aux[Witness.`'a`.T]]
  implicitly[Witness.Aux[Symbol @@ Str]]
  implicitly[Witness.Aux[Symbol @@ Witness.`"a"`.T]]
  implicitly[Witness.Aux[Symbol with Tagged[Str]]]
  type Smbl = Symbol
  type Tgd[A] = Tagged[A]
  implicitly[Witness.Aux[Smbl with Tgd[Str]]]
  implicitly[Witness.Aux[Symbol with Tagged[Witness.`"a"`.T]]]

  implicitly[SymbolToString.Aux[Symb, Witness.`"a"`.T]]
  implicitly[SymbolToString.Aux[Symb, Str]]
  val symbToStr = s_the[SymbolToString[Symb]]
  implicitly[symbToStr.Out =:= Str]
  implicitly[Str =:= symbToStr.Out]

  sameTyped[Str](symbolToString('a))("a".narrow)
  sameTyped[Str](symbolToString(Symbol("a")))("a".narrow)
  sameTyped(symbolToString(Symbol("a")))("a".narrow)

  "symbolToString" should "work" in {
    symbolToString(Symbol("a")) should be ("a")
  }
}