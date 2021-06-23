addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.9.2")
addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.6.3")
addSbtPlugin("com.typesafe.sbt" % "sbt-osgi" % "0.9.6")
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.3")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.8.2")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.29")
addSbtPlugin("com.geirsson" % "sbt-ci-release" % "1.5.7")
addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.4")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.11.4"
