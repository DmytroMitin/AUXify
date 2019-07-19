# AUXify

[![Build Status](https://travis-ci.org/DmytroMitin/AUXify.svg?branch=master)](https://travis-ci.org/DmytroMitin/AUXify)

## @Aux

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

## @This

Transforms

```
@This
sealed trait Nat {
  type ++ = Succ[This]
}

@This
case object _0 extends Nat 

type _0 = _0.type

@This
case class Succ[N <: Nat](n: N) extends Nat
```

into

```
sealed trait Nat { self =>
  type This >: this.type <: Nat { type This = self.This }
  type ++ = Succ[This]
}

case object _0 extends Nat {
  override type This = _0
}

type _0 = _0.type

case class Succ[N <: Nat](n: N) extends Nat {
  override type This = Succ[N]
}
```