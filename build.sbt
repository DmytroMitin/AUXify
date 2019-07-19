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
    publish / skip := true
  )

val commonSettings = Seq(
  crossScalaVersions := supportedScalaVersions,
  scalacOptions ++= Seq(
    "-Ymacro-annotations",
//    "-Ymacro-debug-lite",
//    "-Ymacro-debug-verbose",
//    "-Ydebug",
//    "-Xprint:typer",
//    "-Xprint-types",
  ),
)

lazy val macros: Project = (project in file("macros")).settings(
  libraryDependencies += scalaOrganization.value % "scala-reflect" % scalaVersion.value,
  commonSettings,
)

lazy val core: Project = (project in file("core")).dependsOn(macros).settings(
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % Test,
  commonSettings,
)
