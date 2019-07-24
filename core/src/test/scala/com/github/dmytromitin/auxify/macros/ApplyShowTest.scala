package com.github.dmytromitin.auxify.macros

import org.scalatest._

class ApplyShowTest extends FlatSpec with Matchers {
  @apply
  trait Show[A] {
    def show(a: A): String
  }

  object Show {
//    def apply[A](implicit inst: Show[A]): Show[A] = inst

    implicit val str: Show[String] = new Show[String] {
      override def show(a: String): String = a
    }
    implicit val int: Show[Int] = new Show[Int] {
      override def show(a: Int): String = a.toString
    }
  }

  "strings" should "work" in {
    Show[String].show("abc") should be ("abc")
  }

  "ints" should "work" in {
    Show[Int].show(10) should be ("10")
  }
}