package chill

import sbt._
import Keys._
import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import com.typesafe.tools.mima.plugin.MimaKeys._

import scala.collection.JavaConverters._

object ChillBuild extends Build {
  val kryoVersion = "2.21"

  val sharedSettings = Project.defaultSettings ++ mimaDefaultSettings ++ Seq(

    version := "0.3.3",
    organization := "com.twitter",
    scalaVersion := "2.9.3",
    crossScalaVersions := Seq("2.9.3", "2.10.0"),
    scalacOptions ++= Seq("-unchecked", "-deprecation"),

    // Twitter Hadoop needs this, sorry 1.7 fans
    javacOptions ++= Seq("-target", "1.6", "-source", "1.6", "-Xlint:-options"),
    javacOptions in doc := Seq("-source", "1.6"),

    resolvers ++= Seq(
      Opts.resolver.sonatypeSnapshots,
      Opts.resolver.sonatypeReleases
    ),
    libraryDependencies ++= Seq(
      "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
      "org.scala-tools.testing" %% "specs" % "1.6.9" % "test",
      "com.esotericsoftware.kryo" % "kryo" % kryoVersion
    ),

    parallelExecution in Test := true,

    // Publishing options:
    publishMavenStyle := true,
    publishTo <<= version { v =>
      Some(
        if (v.trim.toUpperCase.endsWith("SNAPSHOT"))
          Opts.resolver.sonatypeSnapshots
        else
          Opts.resolver.sonatypeStaging
      )
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
    chillBijection,
    chillStorm,
    chillJava,
    chillHadoop
  )

  /**
    * This returns the youngest jar we released that is compatible
    * with the current.
    */
  val unreleasedModules = Set[String]("akka")
  val javaOnly = Set[String]("storm", "java", "hadoop")

  def youngestForwardCompatible(subProj: String) =
    Some(subProj)
      .filterNot(unreleasedModules.contains(_))
      .map { s =>
      val suffix = if (javaOnly.contains(s)) "" else "_2.9.3"
      "com.twitter" % ("chill-" + s + suffix) % "0.3.2"
    }

  def module(name: String) = {
    val id = "chill-%s".format(name)
    Project(id = id, base = file(id), settings = sharedSettings ++ Seq(
      Keys.name := id,
      previousArtifact := youngestForwardCompatible(name))
    )
  }

  // We usually do the pattern of having a core module, but we don't want to cause
  // pain for legacy deploys. With this, they can stay the same.
  lazy val chill = Project(
    id = "chill",
    base = file("chill-scala"),
    settings = sharedSettings
  ).settings(
    name := "chill",
    previousArtifact := Some("com.twitter" % "chill_2.9.3" % "0.3.0"),
    libraryDependencies ++= Seq(
      "org.ow2.asm" % "asm-commons" % "4.0"
    )
  ).dependsOn(chillJava)

  lazy val chillAkka = module("akka").settings(
    resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "0.3.1",
      "com.typesafe.akka" % "akka-actor" % "2.0.5"
    )
  ).dependsOn(chill % "test->test;compile->compile")

  lazy val chillBijection = module("bijection").settings(
    libraryDependencies ++= Seq(
      "com.twitter" %% "bijection-core" % "0.5.2"
    )
  ).dependsOn(chill % "test->test;compile->compile")

  // This can only have java deps!
  lazy val chillJava = module("java").settings(
    crossPaths := false,
    autoScalaLibrary := false
  )

  // This can only have java deps!
  lazy val chillStorm = module("storm").settings(
    crossPaths := false,
    autoScalaLibrary := false,
    resolvers ++= Seq(
      "Clojars Repository" at "http://clojars.org/repo",
      "Conjars Repository" at "http://conjars.org/repo"
    ),
    libraryDependencies += "storm" % "storm" % "0.9.0-wip9"
  ).dependsOn(chillJava)

  // This can only have java deps!
  lazy val chillHadoop = module("hadoop").settings(
    crossPaths := false,
    autoScalaLibrary := false,
    libraryDependencies ++= Seq(
      "org.apache.hadoop" % "hadoop-core" % "0.20.2" % "provided",
      "org.slf4j" % "slf4j-api" % "1.6.6",
      "org.slf4j" % "slf4j-log4j12" % "1.6.6" % "provided"
    )
  ).dependsOn(chillJava)
}
