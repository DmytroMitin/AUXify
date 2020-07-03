package com.github.dmytromitin.auxify.meta.syntactic

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AuxTest extends AnyFlatSpec with Matchers {
  type T = AuxIn.A.Aux[Int, String]
}
