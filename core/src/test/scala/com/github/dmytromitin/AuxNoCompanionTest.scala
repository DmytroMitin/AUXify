package com.github.dmytromitin

import org.scalatest._

class AuxNoCompanionTest extends FlatSpec with Matchers {
  sealed trait Nat

  @Aux
  trait Add[N <: Nat, M <: Nat] {
    type Out <: Nat
    def apply(n: N, m: M): Out
  }

}