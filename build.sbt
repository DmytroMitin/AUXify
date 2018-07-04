name := "auxify"
organization := "com.github.dmytromitin"
version := "0.1"
scalaVersion := "2.12.6"

val commonSettings = addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

lazy val macros: Project = (project in file("macros")).settings(
  libraryDependencies += scalaOrganization.value % "scala-reflect" % scalaVersion.value,
  commonSettings
)

lazy val core: Project = (project in file("core")).dependsOn(macros).settings(
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  commonSettings
)
