package chill

import sbt._
import Keys._
import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import com.typesafe.tools.mima.plugin.MimaKeys._
import com.typesafe.sbt.SbtScalariform._


object ChillBuild extends Build {
  val kryoVersion = "3.0.2"


  def isScala210x(scalaVersion: String) = scalaVersion match {
      case version if version startsWith "2.10" => true
      case _ => false
  }

  val sharedSettings = Defaults.coreDefaultSettings ++ mimaDefaultSettings ++ scalariformSettings ++ Seq(

    version := "0.7.0-SNAPSHOT",
    organization := "com.twitter",
    scalaVersion := "2.11.7",
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    ScalariformKeys.preferences := formattingPreferences,

    javacOptions ++= Seq("-Xlint:-options", "-Xlint:unchecked"),
    javacOptions in doc := Seq(),

    resolvers ++= Seq(
      Opts.resolver.sonatypeSnapshots,
      Opts.resolver.sonatypeReleases
    ),
    libraryDependencies ++= Seq(
      "org.scalacheck" %% "scalacheck" % "1.12.4" % "test",
      "org.scalatest" %% "scalatest" % "2.2.5" % "test",
      "com.esotericsoftware" % "kryo-shaded" % kryoVersion
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
          //"twttr" at "http://artifactory.local.twitter.com/libs-releases-local"
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
    chillScrooge,
    chillStorm,
    chillJava,
    chillHadoop,
    chillThrift,
    chillProtobuf,
    chillAkka,
    chillAvro,
    chillAlgebird
    )

  lazy val formattingPreferences = {
   import scalariform.formatter.preferences._
   FormattingPreferences().
     setPreference(AlignParameters, false).
     setPreference(PreserveSpaceBeforeArguments, true)
  }

  /**
    * This returns the youngest jar we released that is compatible
    * with the current.
    */
  val unreleasedModules = Set[String]("akka")
  val javaOnly = Set[String]("storm", "java", "hadoop", "thrift")

  def youngestForwardCompatible(subProj: String) =
    Some(subProj)
      .filterNot(unreleasedModules.contains)
      .map { s =>
      val suffix = if (javaOnly.contains(s)) "" else "_2.10"
      "com.twitter" % ("chill-" + s + suffix) % "0.6.0"
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
    previousArtifact := Some("com.twitter" % "chill_2.10" % "0.6.0")
  ).dependsOn(chillJava)

  lazy val chillAkka = module("akka").settings(
    resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.0",
      "com.typesafe.akka" %% "akka-actor" % "2.3.11" % "provided"
    )
  ).dependsOn(chill % "test->test;compile->compile")

  lazy val chillBijection = module("bijection").settings(
    libraryDependencies ++= Seq(
      "com.twitter" %% "bijection-core" % "0.8.1"
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
    libraryDependencies += "storm" % "storm" % "0.9.0-wip9" % "provided"
  ).dependsOn(chillJava)

  // This can only have java deps!
  lazy val chillHadoop = module("hadoop").settings(
    crossPaths := false,
    autoScalaLibrary := false,
    libraryDependencies ++= Seq(
      "org.apache.hadoop" % "hadoop-core" % "0.20.2" % "provided",
      "org.slf4j" % "slf4j-api" % "1.7.12",
      "org.slf4j" % "slf4j-log4j12" % "1.7.12" % "provided"
    )
  ).dependsOn(chillJava)

  // This can only have java deps!
  lazy val chillThrift = module("thrift").settings(
    crossPaths := false,
    autoScalaLibrary := false,
    libraryDependencies ++= Seq(
      "org.apache.thrift" % "libthrift" % "0.6.1" % "provided"
    )
  )

   def scroogeBuildDeps(scalaVersion: String): Seq[sbt.ModuleID] = isScala210x(scalaVersion) match {
      case false => Seq()
      case true => Seq(
        "com.twitter" %% "scrooge-serializer" % "3.13.0"
     )
  }

  lazy val chillScrooge = module("scrooge").settings(
    skip in compile := !isScala210x(scalaVersion.value),
    skip in test := !isScala210x(scalaVersion.value),
    publishArtifact := isScala210x(scalaVersion.value),
    libraryDependencies ++= Seq(
      "org.apache.thrift" % "libthrift" % "0.6.1" exclude("junit", "junit")
    ) ++ scroogeBuildDeps(scalaVersion.value)
  ).dependsOn(chill % "test->test;compile->compile")

  // This can only have java deps!
  lazy val chillProtobuf = module("protobuf").settings(
    crossPaths := false,
    autoScalaLibrary := false,
    libraryDependencies ++= Seq(
      "com.google.protobuf" % "protobuf-java" % "2.3.0" % "provided"
    )
  ).dependsOn(chillJava)

  lazy val chillAvro = module("avro").settings(
    libraryDependencies ++= Seq(
      "com.twitter" %% "bijection-avro" % "0.8.0",
      "junit" % "junit" % "4.5" % "test"
    )
  ).dependsOn(chill,chillJava, chillBijection)

  lazy val chillAlgebird = module("algebird").settings(
    libraryDependencies ++= Seq(
      "com.twitter" %% "algebird-core" % "0.10.0"
    )
  ).dependsOn(chill)
}
