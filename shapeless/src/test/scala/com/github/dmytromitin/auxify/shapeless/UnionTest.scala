package com.github.dmytromitin.auxify.shapeless

import com.github.dmytromitin.auxify.shapeless.union.{StringsToSymbols, SymbolsToStrings}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import shapeless.labelled.field
import shapeless.union.Union
import shapeless.{Inl, Inr, Witness}

class UnionTest extends AnyFlatSpec with Matchers {
  implicitly[StringsToSymbols.Aux[Union.`"a" -> Int, "b" -> String, "c" -> Boolean`.T,
    Union.`'a -> Int, 'b -> String, 'c -> Boolean`.T]]
  implicitly[SymbolsToStrings.Aux[Union.`'a -> Int, 'b -> String, 'c -> Boolean`.T,
    Union.`"a" -> Int, "b" -> String, "c" -> Boolean`.T]]

  "StringsToSymbols" should "work" in {
    StringsToSymbols[Union.`"a" -> Int, "b" -> String, "c" -> Boolean`.T].stringsToSymbols(Inl(field[Witness.`"a"`.T](1))) should be(Inl(field[Witness.`'a`.T](1)))
    StringsToSymbols[Union.`"a" -> Int, "b" -> String, "c" -> Boolean`.T].stringsToSymbols(Inr(Inl(field[Witness.`"b"`.T]("s")))) should be(Inr(Inl(field[Witness.`'b`.T]("s"))))
    StringsToSymbols[Union.`"a" -> Int, "b" -> String, "c" -> Boolean`.T].stringsToSymbols(Inr(Inr(Inl(field[Witness.`"c"`.T](true))))) should be(Inr(Inr(Inl(field[Witness.`'c`.T](true)))))
  }

  "SymbolsToStrings" should "work" in {
    SymbolsToStrings[Union.`'a -> Int, 'b -> String, 'c -> Boolean`.T].symbolsToStrings(Inl(field[Witness.`'a`.T](1))) should be(Inl(field[Witness.`"a"`.T](1)))
    SymbolsToStrings[Union.`'a -> Int, 'b -> String, 'c -> Boolean`.T].symbolsToStrings(Inr(Inl(field[Witness.`'b`.T]("s")))) should be(Inr(Inl(field[Witness.`"b"`.T]("s"))))
    SymbolsToStrings[Union.`'a -> Int, 'b -> String, 'c -> Boolean`.T].symbolsToStrings(Inr(Inr(Inl(field[Witness.`'c`.T](true))))) should be(Inr(Inr(Inl(field[Witness.`"c"`.T](true)))))
  }

  "StringsToSymbols syntax" should "work" in {
    import StringsToSymbols.syntax._
    (Inl(field[Witness.`"a"`.T](1)): Union.`"a" -> Int, "b" -> String, "c" -> Boolean`.T).stringsToSymbols should be(Inl(field[Witness.`'a`.T](1)))
    (Inr(Inl(field[Witness.`"b"`.T]("s"))): Union.`"a" -> Int, "b" -> String, "c" -> Boolean`.T).stringsToSymbols should be(Inr(Inl(field[Witness.`'b`.T]("s"))))
    (Inr(Inr(Inl(field[Witness.`"c"`.T](true)))): Union.`"a" -> Int, "b" -> String, "c" -> Boolean`.T).stringsToSymbols should be(Inr(Inr(Inl(field[Witness.`'c`.T](true)))))
  }

  "SymbolsToStrings syntax" should "work" in {
    import SymbolsToStrings.syntax._
    (Inl(field[Witness.`'a`.T](1)): Union.`'a -> Int, 'b -> String, 'c -> Boolean`.T).symbolsToStrings should be(Inl(field[Witness.`"a"`.T](1)))
    (Inr(Inl(field[Witness.`'b`.T]("s"))): Union.`'a -> Int, 'b -> String, 'c -> Boolean`.T).symbolsToStrings should be(Inr(Inl(field[Witness.`"b"`.T]("s"))))
    (Inr(Inr(Inl(field[Witness.`'c`.T](true)))): Union.`'a -> Int, 'b -> String, 'c -> Boolean`.T).symbolsToStrings should be(Inr(Inr(Inl(field[Witness.`"c"`.T](true)))))
  }
}
