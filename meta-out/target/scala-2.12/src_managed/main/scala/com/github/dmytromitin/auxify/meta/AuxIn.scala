/*
rule = AuxRule
 */
package com.github.dmytromitin.auxify.meta

import scala.language.higherKinds

object AuxIn {
  
  class A[T] {
    type U
  }
object A { type Aux[T, U0$meta$2] = A[T] { type U = U0$meta$2 } }


  
  abstract class B[+T >: Null <: AnyRef : Ordering, T1[+_ <: AnyRef], T2[X <: AnyRef] >: T1[X], -_[+_ <: AnyRef] <: AnyRef] {
    type U >: Null <: AnyRef
    type U1[-_]
    type U2[X >: Null] <: U1[X]
    def foo[U]: T = null
    type U3 = String
    type U4[+_ <: AnyRef] <: AnyRef
  }
object B { type Aux[T >: Null <: AnyRef, T1[+_ <: AnyRef], T2[X <: AnyRef] >: T1[X], tparam$meta$9[+_ <: AnyRef] <: AnyRef, U0$meta$3 >: Null <: AnyRef, U10$meta$4[-_], U20$meta$5[X >: Null] <: U10$meta$4[X], U40$meta$6[+_ <: AnyRef] <: AnyRef] = B[T, T1, T2, tparam$meta$9] { type U = U0$meta$3; type U1[tparam$meta$7] = U10$meta$4[tparam$meta$7]; type U2[X >: Null] = U20$meta$5[X]; type U4[tparam$meta$8 <: AnyRef] = U40$meta$6[tparam$meta$8] } }
}
