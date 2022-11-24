addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.1")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.9.3")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.10.4")
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.10")
addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.6")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.11.11"
