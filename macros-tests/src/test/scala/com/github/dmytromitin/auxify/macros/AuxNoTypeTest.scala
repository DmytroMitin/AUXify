package com.github.dmytromitin.auxify.macros

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AuxNoTypeTest extends AnyFlatSpec with Matchers {
  sealed trait Nat
  case object _0 extends Nat
  type _0 = _0.type
  case class Succ[N <: Nat](n: N) extends Nat

  type _1 = Succ[_0]
  type _2 = Succ[_1]
  type _3 = Succ[_2]
  type _4 = Succ[_3]
  type _5 = Succ[_4]

  val _1: _1 = Succ(_0)
  val _2: _2 = Succ(_1)
  val _3: _3 = Succ(_2)
  val _4: _4 = Succ(_3)
  val _5: _5 = Succ(_4)

  @aux
  trait Add[N <: Nat, M <: Nat]

  object Add {
    def instance[N <: Nat, M <: Nat]: Add[N, M] = new Add[N, M] {}

    implicit def mkAdd[N <: Nat, M <: Nat]: Add[N, M] = instance
  }

  implicitly[Add.Aux[_2, _3]]

}