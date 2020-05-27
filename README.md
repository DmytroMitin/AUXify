# AUXify

[![Build Status](https://travis-ci.org/DmytroMitin/AUXify.svg?branch=master)](https://travis-ci.org/DmytroMitin/AUXify)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.dmytromitin/auxify-macros_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.dmytromitin/auxify-macros_2.13)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.dmytromitin/auxify-macros_2.13.svg?label=maven%20central&color=success)](https://search.maven.org/search?q=g:%22com.github.dmytromitin%22%20AND%20a:%22auxify-macros_2.13%22)
[![Sonatype Snapshots](https://img.shields.io/nexus/r/https/oss.sonatype.org/com.github.dmytromitin/auxify-macros_2.13.svg?color=success)](https://oss.sonatype.org/content/groups/public/com/github/dmytromitin/auxify-macros_2.13/) [![Join the chat at https://gitter.im/DmytroMitin/AUXify](https://badges.gitter.im/DmytroMitin/AUXify.svg)](https://gitter.im/DmytroMitin/AUXify?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[mvnrepository](https://mvnrepository.com/artifact/com.github.dmytromitin)
[repo1.maven](https://repo1.maven.org/maven2/com/github/dmytromitin/)

## Contents
- [Using AUXify-Macros](#using-auxify-macros)
  * [@aux (helper for type refinement)](#aux-helper-for-type-refinement)
  * [@self](#self)
  * [@instance (constructor)](#instance--constructor-)
  * [@apply (materializer)](#apply--materializer-)
  * [@delegated](#delegated)
  * [@syntax](#syntax)
- [Using AUXify-Meta](#using-auxify-meta)
  * [Code generation with Scalafix](#code-generation-with-scalafix)
  * [Rewriting with Scalafix](#rewriting-with-scalafix)
  * [Code generation with Scalameta](#code-generation-with-scalameta)

## Using AUXify-Macros
Write in `build.sbt`
```scala
scalaVersion := "2.13.0"
//scalaVersion := "2.12.8"
//scalaVersion := "2.11.12"
//scalaVersion := "2.10.7"

resolvers += Resolver.sonatypeRepo("releases")

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

## Using AUXify-Meta

Currently only `@aux` is implemented as Scalafix rewriting rule. It's a semantic rule since we need companion object.

Meta annotation `@aux` works only with classes on contrary to macro annotation `@aux` working only with traits. 
[This will be fixed.](https://github.com/DmytroMitin/AUXify/issues/10) 

### Code generation with Scalafix
For code generation with [Scalameta](https://scalameta.org/) + [SemanticDB](https://scalameta.org/docs/semanticdb/guide.html) + [Scalafix](https://scalacenter.github.io/scalafix/) write in `project/plugins.sbt`
```scala
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.5")
```
and in `build.sbt`
```scala
import com.geirsson.coursiersmall.{Repository => R}

lazy val V = _root_.scalafix.sbt.BuildInfo

inThisBuild(Seq(
  scalaVersion := V.scala212,
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
Annotated code should be placed in `in/src/main/scala`. Code generation in `out/target/scala-2.12/src_managed/main/scala` can be run with `sbt out/compile`.

Example project is [here](https://github.com/DmytroMitin/scalafix-codegen).

### Rewriting with Scalafix
For using rewriting rules with [Scalameta](https://scalameta.org/) + [SemanticDB](https://scalameta.org/docs/semanticdb/guide.html) + [Scalafix](https://scalacenter.github.io/scalafix/) write in `project/plugins.sbt`
```scala
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.5")
```
and in `build.sbt`
```scala
// on the top
import com.geirsson.coursiersmall.{Repository => R}
scalafixResolvers in ThisBuild += new R.Maven("https://oss.sonatype.org/content/groups/public/")
scalafixDependencies in ThisBuild += "com.github.dmytromitin" %% "auxify-meta" % "0.5"

scalaVersion := "2.12.8"

libraryDependencies += "com.github.dmytromitin" %% "auxify-meta-core" % "0.5"

addCompilerPlugin(scalafixSemanticdb)

scalacOptions += "-Yrangepos" // for SemanticDB
```

Rewriting can be run with `sbt "scalafix AuxRule"` (details are [here](https://scalacenter.github.io/scalafix/docs/users/installation.html)).

### Code generation with Scalameta
For code generating syntacticly with pure [Scalameta](https://scalameta.org/) (without [SemanticDB](https://scalameta.org/docs/semanticdb/guide.html) and [Scalafix](https://scalacenter.github.io/scalafix/)) write in `project/build.sbt`
```scala
resolvers += Resolver.sonatypeRepo("releases")
libraryDependencies += "com.github.dmytromitin" %% "auxify-syntactic-meta" % [LATEST VERSION]
```
and in `build.sbt`
```scala
inThisBuild(Seq(
  scalaVersion := "2.13.0"
  //scalaVersion := "2.12.8"
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
Annotated code should be placed in `in/src/main/scala`. Code generation in `out/target/scala-2.12/src_managed/main` can be run with `sbt out/compile`.

Example project is [here](https://github.com/DmytroMitin/scalameta-demo).
