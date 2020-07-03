package com.github.dmytromitin.auxify.macros

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class InstanceByNameTest extends AnyFlatSpec with Matchers {
  @instance
  trait IntHolder {
    def value: Int
  }

  object IntHolder {
//    def instance(n: => Int): IntHolder = new IntHolder {
//      override def value: Int = n
//    }

    implicit val mkIntHolder: IntHolder = instance(throw new Exception("AAA"))
  }

//  implicitly[IntHolder] // TODO #5

  an [Exception] should be thrownBy implicitly[IntHolder].value


}