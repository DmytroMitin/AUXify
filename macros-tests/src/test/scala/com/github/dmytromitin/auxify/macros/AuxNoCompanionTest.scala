package com.github.dmytromitin.auxify.macros

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AuxNoCompanionTest extends AnyFlatSpec with Matchers {
  sealed trait Nat

  @aux
  trait Add[N <: Nat, M <: Nat] {
    type Out <: Nat
    def apply(n: N, m: M): Out
  }

}