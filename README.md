# AUXify

[![Build Status](https://travis-ci.org/DmytroMitin/AUXify.svg?branch=master)](https://travis-ci.org/DmytroMitin/AUXify)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.dmytromitin/auxify-macros_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.dmytromitin/auxify-macros_2.13)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.dmytromitin/auxify-macros_2.13.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.dmytromitin%22%20AND%20a:%22auxify-macros_2.13%22)

[sonatype](https://oss.sonatype.org/content/groups/public/com/github/dmytromitin/)
[mvnrepository](https://mvnrepository.com/artifact/com.github.dmytromitin)

## Using
Write in `build.sbt`
```scala
scalaVersion := "2.13.0"
//scalaVersion := "2.12.8"
//scalaVersion := "2.11.12"
//scalaVersion := "2.10.7"

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies += "com.github.dmytromitin" %% "auxify-macros" % "0.3"

scalacOptions += "-Ymacro-annotations" // in Scala >= 2.13
//addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full) // in Scala <= 2.12
```

## @aux
Transforms
```scala
@aux
trait Add[N <: Nat, M <: Nat] {
  type Out <: Nat
  def apply(n: N, m: M): Out
}
```
into
```scala
trait Add[N <: Nat, M <: Nat] {
  type Out <: Nat
  def apply(n: N, m: M): Out
}

object Add {
  type Aux[N <: Nat, M <: Nat, Out0 <: Nat] = Add[N, M] { type Out = Out0 }
}
```
So it can be used:
```scala
implicitly[Add.Aux[_2, _3, _5]]
```
Convenient for type-level programming.

## @self
Transforms
```scala
@self
sealed trait Nat {
  type ++ = Succ[This]
}

@self
case object _0 extends Nat 

type _0 = _0.type

@self
case class Succ[N <: Nat](n: N) extends Nat
```
into
```scala
sealed trait Nat { self =>
  type Self >: self.type <: Nat { type Self = self.Self }
  type ++ = Succ[Self]
}

case object _0 extends Nat {
  override type Self = _0
}

type _0 = _0.type

case class Succ[N <: Nat](n: N) extends Nat {
  override type Self = Succ[N]
}
```
Convenient for type-level programming.

Generating lower bound `>: self.type` and/or F-bound `type Self = self.Self` for trait can be switched off
```scala
@self(lowerBound = false, fBound = false)
```

## @instance (constructor)
Transforms
```scala
@instance
trait Monoid[A] {
  def empty: A
  def combine(a: A, a1: A): A
}
```
into
```scala
@instance
trait Monoid[A] {
  def empty: A
  def combine(a: A, a1: A): A
}

object Monoid {
  def instance[A](f: => A, f1: (A, A) => A): Monoid[A] = new Monoid[A] {
    override def empty: A = f
    override def combine(a: A, a1: A): A = f1(a, a1)
  }
}
```
So it can be used
```scala
implicit val intMonoid: Monoid[Int] = instance(0, _ + _)
```

Polymorphic methods are not supported (since Scala 2 lacks polymorphic functions).

## @apply (materializer)
Transforms
```scala
@apply
trait Show[A] {
  def show(a: A): String
}
```
into 
```scala
trait Show[A] {
  def show(a: A): String
}

object Show {
  def apply[A](implicit inst: Show[A]): Show[A] = inst
}
```
So it can be used
```scala
Show[Int].show(10)
```

Method materializing type class can return more precise type than the one of implicit to be found (like `the` in [Shapeless](https://github.com/milessabin/shapeless) or [Dotty](https://github.com/lampepfl/dotty)).
For example
```scala
@apply
trait Add[N <: Nat, M <: Nat] {
  type Out <: Nat
  def apply(n: N, m: M): Out
}
```
is transformed into
```scala
trait Add[N <: Nat, M <: Nat] {
  type Out <: Nat
  def apply(n: N, m: M): Out
}

object Add {
  def apply[N <: Nat, M <: Nat](implicit inst: Add[N, M]): Add[N, M] { type Out = inst.Out } = inst
}
```
[Simulacrum](https://github.com/typelevel/simulacrum) annotation `@typeclass` also generates, among other, materializer but doesn't support type classes with multiple type parameters.

## @delegated
Generates methods in companion object delegating to implicit instance of trait (type class).

Transforms
```scala
@delegated
trait Show[A] {
  def show(a: A): String
}
```
into
```scala
trait Show[A] {
  def show(a: A): String
}

object Add {
  def show[A](a: A)(implicit inst: Show[A]): String = inst.show(a)
}
```
So it can be used
`````scala
Show.show(10)
`````

## @syntax
Transforms
```scala
@syntax
trait Monoid[A] {
  def empty: A
  def combine(a: A, a1: A): A
}

```
into
```scala
trait Monoid[A] {
  def empty: A
  def combine(a: A, a1: A): A
}

object Monoid {
  object syntax {
    implicit class Ops[A](a: A) {
      def combine(a1: A)(implicit inst: Monoid[A]): A = inst.combine(a, a1)
    }
  }
}
```
So it can be used
```scala
import Monoid.syntax._
2 combine 3
```
[Simulacrum](https://github.com/typelevel/simulacrum) annotation `@typeclass` also generates syntax but doesn't support type classes with multiple type parameters.

Inheritance of type classes is not supported (anyway it's [broken](https://typelevel.org/blog/2016/09/30/subtype-typeclasses.html)).