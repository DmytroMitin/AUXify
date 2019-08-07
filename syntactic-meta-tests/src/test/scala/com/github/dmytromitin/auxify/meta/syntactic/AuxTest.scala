package com.github.dmytromitin.auxify.meta.syntactic

import org.scalatest._

class AuxTest extends FlatSpec with Matchers {
  type T = AuxIn.A.Aux[Int, String]
}
