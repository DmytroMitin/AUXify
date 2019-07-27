libraryDependencies += "org.scalameta" %% "scalameta" % "4.2.0"

unmanagedSourceDirectories in Compile += baseDirectory.value / ".." / "syntactic-meta" / "src" / "main" / "scala" 