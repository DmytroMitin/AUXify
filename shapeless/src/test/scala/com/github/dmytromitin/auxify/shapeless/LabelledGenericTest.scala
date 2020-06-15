package com.github.dmytromitin.auxify.shapeless

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import shapeless.{::, DefaultSymbolicLabelling, Generic, HNil, Inl, Inr, Lazy, LowPriority, Witness}
import shapeless.record.Record
import shapeless.union.Union
import shapeless.test.sameTyped
import shapeless.syntax.singleton._
import shapeless.labelled.field


sealed trait B
case class C(i: Int) extends B
case class D(s: String) extends B
case object E extends B


class LabelledGenericTest extends AnyFlatSpec with Matchers {
  import LabelledGenericTest._

  implicitly[LabelledGeneric.Aux[A, Record]]

  implicitly[gen.Repr =:= Record]

  sameTyped[Record](gen.to(A(1, "a", true)))(field[Si](1) :: field[Ss]("a") :: field[Sb](true) :: HNil)
  sameTyped[Record](gen.to(A(1, "a", true)))(("i" ->> 1) :: ("s" ->> "a") :: ("b" ->> true) :: HNil)
  sameTyped[A](gen.from(field[Si](1) :: field[Ss]("a") :: field[Sb](true) :: HNil))(A(1, "a", true))

  "LabelledGeneric" should "work for case classes" in {
    gen.to(A(1, "a", true)) should be (field[Si](1) :: field[Ss]("a") :: field[Sb](true) :: HNil)
    gen.from(field[Si](1) :: field[Ss]("a") :: field[Sb](true) :: HNil) should be (A(1, "a", true))
  }

  implicitly[LabelledGeneric.Aux[B, Union]]
  implicitly[gen1.Repr =:= Union]

  sameTyped[Union](gen1.to(C(1): B))(Inl(field[SC](C(1))))
  sameTyped[Union](gen1.to(D("a"): B))(Inr(Inl(field[SD](D("a")))))
  sameTyped[Union](gen1.to(E: B))(Inr(Inr(Inl(field[SE](E)))))
  sameTyped[B](gen1.from(Inl(field[SC](C(1)))))(C(1))
  sameTyped[B](gen1.from(Inr(Inl(field[SD](D("a"))))))(D("a"))
  sameTyped[B](gen1.from(Inr(Inr(Inl(field[SE](E))))))(E)

  "LabelledGeneric" should "work for sealed traits" in {
    gen1.to(C(1): B) should be (Inl(field[SC](C(1))))
    gen1.to(D("a"): B) should be (Inr(Inl(field[SD](D("a")))))
    gen1.to(E: B) should be (Inr(Inr(Inl(field[SE](E)))))
    gen1.from(Inl(field[SC](C(1)))) should be (C(1))
    gen1.from(Inr(Inl(field[SD](D("a"))))) should be (D("a"))
    gen1.from(Inr(Inr(Inl(field[SE](E))))) should be (E)
  }
}

object LabelledGenericTest {
  case class A(i: Int, s: String, b: Boolean)
  type Record = Record.`"i" -> Int, "s" -> String, "b" -> Boolean`.T
  implicitly[Generic.Aux[A, Int :: String :: Boolean :: HNil]]
  Generic[A]
  val gen = LabelledGeneric[A]/*()*/
  type Si = Witness.`"i"`.T
  type Ss = Witness.`"s"`.T
  type Sb = Witness.`"b"`.T

  type Union = Union.`"C" -> C, "D" -> D, "E" -> E.type`.T
  val gen1 = LabelledGeneric[B]
  type SC = Witness.`"C"`.T
  type SD = Witness.`"D"`.T
  type SE = Witness.`"E"`.T
}
