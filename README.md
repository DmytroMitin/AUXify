# AUXify

[![Build Status](https://travis-ci.org/DmytroMitin/AUXify.svg?branch=master)](https://travis-ci.org/DmytroMitin/AUXify)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.dmytromitin/auxify-macros_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.dmytromitin/auxify-macros_2.13)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.dmytromitin/auxify-macros_2.13.svg?label=maven%20central&color=success)](https://search.maven.org/search?q=g:%22com.github.dmytromitin%22%20AND%20a:%22auxify-macros_2.13%22)
[![Sonatype Snapshots](https://img.shields.io/nexus/r/https/oss.sonatype.org/com.github.dmytromitin/auxify-macros_2.13.svg?color=success)](https://oss.sonatype.org/content/groups/public/com/github/dmytromitin/auxify-macros_2.13/)
[![javadoc](https://javadoc.io/badge2/com.github.dmytromitin/auxify-macros_2.13/javadoc.svg)](https://javadoc.io/doc/com.github.dmytromitin/auxify-macros_2.13)
[![Scaladex](https://index.scala-lang.org/dmytromitin/auxify/auxify-macros/latest.svg?color=success)](https://index.scala-lang.org/dmytromitin/auxify/auxify-macros)
[![Join the chat at https://gitter.im/DmytroMitin/AUXify](https://badges.gitter.im/DmytroMitin/AUXify.svg)](https://gitter.im/DmytroMitin/AUXify?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[mvnrepository](https://mvnrepository.com/artifact/com.github.dmytromitin)
[repo1.maven](https://repo1.maven.org/maven2/com/github/dmytromitin/)

## Contents
- [Using AUXify-Shapeless](#using-auxify-shapeless)
- [Using AUXify-Macros](#using-auxify-macros)
  * [@aux (helper for type refinement)](#aux-helper-for-type-refinement)
  * [@self](#self)
  * [@instance (constructor)](#instance--constructor-)
  * [@apply (materializer)](#apply--materializer-)
  * [@delegated](#delegated)
  * [@syntax](#syntax)
  * [@poly](#poly)
- [Using AUXify-Meta](#using-auxify-meta)
  * [Code generation with Scalafix](#code-generation-with-scalafix)
  * [Rewriting with Scalafix](#rewriting-with-scalafix)
  * [Code generation with Scalameta](#code-generation-with-scalameta)

## Using AUXify-Shapeless
Write in `build.sbt`
```scala
scalaVersion := "2.13.3"
//scalaVersion := "2.12.11"
//scalaVersion := "2.11.12"
//scalaVersion := "2.10.7"

resolvers += Resolver.sonatypeRepo("public")

libraryDependencies ++= Seq(
  "com.github.dmytromitin" %% "auxify-shapeless" % [LATEST VERSION],
  "com.github.dmytromitin" %% "shapeless" % (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v >= 11 => "2.4.0-M1-30032020-e6c3f71-PATCH"
    case _                       => "2.4.0-SNAPSHOT-18022020-bf55524-PATCH"
  })
)
```

Helps to overcome [Shapeless](https://github.com/milessabin/shapeless/) limitation that [`shapeless.LabelledGeneric`](https://github.com/milessabin/shapeless/wiki/Feature-overview:-shapeless-2.0.0#generic-representation-of-sealed-families-of-case-classes) is `Symbol`-based rather than `String`-based.

Introduces type classes `SymbolToString`, `StringToSymbol` to convert between symbol singleton type and string singleton type
```scala
implicitly[StringToSymbol.Aux["a", Symbol @@ "a"]]
implicitly[SymbolToString.Aux[Symbol @@ "a", "a"]]
stringToSymbol("a") // returns Symbol("a") of type Symbol @@ "a"
symbolToString(Symbol("a")) // returns "a" of type "a"
```
and `String`-based type class `com.github.dmytromitin.auxify.shapeless.LabelledGeneric`
```scala
case class A(i: Int, s: String, b: Boolean)
implicitly[LabelledGeneric.Aux[A, Record.`"i" -> Int, "s" -> String, "b" -> Boolean`.T]]
LabelledGeneric[A].to(A(1, "a", true)) // field["i"](1) :: field["s"]("a") :: field["b"](true) :: HNil
LabelledGeneric[A].from(field["i"](1) :: field["s"]("a") :: field["b"](true) :: HNil) // A(1, "a", true)
```
Also there are convenient syntaxes
```scala
import com.github.dmytromitin.auxify.shapeless.hlist._
import StringsToSymbols.syntax._
("a".narrow :: "b".narrow :: "c".narrow :: HNil).stringsToSymbols // 'a.narrow :: 'b.narrow :: 'c.narrow :: HNil
import SymbolsToStrings.syntax._
('a.narrow :: 'b.narrow :: 'c.narrow :: HNil).symbolsToStrings // "a".narrow :: "b".narrow :: "c".narrow :: HNil

import com.github.dmytromitin.auxify.shapeless.coproduct._
import StringsToSymbols.syntax._
(Inr(Inr(Inl("c".narrow))) : "a" :+: "b" :+: "c" :+: CNil).stringsToSymbols // Inr(Inr(Inl('c.narrow))) : (Symbol @@ "a") :+: (Symbol @@ "b") :+: (Symbol @@ "c") :+: CNil
import SymbolsToStrings.syntax._
(Inr(Inr(Inl('c.narrow))) : (Symbol @@ "a") :+: (Symbol @@ "b") :+: (Symbol @@ "c") :+: CNil).symbolsToStrings // Inr(Inr(Inl("c".narrow))) : "a" :+: "b" :+: "c" :+: CNil

import com.github.dmytromitin.auxify.shapeless.record._
import StringsToSymbols.syntax._
(field["a"](1) :: field["b"]("s") :: field["c"](true) :: HNil).stringsToSymbols // field[Symbol @@ "a"](1) :: field[Symbol @@ "b"]("s") :: field[Symbol @@ "c"](true) :: HNil
import SymbolsToStrings.syntax._
(field[Symbol @@ "a"](1) :: field[Symbol @@ "b"]("s") :: field[Symbol @@ "c"](true) :: HNil).symbolsToStrings // field["a"](1) :: field["b"]("s") :: field["c"](true) :: HNil

import com.github.dmytromitin.auxify.shapeless.union._
import StringsToSymbols.syntax._
(Inr(Inr(Inl(field["c"](true)))): Union.`"a" -> Int, "b" -> String, "c" -> Boolean`.T).stringsToSymbols // Inr(Inr(Inl(field[Witness.`'c`.T](true)))): Union.`'a -> Int, 'b -> String, 'c -> Boolean`.T
import SymbolsToStrings.syntax._
(Inr(Inr(Inl(field[Symbol @@ "c"](true)))): Union.`'a -> Int, 'b -> String, 'c -> Boolean`.T).symbolsToStrings // Inr(Inr(Inl(field[Witness.`"c"`.T](true)))): Union.`"a" -> Int, "b" -> String, "c" -> Boolean`.T
```

You can play with AUXify online at Scastie: https://scastie.scala-lang.org/r52fCgloRc2VVM5FnNmbsQ

## Using AUXify-Macros
Write in `build.sbt`
```scala
scalaVersion := "2.13.3"
//scalaVersion := "2.12.11"
//scalaVersion := "2.11.12"
//scalaVersion := "2.10.7"

resolvers += Resolver.sonatypeRepo("public")

libraryDependencies += "com.github.dmytromitin" %% "auxify-macros" % [LATEST VERSION]

scalacOptions += "-Ymacro-annotations" // in Scala >= 2.13
//addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full) // in Scala <= 2.12
```

## @aux (helper for type refinement)
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
  type ++ = Succ[Self]
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

Method materializing type class can return more precise type than the one of implicit to be found (like `the` in [Shapeless](https://github.com/milessabin/shapeless) or `summon` in [Dotty](https://github.com/lampepfl/dotty)).
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

object Show {
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

## @poly
Transforms
```scala
@poly
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
  object addPoly extends Poly2 {
    implicit def cse[N <: Nat, M <: Nat](implicit add: Add[N, M]): Case.Aux[N, M, add.Out] = at((n, m) => add(n, m)) 
  }
}
```

`@poly` is not implemented yet. See [issue](https://github.com/DmytroMitin/AUXify/issues/34).

## Using AUXify-Meta

Currently only `@aux` is implemented as Scalafix rewriting rule. It's a semantic rule since we need companion object.

Meta annotation `@aux` works only with classes on contrary to macro annotation `@aux` working only with traits. 
[This will be fixed.](https://github.com/DmytroMitin/AUXify/issues/10) 

### Code generation with Scalafix
For code generation with [Scalameta](https://scalameta.org/) + [SemanticDB](https://scalameta.org/docs/semanticdb/guide.html) + [Scalafix](https://scalacenter.github.io/scalafix/) write in `project/plugins.sbt`
```scala
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.18")
```
and in `build.sbt`
```scala
import com.geirsson.coursiersmall.{Repository => R}

lazy val V = _root_.scalafix.sbt.BuildInfo

inThisBuild(Seq(
  scalaVersion := V.scala213,
  addCompilerPlugin(scalafixSemanticdb),
  scalafixResolvers in ThisBuild += new R.Maven("https://oss.sonatype.org/content/groups/public/"),
  // brings rewriting rules
  scalafixDependencies in ThisBuild += "com.github.dmytromitin" %% "auxify-meta" % [LATEST VERSION],
  scalacOptions += "-Yrangepos" // for SemanticDB
))

lazy val in = project
  .settings(
    // brings meta annotations
    libraryDependencies += "com.github.dmytromitin" %% "auxify-meta-core" % [LATEST VERSION]
  )

lazy val out = project
  .settings(
    sourceGenerators.in(Compile) += Def.taskDyn {
      val root = baseDirectory.in(ThisBuild).value.toURI.toString
      val from = sourceDirectory.in(in, Compile).value
      val to = sourceManaged.in(Compile).value
      val outFrom = from.toURI.toString.stripSuffix("/").stripPrefix(root)
      val outTo = to.toURI.toString.stripSuffix("/").stripPrefix(root)
      Def.task {
        scalafix
          .in(in, Compile)
          .toTask(s" AuxRule --out-from=$outFrom --out-to=$outTo")
          .value
        (to ** "*.scala").get
      }
    }.taskValue,
    
    // for import statement and if meta annotation is not expanded
    libraryDependencies += "com.github.dmytromitin" %% "auxify-meta-core" % [LATEST VERSION]
  )
```
Annotated code should be placed in `in/src/main/scala`. Code generation in `out/target/scala-2.13/src_managed/main/scala` can be run with `sbt out/compile`.

Example project is [here](https://github.com/DmytroMitin/scalafix-codegen).

### Rewriting with Scalafix
For using rewriting rules with [Scalameta](https://scalameta.org/) + [SemanticDB](https://scalameta.org/docs/semanticdb/guide.html) + [Scalafix](https://scalacenter.github.io/scalafix/) write in `project/plugins.sbt`
```scala
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.18")
```
and in `build.sbt`
```scala
// on the top
import com.geirsson.coursiersmall.{Repository => R}
scalafixResolvers in ThisBuild += new R.Maven("https://oss.sonatype.org/content/groups/public/")
scalafixDependencies in ThisBuild += "com.github.dmytromitin" %% "auxify-meta" % [LATEST VERSION]

scalaVersion := "2.13.3"
//scalaVersion := "2.12.11"
//scalaVersion := "2.11.12"

libraryDependencies += "com.github.dmytromitin" %% "auxify-meta-core" % [LATEST VERSION]

addCompilerPlugin(scalafixSemanticdb)

scalacOptions += "-Yrangepos" // for SemanticDB
```

Rewriting can be run with `sbt "scalafix AuxRule"` (details are [here](https://scalacenter.github.io/scalafix/docs/users/installation.html)).

### Code generation with Scalameta
For code generating syntacticly with pure [Scalameta](https://scalameta.org/) (without [SemanticDB](https://scalameta.org/docs/semanticdb/guide.html) and [Scalafix](https://scalacenter.github.io/scalafix/)) write in `project/build.sbt`
```scala
resolvers += Resolver.sonatypeRepo("public")
libraryDependencies += "com.github.dmytromitin" %% "auxify-syntactic-meta" % [LATEST VERSION]
```
and in `build.sbt`
```scala
inThisBuild(Seq(
  scalaVersion := "2.13.3"
  //scalaVersion := "2.12.11"
  //scalaVersion := "2.11.12"
))

lazy val in = project
  .settings(
    libraryDependencies += "com.github.dmytromitin" %% "auxify-meta-core" % [LATEST VERSION]
  )

lazy val out = project
  .settings(
    sourceGenerators in Compile += Def.task {
      import com.github.dmytromitin.auxify.meta.syntactic.ScalametaTransformer
      
      val finder: PathFinder = sourceDirectory.in(in, Compile).value ** "*.scala"
  
      for(inputFile <- finder.get) yield {
        val inputStr = IO.read(inputFile)
        val outputFile = sourceManaged.in(Compile).value / inputFile.name
        val outputStr = ScalametaTransformer.transform(inputStr)
        IO.write(outputFile, outputStr)
        outputFile
      }
    }.taskValue,
    
    // for import statement and if meta annotation is not expanded
    libraryDependencies += "com.github.dmytromitin" %% "auxify-meta-core" % [LATEST VERSION]
  )
```
Annotated code should be placed in `in/src/main/scala`. Code generation in `out/target/scala-2.13/src_managed/main` can be run with `sbt out/compile`.

Example project is [here](https://github.com/DmytroMitin/scalameta-demo).
