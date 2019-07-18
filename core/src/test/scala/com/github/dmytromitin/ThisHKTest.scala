package com.github.dmytromitin

import org.scalatest._

class ThisHKTest extends FlatSpec with Matchers {
  @This
  sealed trait A[X >: Null <: AnyRef, Y]

  @This
  class B[X >: Null <: AnyRef, Y] extends A[X, Y]

  @This
  object C extends A[String, Nothing]

}