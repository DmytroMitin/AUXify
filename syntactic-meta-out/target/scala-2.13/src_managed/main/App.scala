import com.github.dmytromitin.auxify.meta.aux
object App {
  class A[T] { type U }
  class B
  object C
  object A { type Aux[T, U0$meta$1] = A[T] { type U = U0$meta$1 } }
}