package com.github.dmytromitin.auxify.shapeless

import org.scalatest._
import shapeless.{Witness, the => s_the}
import shapeless.test.sameTyped
import shapeless.syntax.singleton._
import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec

class StringSymbolTest extends AnyFlatSpec with Matchers {

  implicitly[StringToSymbol.Aux[Witness.`"a"`.T, Witness.`'a`.T]]
  val strToSymb = s_the[StringToSymbol[Witness.`"a"`.T]]
  implicitly[strToSymb.Out =:= Witness.`'a`.T]
  implicitly[Witness.`'a`.T =:= strToSymb.Out]

  "stringToSymbol" should "work" in {
    sameTyped[Witness.`'a`.T](stringToSymbol("a"))(Symbol("a").narrow) should be (())
    sameTyped(stringToSymbol("a"))(Symbol("a").narrow) should be (())
  }

  implicitly[SymbolToString.Aux[Witness.`'a`.T, Witness.`"a"`.T]]
  val symbToStr = s_the[SymbolToString[Witness.`'a`.T]]
  implicitly[symbToStr.Out =:= Witness.`"a"`.T]
  implicitly[Witness.`"a"`.T =:= symbToStr.Out]

  "symbolToString" should "work" in {
    sameTyped[Witness.`"a"`.T](symbolToString('a))("a".narrow) should be (())
    sameTyped(symbolToString(Symbol("a")))("a".narrow) should be (())
  }
}