package com.github.dmytromitin.auxify.shapeless

import shapeless.Witness
import shapeless.labelled.{FieldType, field}
import shapeless.syntax.singleton._

//FIXME #36
class FieldTypeTest {
//  def syFldToStrFld[St <: String, Sy <: Symbol, T](syFld : FieldType[Sy,T])(implicit syWit : Witness.Aux[Sy]) : FieldType[St,T] =
//    symbolToString(syWit.value) ->> (syFld : T)
//    (syWit.value.name : String) ->> (syFld : T)
//    field[/*syWit.value.name.type*/St][T](syFld : T)
}
