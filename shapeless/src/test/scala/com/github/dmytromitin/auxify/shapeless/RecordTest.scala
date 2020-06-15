package com.github.dmytromitin.auxify.shapeless

import com.github.dmytromitin.auxify.shapeless.record.{StringsToSymbols, SymbolsToStrings}
import shapeless.record.Record
import shapeless.{::, HNil, Witness}
import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec

class RecordTest extends AnyFlatSpec with Matchers {
  implicitly[StringsToSymbols.Aux[Record.`"a" -> Int, "b" -> String, "c" -> Boolean`.T,
    Record.`'a -> Int, 'b -> String, 'c -> Boolean`.T]]
  implicitly[SymbolsToStrings.Aux[Record.`'a -> Int, 'b -> String, 'c -> Boolean`.T,
    Record.`"a" -> Int, "b" -> String, "c" -> Boolean`.T]]
}
