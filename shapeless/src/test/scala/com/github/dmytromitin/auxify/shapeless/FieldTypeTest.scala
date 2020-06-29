package com.github.dmytromitin.auxify.shapeless

import shapeless.Witness
import shapeless.labelled.FieldType
import shapeless.syntax.singleton._

//https://stackoverflow.com/questions/50526509/using-string-instead-of-symbol-in-labelledgeneric-etc
class FieldTypeTest {
  def syFldToStrFld[St <: String, Sy <: Symbol, T](syFld : FieldType[Sy,T])(implicit syWit : Witness.Aux[Sy]) : FieldType[St,T] =
    (syWit.value.name : String) ->> (syFld : T)
}
