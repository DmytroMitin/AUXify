package com.github.dmytromitin.auxify.shapeless

import com.github.dmytromitin.auxify.shapeless.hlist.{StringsToSymbols, SymbolsToStrings}
import shapeless.{::, HNil, Witness}
import shapeless.syntax.singleton._
import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec

class HListTest extends AnyFlatSpec with Matchers {
  implicitly[StringsToSymbols.Aux[Witness.`"a"`.T :: Witness.`"b"`.T :: Witness.`"c"`.T :: HNil,
    Witness.`'a`.T :: Witness.`'b`.T :: Witness.`'c`.T :: HNil]]
  implicitly[SymbolsToStrings.Aux[Witness.`'a`.T :: Witness.`'b`.T ::Witness.`'c`.T :: HNil,
    Witness.`"a"`.T :: Witness.`"b"`.T ::Witness.`"c"`.T :: HNil]]

  "StringsToSymbols" should "work" in {
    StringsToSymbols[Witness.`"a"`.T :: Witness.`"b"`.T :: Witness.`"c"`.T :: HNil].stringsToSymbols("a".narrow :: "b".narrow :: "c".narrow :: HNil) should be('a.narrow :: 'b.narrow :: 'c.narrow :: HNil)
  }

  "SymbolsToStrings" should "work" in {
    SymbolsToStrings[Witness.`'a`.T :: Witness.`'b`.T :: Witness.`'c`.T :: HNil].symbolsToStrings('a.narrow :: 'b.narrow :: 'c.narrow :: HNil) should be("a".narrow :: "b".narrow :: "c".narrow :: HNil)
  }

  "StringsToSymbols syntax" should "work" in {
    import StringsToSymbols.syntax._
    ("a".narrow :: "b".narrow :: "c".narrow :: HNil).stringsToSymbols should be('a.narrow :: 'b.narrow :: 'c.narrow :: HNil)
  }

  "SymbolsToStrings syntax" should "work" in {
    import SymbolsToStrings.syntax._
    ('a.narrow :: 'b.narrow :: 'c.narrow :: HNil).symbolsToStrings should be("a".narrow :: "b".narrow :: "c".narrow :: HNil)
  }
}
