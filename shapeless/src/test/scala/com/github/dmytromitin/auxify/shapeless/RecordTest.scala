package com.github.dmytromitin.auxify.shapeless

import com.github.dmytromitin.auxify.shapeless.record.{StringsToSymbols, SymbolsToStrings}
import shapeless.record.Record
import shapeless.{::, HNil, Witness}
import shapeless.labelled.field
import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec

class RecordTest extends AnyFlatSpec with Matchers {
  implicitly[StringsToSymbols.Aux[Record.`"a" -> Int, "b" -> String, "c" -> Boolean`.T,
    Record.`'a -> Int, 'b -> String, 'c -> Boolean`.T]]
  implicitly[SymbolsToStrings.Aux[Record.`'a -> Int, 'b -> String, 'c -> Boolean`.T,
    Record.`"a" -> Int, "b" -> String, "c" -> Boolean`.T]]

  "StringsToSymbols" should "work" in {
    StringsToSymbols[Record.`"a" -> Int, "b" -> String, "c" -> Boolean`.T].stringsToSymbols(field[Witness.`"a"`.T](1) :: field[Witness.`"b"`.T]("s") :: field[Witness.`"c"`.T](true) :: HNil) should be(field[Witness.`'a`.T](1) :: field[Witness.`'b`.T]("s") :: field[Witness.`'c`.T](true) :: HNil)
  }

  "SymbolsToStrings" should "work" in {
    SymbolsToStrings[Record.`'a -> Int, 'b -> String, 'c -> Boolean`.T].symbolsToStrings(field[Witness.`'a`.T](1) :: field[Witness.`'b`.T]("s") :: field[Witness.`'c`.T](true) :: HNil) should be(field[Witness.`"a"`.T](1) :: field[Witness.`"b"`.T]("s") :: field[Witness.`"c"`.T](true) :: HNil)
  }

  "StringsToSymbols syntax" should "work" in {
    import StringsToSymbols.syntax._
    (field[Witness.`"a"`.T](1) :: field[Witness.`"b"`.T]("s") :: field[Witness.`"c"`.T](true) :: HNil).stringsToSymbols should be(field[Witness.`'a`.T](1) :: field[Witness.`'b`.T]("s") :: field[Witness.`'c`.T](true) :: HNil)
  }

  "SymbolsToStrings syntax" should "work" in {
    import SymbolsToStrings.syntax._
    (field[Witness.`'a`.T](1) :: field[Witness.`'b`.T]("s") :: field[Witness.`'c`.T](true) :: HNil).symbolsToStrings should be(field[Witness.`"a"`.T](1) :: field[Witness.`"b"`.T]("s") :: field[Witness.`"c"`.T](true) :: HNil)
  }
}
