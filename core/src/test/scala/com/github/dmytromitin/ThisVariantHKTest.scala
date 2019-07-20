package com.github.dmytromitin

import org.scalatest._

class ThisVariantHKTest extends FlatSpec with Matchers {
  @This
  sealed trait A[+X >: Null <: AnyRef, -Y] /*{ self =>
    type This >: this.type <: A[X, Y] { type This = self.This }
  }*/

  @This
  class B[X >: Null <: AnyRef, Y] extends A[X, Y] {
//    type This = B[X, Y]
  }

  @This
  object C extends A[String, Nothing] {
//    type This = C.type
  }

}