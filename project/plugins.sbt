resolvers += Resolver.sonatypeRepo("releases")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.2")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.1")

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.17")