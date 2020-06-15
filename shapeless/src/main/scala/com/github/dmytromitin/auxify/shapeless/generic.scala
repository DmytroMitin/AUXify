package com.github.dmytromitin.auxify.shapeless

import com.github.dmytromitin.auxify.macros.{apply, aux, instance}
import shapeless.LabelledGeneric.Aux
import shapeless.ops.{hlist => shlist}
import shapeless.{Coproduct, DefaultSymbolicLabelling, Generic, HList, Lazy, LabelledGeneric => SLabelledGeneric}

@aux @apply @instance
trait LabelledGeneric[T] extends Serializable {
  type Repr
  def to(t : T) : Repr
  def from(r : Repr) : T
}
trait LowPriorityLabelledGeneric {
//  implicit def lpLabelledGeneric[T](implicit gen: Lazy[LabelledGeneric[T]]): LabelledGeneric.Aux[T, gen.value.Repr] =
//    gen.value
}
object LabelledGeneric extends LowPriorityLabelledGeneric {
//  def apply[T] = new PartiallyApplied[T]
//  class PartiallyApplied[T] {
//    def apply[Repr]()(implicit gen: Aux[T, Repr]): Aux[T, Repr] = gen
//  }

  implicit def caseClass[T, L <: HList, L1 <: HList/*, L2 <: HList*/](implicit
    gen: SLabelledGeneric.Aux[T, L],
    sts: record.SymbolsToStrings.Aux[L, L1],
    sts1: record.StringsToSymbols.Aux[L1, L/*L2*/]
// ,ev: L2 <:< L
  ): Aux[T, L1] = instance(t => sts(gen.to(t)), l => gen.from(sts1(l)))

//  implicit def caseClass[T, K <: HList, K1 <: HList, V <: HList, R <: HList]
//  (implicit
//   lab: DefaultSymbolicLabelling.Aux[T, K],
//   sts: hlist.SymbolsToStrings.Aux[K, K1],
//   gen: Generic.Aux[T, V],
//   zip: shlist.ZipWithKeys.Aux[K1, V, R],
//   ev: R <:< V
//  ): Aux[T, R] = instance(t => zip(gen.to(t)), r => gen.from(r))

//  implicit def sealedTrait[T, C <: Coproduct, C1 <: Coproduct](implicit
//    gen: SLabelledGeneric.Aux[T, C],
//    sts: union.SymbolsToStrings.Aux[C, C1],
//    sts1: union.StringsToSymbols.Aux[C1, C]
//  ): Aux[T, C1] = instance(t => sts(gen.to(t)), c => gen.from(sts1(c)))
}