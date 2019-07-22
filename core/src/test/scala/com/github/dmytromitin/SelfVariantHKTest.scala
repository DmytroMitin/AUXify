package com.github.dmytromitin

import org.scalatest._

class SelfVariantHKTest extends FlatSpec with Matchers {
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