# AUXify

[![Build Status](https://travis-ci.org/DmytroMitin/AUXify.svg?branch=master)](https://travis-ci.org/DmytroMitin/AUXify)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.dmytromitin/auxify-macros/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.dmytromitin/auxify-macros)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.dmytromitin/auxify_2.13.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.dmytromitin%22%20AND%20a:%22auxify_2.13%22)

[sonatype](https://oss.sonatype.org/content/groups/public/com/github/dmytromitin/)
[maven](https://search.maven.org/artifact/com.github.dmytromitin/auxify_2.13/0.1/jar)

## Using
Write in `build.sbt`
```scala
scalaVersion := "2.13.0"
//scalaVersion := "2.12.8"
//scalaVersion := "2.11.12"
//scalaVersion := "2.10.7"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("staging")
)

libraryDependencies += "com.github.dmytromitin" %% "auxify-macros" % "0.1"

scalacOptions += "-Ymacro-annotations" // in Scala >= 2.13
//addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full) // in Scala <= 2.12
```

## @Aux
Transforms
```scala
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
```scala
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
```scala
implicitly[Add.Aux[_2, _3, _5]]
```

## @This
Transforms
```scala
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
```scala
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

Generating lower bound `>: this.type` and/or F-bound `type This = self.This` for trait can be switched off
```scala
@This(lowerBound = false, fBound = false)
```