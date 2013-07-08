package chill

import sbt._
import Keys._
import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import com.typesafe.tools.mima.plugin.MimaKeys._

import scala.collection.JavaConverters._

object ChillBuild extends Build {
  val kryoVersion = "2.21"

  val sharedSettings = Project.defaultSettings ++
    mimaDefaultSettings ++ Seq(

  version := "0.2.3",

  organization := "com.twitter",

  scalaVersion := "2.9.3",

  crossScalaVersions := Seq("2.9.3", "2.10.0"),

  scalacOptions ++= Seq("-unchecked", "-deprecation"),

  resolvers ++= Seq(
    "sonatype-snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
    "sonatype-releases"  at "http://oss.sonatype.org/content/repositories/releases"
  ),

  libraryDependencies ++= Seq(
    "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
    "org.scala-tools.testing" %% "specs" % "1.6.9" % "test",
    "com.esotericsoftware.kryo" % "kryo" % kryoVersion
  ),

  parallelExecution in Test := true,

  // Publishing options:

  publishMavenStyle := true,

  publishTo <<= version { (v: String) =>
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },

  publishArtifact in Test := false,

  pomIncludeRepository := { x => false },

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
    )

  // Aggregated project
  lazy val chillAll = Project(
    id = "chill-all",
    base = file("."),
    settings = sharedSettings
  ).settings(
    test := { },
    publish := { },
    publishLocal := { }
  ).aggregate(
    chill,
    chillStorm,
    chillJava,
    chillHadoop
  )

  // We usually do the pattern of having a core module, but we don't want to cause
  // pain for legacy deploys. With this, they can stay the same.
  lazy val chill = Project(
    id = "chill",
    base = file("chill-scala"),
    settings = sharedSettings
  ).settings(
    name := "chill",
    previousArtifact := Some("com.twitter" % "chill_2.9.2" % "0.2.2"),
    libraryDependencies ++= Seq(
      "com.twitter" %% "bijection-core" % "0.5.2",
      "org.ow2.asm" % "asm-commons" % "4.0"
    )
  )

  // This can only have java deps!
  lazy val chillJava = Project(
    id = "chill-java",
    base = file("chill-java"),
    settings = sharedSettings
  ).settings(
    autoScalaLibrary := false,
    name := "chill-java",
    previousArtifact := None
  )

  lazy val chillStorm = Project(
    id = "chill-storm",
    base = file("chill-storm"),
    settings = sharedSettings
  ).settings(
    name := "chill-storm",
    resolvers ++= Seq(
      "Clojars Repository" at "http://clojars.org/repo",
      "Conjars Repository" at "http://conjars.org/repo"
    ),
    libraryDependencies += "storm" % "storm" % "0.9.0-wip9"
  ).dependsOn(chill)

  // This can only have java deps!
  lazy val chillHadoop = Project(
    id = "chill-hadoop",
    base = file("chill-hadoop"),
    settings = sharedSettings
  ).settings(
    autoScalaLibrary := false,
    name := "chill-hadoop",
    libraryDependencies ++= Seq(
      "org.apache.hadoop" % "hadoop-core" % "0.20.2" % "provided",
      "org.slf4j" % "slf4j-api" % "1.6.6",
      "org.slf4j" % "slf4j-log4j12" % "1.6.6" % "provided"
    ),
    previousArtifact := None
  ).dependsOn(chillJava)
}
