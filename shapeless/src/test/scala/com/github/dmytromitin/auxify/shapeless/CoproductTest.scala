package com.github.dmytromitin.auxify.shapeless

import com.github.dmytromitin.auxify.shapeless.coproduct.{StringsToSymbols, SymbolsToStrings}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import shapeless.syntax.singleton._
import shapeless.{:+:, CNil, Inl, Inr, Witness}

class CoproductTest extends AnyFlatSpec with Matchers {
  implicitly[StringsToSymbols.Aux[Witness.`"a"`.T :+: Witness.`"b"`.T :+: Witness.`"c"`.T :+: CNil,
    Witness.`'a`.T :+: Witness.`'b`.T :+: Witness.`'c`.T :+: CNil]]
  implicitly[SymbolsToStrings.Aux[Witness.`'a`.T :+: Witness.`'b`.T :+: Witness.`'c`.T :+: CNil,
    Witness.`"a"`.T :+: Witness.`"b"`.T :+: Witness.`"c"`.T :+: CNil]]

  "StringsToSymbols" should "work" in {
    StringsToSymbols[Witness.`"a"`.T :+: Witness.`"b"`.T :+: Witness.`"c"`.T :+: CNil].stringsToSymbols(Inl("a".narrow)) should be(Inl('a.narrow))
    StringsToSymbols[Witness.`"a"`.T :+: Witness.`"b"`.T :+: Witness.`"c"`.T :+: CNil].stringsToSymbols(Inr(Inl("b".narrow))) should be(Inr(Inl('b.narrow)))
    StringsToSymbols[Witness.`"a"`.T :+: Witness.`"b"`.T :+: Witness.`"c"`.T :+: CNil].stringsToSymbols(Inr(Inr(Inl("c".narrow)))) should be(Inr(Inr(Inl('c.narrow))))
  }

  "SymbolsToStrings" should "work" in {
    SymbolsToStrings[Witness.`'a`.T :+: Witness.`'b`.T :+: Witness.`'c`.T :+: CNil].symbolsToStrings(Inl('a.narrow)) should be(Inl("a".narrow))
    SymbolsToStrings[Witness.`'a`.T :+: Witness.`'b`.T :+: Witness.`'c`.T :+: CNil].symbolsToStrings(Inr(Inl('b.narrow))) should be(Inr(Inl("b".narrow)))
    SymbolsToStrings[Witness.`'a`.T :+: Witness.`'b`.T :+: Witness.`'c`.T :+: CNil].symbolsToStrings(Inr(Inr(Inl('c.narrow)))) should be(Inr(Inr(Inl("c".narrow))))
  }

  "StringsToSymbols syntax" should "work" in {
    import StringsToSymbols.syntax._
    (Inl("a".narrow) : Witness.`"a"`.T :+: Witness.`"b"`.T :+: Witness.`"c"`.T :+: CNil).stringsToSymbols should be(Inl('a.narrow))
    (Inr(Inl("b".narrow)) : Witness.`"a"`.T :+: Witness.`"b"`.T :+: Witness.`"c"`.T :+: CNil).stringsToSymbols should be(Inr(Inl('b.narrow)))
    (Inr(Inr(Inl("c".narrow))) : Witness.`"a"`.T :+: Witness.`"b"`.T :+: Witness.`"c"`.T :+: CNil).stringsToSymbols should be(Inr(Inr(Inl('c.narrow))))
  }

  "SymbolsToStrings syntax" should "work" in {
    import SymbolsToStrings.syntax._
    (Inl('a.narrow): Witness.`'a`.T :+: Witness.`'b`.T :+: Witness.`'c`.T :+: CNil).symbolsToStrings should be(Inl("a".narrow))
    (Inr(Inl('b.narrow)): Witness.`'a`.T :+: Witness.`'b`.T :+: Witness.`'c`.T :+: CNil).symbolsToStrings should be(Inr(Inl("b".narrow)))
    (Inr(Inr(Inl('c.narrow))): Witness.`'a`.T :+: Witness.`'b`.T :+: Witness.`'c`.T :+: CNil).symbolsToStrings should be(Inr(Inr(Inl("c".narrow))))
  }
}
