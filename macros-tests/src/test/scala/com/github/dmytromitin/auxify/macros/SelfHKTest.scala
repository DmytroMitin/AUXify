package com.github.dmytromitin.auxify.macros

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SelfHKTest extends AnyFlatSpec with Matchers {
  @self
  sealed trait A[X >: Null <: AnyRef, Y]

  @self
  class B[X >: Null <: AnyRef, Y] extends A[X, Y]

  @self
  object C extends A[String, Nothing]

}