package com.github.dmytromitin

import org.scalatest._

class SyntaxMonoidTest extends FlatSpec with Matchers {
  @syntax
  trait Monoid[A] {
    def empty: A
    def combine(a: A, a1: A): A
  }

  object Monoid {
    def instance[A](f: => A, f1: (A, A) => A): Monoid[A] = new Monoid[A] {
      override def empty: A = f
      override def combine(a: A, a1: A): A = f1(a, a1)
    }

//    object syntax {
//      implicit class Ops[A](a: A) {
//        def combine(a1: A)(implicit inst: Monoid[A]): A = inst.combine(a, a1)
//      }
//    }

    implicit val int: Monoid[Int] = instance(0, _ + _)
    implicit val str: Monoid[String] = instance("", _ + _)
  }

  import Monoid.syntax._

  "2 + 3" should "be 5" in {
    2 combine 3 should be (5)
  }

}