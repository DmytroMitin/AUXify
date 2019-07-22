lazy val scala213 = "2.13.0"
lazy val scala212 = "2.12.8"
lazy val scala211 = "2.11.12"
lazy val scala210 = "2.10.7"
lazy val supportedScalaVersions = List(scala213, scala212, scala211, scala210)

inThisBuild(Seq(
  name                 := "auxify",
  organization         := "com.github.dmytromitin",
  organizationName     := "Dmytro Mitin",
  organizationHomepage := Some(url("https://github.com/DmytroMitin")),
  version              := "0.3",
  scalaVersion         := scala213,
  scmInfo := Some(ScmInfo(
      url("https://github.com/DmytroMitin/AUXify"),
      "https://github.com/DmytroMitin/AUXify.git"
  )),
  developers := List(Developer(
    id = "DmytroMitin",
    name = "Dmytro Mitin",
    email = "dmitin3@gmail.com",
    url = url("https://github.com/DmytroMitin")
  )),
  description := "Library providing macro annotations @aux, @self, @instance, @apply, @delegated, @syntax",
  licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  homepage := Some(url("https://github.com/DmytroMitin/AUXify")),
  // Remove all additional repository other than Maven Central from POM
  pomIncludeRepository := { _ => false },
  publishMavenStyle := true,
  useGpg := true,
  credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credential"),
))

ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

lazy val root = (project in file("."))
  .aggregate(macros, core)
  .settings(
    crossScalaVersions := Nil,
    publish / skip := true,
  )

lazy val commonSettings = Seq(
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
    scalaOrganization.value % "scala-compiler" % scalaVersion.value % Provided,
  )
)

lazy val macros = (project in file("macros")).settings(
  name := "auxify-macros",
  libraryDependencies += scalaOrganization.value % "scala-reflect" % scalaVersion.value,
  commonSettings,
)

lazy val core = (project in file("core")).dependsOn(macros).settings(
  name := "auxify-core",
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % Test,
  commonSettings,
  publish / skip := true,
)
