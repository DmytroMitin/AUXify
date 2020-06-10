package com.github.dmytromitin.auxify.shapeless

import shapeless.{HNil, Inl, Inr, Witness}
import shapeless.record.Record
import shapeless.union.Union
import shapeless.test.sameTyped
import shapeless.syntax.singleton._
import shapeless.labelled.field

class LabelledGenericTest {
  case class A(i: Int, s: String, b: Boolean)
  type Record = Record.`"i" -> Int, "s" -> String, "b" -> Boolean`.T
  implicitly[LabelledGeneric.Aux[A, Record]]
  val gen = LabelledGeneric[A]
  implicitly[gen.Repr =:= Record]
  sameTyped[Record](gen.to(A(1, "a", true)))(field["i"](1) :: field["s"]("a") :: field["b"](true) :: HNil)
  sameTyped[Record](gen.to(A(1, "a", true)))(("i" ->> 1) :: ("s" ->> "a") :: ("b" ->> true) :: HNil)
  type Si = Witness.`"i"`.T
  type Ss = Witness.`"s"`.T
  type Sb = Witness.`"b"`.T
  sameTyped[A](gen.from(field[Si](1) :: field[Ss]("a") :: field[Sb](true) :: HNil))(A(1, "a", true))

  sealed trait B
  case class C(i: Int) extends B
  case class D(s: String) extends B
  case object E extends B
  type Union = Union.`"C" -> C, "D" -> D, "E" -> E.type`.T
  implicitly[LabelledGeneric.Aux[B, Union]]
  val gen1 = LabelledGeneric[B]
  implicitly[gen1.Repr =:= Union]
  type SC = Witness.`"C"`.T
  type SD = Witness.`"D"`.T
  type SE = Witness.`"E"`.T
  sameTyped[Union](gen1.to(C(1)))(Inl(field[SC](C(1))))
  sameTyped[Union](gen1.to(D("a")))(Inr(Inl(field[SD](D("a")))))
  sameTyped[Union](gen1.to(E))(Inr(Inr(Inl(field[SE](E)))))
  sameTyped[B](gen1.from(Inl(field[SC](C(1)))))(C(1))
  sameTyped[B](gen1.from(Inr(Inl(field[SD](D("a"))))))(D("a"))
  sameTyped[B](gen1.from(Inr(Inr(Inl(field[SE](E))))))(E)

}
