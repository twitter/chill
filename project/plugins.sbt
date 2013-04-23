resolvers += Resolver.url("sbt-plugin-releases", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.7")

addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.1.5")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.4.0")

