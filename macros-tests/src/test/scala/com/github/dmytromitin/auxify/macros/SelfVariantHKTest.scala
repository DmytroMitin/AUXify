package com.github.dmytromitin.auxify.macros

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SelfVariantHKTest extends AnyFlatSpec with Matchers {
  @self
  sealed trait A[+X >: Null <: AnyRef, -Y] /*{ self =>
    type Self >: this.type <: A[X, Y] { type Self = self.Self }
  }*/

  @self
  class B[X >: Null <: AnyRef, Y] extends A[X, Y] {
//    type Self = B[X, Y]
  }

  @self
  object C extends A[String, Nothing] {
//    type Self = C.type
  }

}