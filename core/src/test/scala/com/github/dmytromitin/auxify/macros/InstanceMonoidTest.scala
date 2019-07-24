package com.github.dmytromitin.auxify.macros

import org.scalatest._

class InstanceMonoidTest extends FlatSpec with Matchers {
  @instance
  trait Monoid[A] {
    def empty: A
    def combine(a: A, a1: A): A
  }

  object Monoid {
//    def instance[A](f: => A, f1: (A, A) => A): Monoid[A] = new Monoid[A] {
//      override def empty: A = f
//      override def combine(a: A, a1: A): A = f1(a, a1)
//    }

    implicit val int: Monoid[Int] = instance(0, _ + _)
    implicit val str: Monoid[String] = instance("", _ + _)
  }

  "2 + 3" should "be 5" in {
    implicitly[Monoid[Int]].combine(2, 3) should be (5)
  }

}