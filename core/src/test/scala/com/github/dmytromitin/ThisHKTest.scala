package com.github.dmytromitin

import org.scalatest._

class ThisHKTest extends FlatSpec with Matchers {
  @self
  sealed trait A[X >: Null <: AnyRef, Y]

  @self
  class B[X >: Null <: AnyRef, Y] extends A[X, Y]

  @self
  object C extends A[String, Nothing]

}