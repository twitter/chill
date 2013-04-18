package chill

import sbt._
import Keys._
import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import com.typesafe.tools.mima.plugin.MimaKeys._

import scala.collection.JavaConverters._

object ChillBuild extends Build {
  val kryoVersion = "2.17"

  val sharedSettings = Project.defaultSettings ++
    mimaDefaultSettings ++ Seq(

  version := "0.2.1-SNAPSHOT",

  organization := "com.twitter",

  crossScalaVersions := Seq("2.9.2", "2.10.0"),

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
  ).aggregate(chill)

  // We usually do the pattern of having a core module, but we don't want to cause
  // pain for legacy deploys. With this, they can stay the same.
  lazy val chill = Project(
    id = "chill",
    base = file("chill-scala"),
    settings = sharedSettings
  ).settings(
    name := "chill",
    previousArtifact := Some("com.twitter" % "chill_2.9.2" % "0.2.0"),
    libraryDependencies ++= Seq(
      "com.twitter" %% "bijection-core" % "0.3.0",
      "org.ow2.asm" % "asm-commons" % "4.0"
    )
  )
}
