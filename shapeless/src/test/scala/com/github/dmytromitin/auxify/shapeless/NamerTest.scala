package com.github.dmytromitin.auxify.shapeless

import com.github.dmytromitin.auxify.macros.{apply, aux, instance}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
//import shapeless.ops.hlist.LeftFolder
import shapeless.ops.record.Keys
import shapeless.{::, DepFn2, HList, HNil, Lazy, Poly2, Strict, Witness, poly}
import shapeless.test.typed
import shapeless.syntax.singleton._

import scala.annotation.implicitNotFound

//https://gitter.im/milessabin/shapeless?at=5ed9e05922dd444224fd20cd
//https://gitter.im/milessabin/shapeless?at=5ee0f11aee693d6eb3b770f7
class NamerTest extends AnyFlatSpec with Matchers {
  import NamerTest._

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
    def apply[S <: String /*with Singleton*/](name0: S)(implicit witness: Witness.Aux[S]): NameR[S] = new NameR[S] {
      override type R = S
      override val name: S = name0
    }
  }

  class Holder[H <: HList](hlist: H) {
    def add[S <: String /*with Singleton*/](name: S)(implicit witness: Witness.Aux[S]): Holder[NameR[S] :: H] = new Holder[NameR[S] :: H](Name[S](name) :: hlist)
    def select[S <: String /*with Singleton*/](name: S)(implicit witness: Witness.Aux[S], selector: NameSelector[H, S]): S = name
  }
  object Holder extends Holder(HNil: HNil)

  @implicitNotFound("Could not find field ${S} in ${L}")
  trait NameSelector[L <: HList, S <: String] {
    def apply(l: L): NameR[S]
  }
  object NameSelector {
    def apply[L <: HList, S <: String /*with Singleton*/](name: S, l: L)(implicit witness: Witness.Aux[S], selector: NameSelector[L, S]): NameR[S] =
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

  typed[Witness.`"int"`.T](fields.select("int".narrow))

  val gen = LabelledGeneric[Foo]
  val keysInst = Keys[gen.Repr]
  val keys = keysInst()

  object addPoly extends Poly2 {
    implicit def cse[H <: HList, S <: String /*with Singleton*/](implicit witness: Witness.Aux[S]): Case.Aux[Holder[H], S, Holder[NameR[S] :: H]] =
      at((holder, s) => holder.add(s))
  }

  val fields1 = LeftFolder[keysInst.Out, Holder[HNil], addPoly.type].apply(keys, Holder)
//  val fields1 = keys.foldLeft(Holder: Holder[HNil])(addPoly)

  typed[Witness.`"int"`.T](fields1.select("int".narrow))
}

object NamerTest {
  final case class Foo(int: Int, long: Long)

  // use LeftFolder with Strict instead of standard LeftFolder   https://github.com/milessabin/shapeless/pull/859
  @aux @instance @apply
  trait LeftFolder[L <: HList, In, HF] extends DepFn2[L, In] with Serializable {
    type Out
    def apply(l: L, in: In): Out
  }
  object LeftFolder {
    implicit def hnilLeftFolder[In, HF]: Aux[HNil, In , HF, In] =
      instance((l, in) => in)

    implicit def hlistLeftFolder[H, T <: HList, In, HF, OutH, FtOut]
    (implicit cse: poly.Case2.Aux[HF, In, H, OutH], folder : Strict[Aux[T, OutH, HF, FtOut]]): Aux[H :: T, In, HF, FtOut] =
      instance((l, in) => folder.value(l.tail, cse(in, l.head)))
  }
}
