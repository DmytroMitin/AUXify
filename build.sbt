lazy val scala213 = "2.13.0"
lazy val scala212 = "2.12.8"
lazy val scala211 = "2.11.12"
lazy val scala210 = "2.10.7"
lazy val supportedScalaVersions = List(scala213, scala212, scala211, scala210)

ThisBuild / name         := "auxify"
ThisBuild / organization := "com.github.dmytromitin"
ThisBuild / version      := "0.1"
ThisBuild / scalaVersion := scala213

lazy val root = (project in file("."))
  .aggregate(macros, core)
  .settings(
    crossScalaVersions := Nil,
    publish / skip := true,
  )

val commonSettings = Seq(
  crossScalaVersions := supportedScalaVersions,
  scalacOptions ++= (
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v >= 13 => Seq("-Ymacro-annotations")
      case _                       => Nil
    }) ++ Seq(
//    "-Ymacro-debug-lite",
//    "-Ymacro-debug-verbose",
//    "-Ydebug",
//    "-Xprint:typer",
//    "-Xprint-types",
  ),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ),
  libraryDependencies ++= (
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v >= 13 => Seq(
        "org.typelevel" % "macro-compat_2.13.0-RC2" % "1.1.1",
      )
      case _                       => Seq(
        compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
        "org.typelevel" %% "macro-compat" % "1.1.1",
      )
    }
  ) ++ Seq(
    "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
  )
)

lazy val macros: Project = (project in file("macros")).settings(
  libraryDependencies += scalaOrganization.value % "scala-reflect" % scalaVersion.value,
  commonSettings,
)

lazy val core: Project = (project in file("core")).dependsOn(macros).settings(
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % Test,
  commonSettings,
)
