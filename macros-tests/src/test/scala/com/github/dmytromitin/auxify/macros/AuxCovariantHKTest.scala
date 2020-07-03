package com.github.dmytromitin.auxify.macros

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.{higherKinds, reflectiveCalls}

class AuxCovariantHKTest extends AnyFlatSpec with Matchers {
  sealed trait Nat
  object _0 extends Nat
  type _0 = _0.type
  class Succ[+N <: Nat] extends Nat

  type _1 = Succ[_0]
  type _2 = Succ[_1]
  type _3 = Succ[_2]
  type _4 = Succ[_3]
  type _5 = Succ[_4]

  @aux
  trait Add[+N <: Nat] {
    protected type Out[+M <: Nat] <: Nat
    type X = String
  }

  object Add {
//    type Aux[+N <: Nat, Out0[+_ <: Nat] <: Nat] = Add[N] { type Out[+M <: Nat] = Out0[M] }
    def instance[N <: Nat, Out0[+_ <: Nat] <: Nat]: Aux[N, Out0] = new Add[N] { type Out[+M <: Nat] = Out0[M] }

    implicit def zeroAdd: Aux[_0, ({ type λ[+M <: Nat] = M })#λ] = instance[_0, ({ type λ[+M <: Nat] = M })#λ]
    implicit def succAdd[N <: Nat](implicit
      add: Add[N]): Aux[Succ[N], ({ type λ[+M <: Nat] = Succ[add.Out[M]] })#λ] = instance[Succ[N], ({ type λ[+M <: Nat] = Succ[add.Out[M]] })#λ]
  }

  "implicitly[Add.Aux[_3, ({ type λ[M <: Nat] = Succ[Succ[Succ[M]]] })#λ]]" should compile

}