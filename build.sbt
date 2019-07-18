name := "auxify"
organization := "com.github.dmytromitin"
version := "0.1"

val commonSettings = Seq(
  scalaVersion := "2.13.0",
  scalacOptions += "-Ymacro-annotations",
)

lazy val macros: Project = (project in file("macros")).settings(
  libraryDependencies += scalaOrganization.value % "scala-reflect" % scalaVersion.value,
  commonSettings,
)

lazy val core: Project = (project in file("core")).dependsOn(macros).settings(
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % Test,
  commonSettings,
)
