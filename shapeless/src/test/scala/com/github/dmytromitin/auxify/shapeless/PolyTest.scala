package com.github.dmytromitin.auxify.shapeless

import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec
import shapeless.{::, HNil, Witness, poly}
import shapeless.test.sameTyped
import shapeless.syntax.singleton._
import shapeless.tag.{@@, Tagged}
import shapeless.{the => s_the}

class PolyTest extends AnyFlatSpec with Matchers {
  implicitly[stringToSymbolPoly.Case.Aux[Witness.`"a"`.T, Witness.`'a`.T]]
  implicitly[symbolToStringPoly.Case.Aux[Witness.`'a`.T, Witness.`"a"`.T]]

  implicitly[stringToSymbolPoly.ProductCase.Aux[Witness.`"a"`.T :: HNil, Witness.`'a`.T]]
  implicitly[symbolToStringPoly.ProductCase.Aux[Witness.`'a`.T :: HNil, Witness.`"a"`.T]]

  implicitly[poly.Case.Aux[stringToSymbolPoly.type, Witness.`"a"`.T :: HNil, Witness.`'a`.T]]
  implicitly[poly.Case.Aux[symbolToStringPoly.type, Witness.`'a`.T :: HNil, Witness.`"a"`.T]]

  implicitly[poly.Case1.Aux[stringToSymbolPoly.type, Witness.`"a"`.T, Witness.`'a`.T]]
  implicitly[poly.Case1.Aux[symbolToStringPoly.type, Witness.`'a`.T, Witness.`"a"`.T]]

  implicitly[poly.Case[symbolToStringPoly.type, (Symbol @@ Witness.`"a"`.T) :: HNil]]

  // without apply[Witness.`...`.T] doesn't compile in 2.11+
  sameTyped[Witness.`'a`.T](stringToSymbolPoly.apply[Witness.`"a"`.T]("a".narrow))('a.narrow)
  sameTyped[Witness.`"a"`.T](symbolToStringPoly.apply[Witness.`'a`.T]('a.narrow))("a".narrow)

  "Poly" should "work" in {
    stringToSymbolPoly.apply/*[Witness.`"a"`.T]*/("a".narrow) === 'a should be(true)
    symbolToStringPoly.apply/*[Witness.`'a`.T]*/('a.narrow) should be("a")
  }
}
