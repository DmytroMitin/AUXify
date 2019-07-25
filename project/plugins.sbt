resolvers += Resolver.sonatypeRepo("releases")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.4")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.1")

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.5")
