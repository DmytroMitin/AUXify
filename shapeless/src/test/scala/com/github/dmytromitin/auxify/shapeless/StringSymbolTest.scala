package com.github.dmytromitin.auxify.shapeless

import org.scalatest._
import shapeless.tag.@@
import shapeless.{the => s_the}
import shapeless.test.sameTyped
import shapeless.syntax.singleton._
import org.scalatest.matchers.should.Matchers

class StringSymbolTest extends FlatSpec with Matchers {

  implicitly[StringToSymbol.Aux["a", Symbol @@ "a"]]
  val strToSymb = s_the[StringToSymbol["a"]]
  implicitly[strToSymb.Out =:= (Symbol @@ "a")]
  implicitly[(Symbol @@ "a") =:= strToSymb.Out]

  "stringToSymbol" should "work" in {
    sameTyped[Symbol @@ "a"](stringToSymbol("a"))(Symbol("a").narrow) should be ()
    sameTyped(stringToSymbol("a"))(Symbol("a").narrow) should be ()
  }

  implicitly[SymbolToString.Aux[Symbol @@ "a", "a"]]
  val symbToStr = s_the[SymbolToString[Symbol @@ "a"]]
  implicitly[symbToStr.Out =:= "a"]
  implicitly["a" =:= symbToStr.Out]

  "symbolToString" should "work" in {
    sameTyped["a"](symbolToString(Symbol("a")))("a".narrow) should be ()
    sameTyped(symbolToString(Symbol("a")))("a".narrow) should be ()
  }
}