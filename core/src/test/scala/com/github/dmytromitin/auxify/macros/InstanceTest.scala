package com.github.dmytromitin.auxify.macros

import org.scalatest._

class InstanceTest extends FlatSpec with Matchers {
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

  @instance
  trait Add[N <: Nat, M <: Nat] {
    type Out <: Nat
    def apply(n: N, m: M): Out
  }

  object Add {
    type Aux[N <: Nat, M <: Nat, Out0 <: Nat] = Add[N, M] { type Out = Out0 }
//    def instance[N <: Nat, M <: Nat, Out0 <: Nat](f: (N, M) => Out0): Add[N, M] { type Out = Out0 } = new Add[N, M] {
//      override type Out = Out0
//      override def apply(n: N, m: M): Out = f(n, m)
//    }

    implicit def zeroAdd[M <: Nat]: Aux[_0, M, M] = instance((_, m) => m)
    implicit def succAdd[N <: Nat, M <: Nat, N_addM <: Nat](implicit add: Aux[N, M, N_addM]): Aux[Succ[N], M, Succ[N_addM]] =
      instance((succN, m) => Succ(add(succN.n, m)))
  }

  implicitly[Add.Aux[_2, _3, _5]]

  "2 + 3" should "be 5" in {
    implicitly[Add[_2, _3]].apply(_2, _3) should be (_5)
  }

}