package com.github.dmytromitin.auxify.shapeless

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import shapeless.ops.record.Keys
import shapeless.{::, HList, HNil, Poly2, Witness}
import shapeless.test.typed
import shapeless.syntax.singleton._

import scala.annotation.implicitNotFound

//https://gitter.im/milessabin/shapeless?at=5ed9e05922dd444224fd20cd
//https://gitter.im/milessabin/shapeless?at=5ee0f11aee693d6eb3b770f7
class NamerTest extends AnyFlatSpec with Matchers {
  trait Name {
    type R <: String
    val name: R
    override def toString: String = name
  }
  trait NameR[S <: String] extends Name {
    type R = S
    val name: S
  }
  object Name {
    def apply[S <: String with Singleton](name0: S): NameR[S] = new NameR[S] {
      override type R = S
      override val name: S = name0
    }
  }

  class Holder[H <: HList](hlist: H) {
    def add[S <: String with Singleton](name: S): Holder[NameR[S] :: H] = new Holder[NameR[S] :: H](Name[S](name) :: hlist)
    def select[S <: String with Singleton](name: S)(implicit selector: NameSelector[H, S]): S = name
  }
  object Holder extends Holder(HNil: HNil)

  @implicitNotFound("Could not find field ${S} in ${L}")
  trait NameSelector[L <: HList, S <: String] {
    def apply(l: L): NameR[S]
  }
  object NameSelector {
    def apply[L <: HList, S <: String with Singleton](name: S, l: L)(implicit selector: NameSelector[L, S]): NameR[S] =
      selector.apply(l)
    def apply[L <: HList, S <: String](implicit selector: NameSelector[L, S]): NameSelector[L, S] = selector

    implicit def select[S <: String, T <: HList]: NameSelector[NameR[S] :: T, S] = new NameSelector[NameR[S] :: T, S] {
      override def apply(l: NameR[S] :: T): NameR[S] = l.head
    }

    implicit def recurse[S1 <: String, S <: String, T <: HList](implicit st: NameSelector[T, S]): NameSelector[NameR[S1] :: T, S] =
      new NameSelector[NameR[S1] :: T, S] {
        override def apply(l: NameR[S1] :: T): NameR[S] = st.apply(l.tail)
      }
  }

  val fields = Holder.add("int".narrow).add("long".narrow)

  "Namer" should "work with Strings" in {
    typed[Witness.`"int"`.T](fields.select("int".narrow)) should be (())
  }

  final case class Foo(int: Int, long: Long)

  val lbl = LabelledGeneric[Foo]
  val keys = Keys[lbl.Repr].apply

  object addPoly extends Poly2 {
    implicit def cse[H <: HList, S <: String with Singleton]: Case.Aux[Holder[H], S, Holder[NameR[S] :: H]] =
      at((holder, s) => holder.add(s))
  }

  val fields1 = keys.foldLeft(Holder: Holder[HNil])(addPoly)

  "Namer" should "work with Symbols" in {
    typed[Witness.`"int"`.T](fields1.select("int".narrow)) should be (())
  }
}
