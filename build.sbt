name := "chill"

version := "0.0.2"

organization := "com.twitter"

scalaVersion := "2.9.2"

// Use ScalaCheck

libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
  "org.scala-tools.testing" % "specs_2.9.0-1" % "1.6.8" % "test",
  "com.esotericsoftware.kryo" % "kryo" % "2.17",
  "commons-codec" % "commons-codec" % "1.7"
)

parallelExecution in Test := true

// Publishing options:

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "http://artifactory.local.twitter.com/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("twitter-snapshots" at nexus + "libs-snapshots-local")
  else
    Some("twitter-releases"  at nexus + "libs-releases-local")
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
