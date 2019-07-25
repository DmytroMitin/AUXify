package com.github.dmytromitin.auxify.macros

import org.scalatest._

import scala.language.higherKinds

@Ignore
class AuxTest1 extends FlatSpec with Matchers {

  // TODO #9
//  @aux
//  trait A[+T >: Null <: AnyRef /*: Ordering*/, T1[+_ <: AnyRef], T2[X <: AnyRef] >: T1[X], -_[+_ <: AnyRef] <: AnyRef] {
//    type U >: Null <: AnyRef
//    type U1[-_]
//    type U2[X >: Null] <: U1[X]
//    def foo[U]: T = null
//    type U3 = String
//    type U4[+_ <: AnyRef] <: AnyRef
//  }

}