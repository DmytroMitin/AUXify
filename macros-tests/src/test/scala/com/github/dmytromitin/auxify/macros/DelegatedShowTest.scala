package com.github.dmytromitin.auxify.macros

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DelegatedShowTest extends AnyFlatSpec with Matchers {
  @delegated
  trait Show[A] {
    def show(a: A): String
  }

  object Show {
//    def show[A](a: A)(implicit inst: Show[A]): String = inst.show(a)

    implicit val str: Show[String] = new Show[String] {
      override def show(a: String): String = a
    }
    implicit val int: Show[Int] = new Show[Int] {
      override def show(a: Int): String = a.toString
    }
  }

  "strings" should "work" in {
    Show.show("abc") should be ("abc")
  }

  "ints" should "work" in {
    Show.show(10) should be ("10")
  }

}