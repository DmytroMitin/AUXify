libraryDependencies += "org.scalameta" %% "scalameta" % "4.3.14"

unmanagedSourceDirectories in Compile += baseDirectory.value / ".." / "syntactic-meta" / "src" / "main" / "scala" 