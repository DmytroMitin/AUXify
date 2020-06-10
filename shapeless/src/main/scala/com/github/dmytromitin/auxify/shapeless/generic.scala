package com.github.dmytromitin.auxify.shapeless

import com.github.dmytromitin.auxify.macros.{apply, aux, instance}
import shapeless.{Coproduct, HList, LabelledGeneric => SLabelledGeneric}

@aux @apply @instance
trait LabelledGeneric[T] extends Serializable {
  type Repr
  def to(t : T) : Repr
  def from(r : Repr) : T
}
object LabelledGeneric {
  implicit def caseClass[T, L <: HList, L1 <: HList](implicit
    gen: SLabelledGeneric.Aux[T, L],
    sts: record.SymbolsToStrings.Aux[L, L1],
    sts1: record.StringsToSymbols.Aux[L1, L]
  ): Aux[T, L1] = instance(t => sts(gen.to(t)), l => gen.from(sts1(l)))

  implicit def sealedTrait[T, C <: Coproduct, C1 <: Coproduct](implicit
    gen: SLabelledGeneric.Aux[T, C],
    sts: union.SymbolsToStrings.Aux[C, C1],
    sts1: union.StringsToSymbols.Aux[C1, C]
  ): Aux[T, C1] = instance(t => sts(gen.to(t)), c => gen.from(sts1(c)))
}