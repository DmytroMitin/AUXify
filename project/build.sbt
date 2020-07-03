libraryDependencies += "org.scalameta" %% "scalameta" % "4.3.18"

unmanagedSourceDirectories in Compile += baseDirectory.value / ".." / "syntactic-meta" / "src" / "main" / "scala" 