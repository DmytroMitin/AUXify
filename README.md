# AUXify

Transforms

```
@Aux
trait Add[N <: Nat, M <: Nat] {
  type Out <: Nat
  def apply(n: N, m: M): Out
}

object Add {
  //...
}
```

into

```
trait Add[N <: Nat, M <: Nat] {
  type Out <: Nat
  def apply(n: N, m: M): Out
}

object Add {
  type Aux[N <: Nat, M <: Nat, Out0 <: Nat] = Add[N, M] { type Out = Out0 }
  
  //...
}
```

So it can be used:

```
implicitly[Add.Aux[_2, _3, _5]]
```
