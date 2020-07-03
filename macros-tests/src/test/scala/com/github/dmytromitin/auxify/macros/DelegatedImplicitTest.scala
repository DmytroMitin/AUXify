package com.github.dmytromitin.auxify.macros

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DelegatedImplicitTest extends AnyFlatSpec with Matchers {
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

  @delegated
  trait Add[N <: Nat, M <: Nat] {
    type Out <: Nat
    def apply(n: N, m: M): Out
  }

  def test(implicit add: Add[_2, _3]) = Add.apply[_2, _3](_2, _3)

  @delegated
  trait Add1[N <: Nat] {
    type Out <: Nat
    def apply[M <: Nat](n: N, m: M): Out
  }

  def test1(implicit add: Add1[_2]) = Add1.apply[_2, _3](_2, _3)

  @delegated
  trait Add2[N <: Nat] {
    type Out <: Nat
    def apply[M <: Nat]: Out
  }

  def test2(implicit add: Add2[_2]) = Add2.apply[_2, _3]

  @delegated
  trait Add3[N <: Nat] {
    type Out <: Nat
    def apply[M <: Nat](): Out
  }

  def test3(implicit add: Add3[_2]) = Add3.apply[_2, _3]()

  @delegated
  trait Add4[N <: Nat] {
    type Out <: Nat
    def apply[M <: Nat](i: Int, s: String)(implicit n: N): Out
  }

  def test4(implicit add: Add4[_2], two: _2) = Add4.apply[_2, _3](10, "abc")


}