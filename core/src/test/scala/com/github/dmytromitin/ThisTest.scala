package com.github.dmytromitin

import scala.language.higherKinds
import org.scalatest._

class ThisTest extends FlatSpec with Matchers {
  sealed trait Nat { self =>
    type This >: this.type <: Nat { type This = self.This }
    type ++ = Succ[This]
    type +[M <: Nat] <: Nat

    def ++ : ++ = Succ(this)
    def +[M <: Nat](m: M): +[M]
  }

  @This
  case object _0 extends Nat {
//    override type This = _0
    override type +[M <: Nat] = M

    override def +[M <: Nat](m: M): +[M] = m
  }

  type _0 = _0.type

  @This
  case class Succ[N <: Nat](n: N) extends Nat {
//    override type This = Succ[N]
    override type +[M <: Nat] = Succ[N# + [M]]

    override def +[M <: Nat](m: M): +[M] = Succ(n + m)
  }

  type _1 = _0# ++
  type _2 = _1# ++
  type _3 = _2# ++
  type _4 = _3# ++
  type _5 = _4# ++

  val _1: _1 = _0.++
  val _2: _2 = _1.++
  val _3: _3 = _2.++
  val _4: _4 = _3.++
  val _5: _5 = _4.++

  type + [N <: Nat, M <: Nat] = N# + [M]

  implicitly[_2 + _3 =:= _5]

  "2 + 3" should "be 5" in {
    _2 + _3 should be (_5)
  }

}