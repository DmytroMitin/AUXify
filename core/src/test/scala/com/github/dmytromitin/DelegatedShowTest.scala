package com.github.dmytromitin

import org.scalatest._

class DelegatedShowTest extends FlatSpec with Matchers {
  @delegated
  trait Show[A] {
    def show(a: A): String
  }

  object Show {
//    def show[A](a: A)(implicit inst: Show[A]): String = inst.show(a)

    implicit val str: Show[String] = identity[String]
    implicit val int: Show[Int] = _.toString
  }

  "strings" should "work" in {
    Show.show("abc") should be ("abc")
  }

  "ints" should "work" in {
    Show.show(10) should be ("10")
  }

}