/*
rule = AuxRule
 */
package com.github.dmytromitin.auxify.meta

object AuxIn {
  
  class A[T] {
    type U
  }
object A { type Aux[T, U0$meta$1] = A[T] { type U = U0$meta$1 } }
}
