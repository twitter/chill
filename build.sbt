name := "chill"

version := "0.2.1-SNAPSHOT"

organization := "com.twitter"

crossScalaVersions := Seq("2.9.2", "2.10.0")

scalacOptions ++= Seq("-unchecked", "-deprecation")

resolvers ++= Seq(
  "sonatype-snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "sonatype-releases"  at "http://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
  "org.scala-tools.testing" %% "specs" % "1.6.9" % "test",
  "com.esotericsoftware.kryo" % "kryo" % "2.17",
  "com.twitter" %% "bijection-core" % "0.3.0",
  "org.ow2.asm" % "asm-commons" % "4.0"
)

parallelExecution in Test := true

// Publishing options:

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { x => false }

pomExtra := (
  <url>https://github.com/twitter/chill</url>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      <distribution>repo</distribution>
      <comments>A business-friendly OSS license</comments>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:twitter/chill.git</url>
    <connection>scm:git:git@github.com:twitter/chill.git</connection>
  </scm>
  <developers>
    <developer>
      <id>oscar</id>
      <name>Oscar Boykin</name>
      <url>http://twitter.com/posco</url>
    </developer>
    <developer>
      <id>sritchie</id>
      <name>Sam Ritchie</name>
      <url>http://twitter.com/sritchie</url>
    </developer>
  </developers>)
