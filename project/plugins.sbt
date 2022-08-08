addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.0")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.2")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.10.1")
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.10")
addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.4")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.11.11"
