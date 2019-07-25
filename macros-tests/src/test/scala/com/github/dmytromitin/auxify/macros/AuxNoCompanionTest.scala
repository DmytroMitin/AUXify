package com.github.dmytromitin.auxify.macros

import org.scalatest._

class AuxNoCompanionTest extends FlatSpec with Matchers {
  sealed trait Nat

  @aux
  trait Add[N <: Nat, M <: Nat] {
    type Out <: Nat
    def apply(n: N, m: M): Out
  }

}