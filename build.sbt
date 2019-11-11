import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import sbtrelease.ReleaseStateTransformations._

val akkaVersion = "2.4.16"
val algebirdVersion = "0.13.5"
val bijectionVersion = "0.9.6"
val kryoVersion = "4.0.2"
val scroogeVersion = "4.12.0"

val sharedSettings = mimaDefaultSettings ++ Seq(
  organization := "com.twitter",
  scalaVersion := "2.11.12",
  crossScalaVersions := Seq("2.10.7", "2.11.12", "2.12.8"),
  scalacOptions ++= Seq("-unchecked", "-deprecation"),
  scalacOptions ++= {
    scalaVersion.value match {
      case v if v.startsWith("2.10") => Nil
      case v if v.startsWith("2.11") => Seq("-Ywarn-unused", "-Ywarn-unused-import")
      case _                         => Seq("-Ywarn-unused")
    }
  },
  scalariformPreferences := formattingPreferences,
  scalariformAutoformat := false,
  // Twitter Hadoop needs this, sorry 1.7 fans
  javacOptions ++= Seq("-target", "1.6", "-source", "1.6", "-Xlint:-options"),
  javacOptions in doc := Seq("-source", "1.6"),
  resolvers ++= Seq(
    Opts.resolver.sonatypeSnapshots,
    Opts.resolver.sonatypeReleases
  ),
  libraryDependencies ++= Seq(
    "org.scalacheck" %% "scalacheck" % "1.14.0" % "test",
    "org.scalatest" %% "scalatest" % "3.0.8" % "test",
    "com.esotericsoftware" % "kryo-shaded" % kryoVersion
  ),
  parallelExecution in Test := true,
  // Publishing options:
  releaseCrossBuild := false, // needs to be false for sbt-doge
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    releaseStepCommandAndRemaining("+test"),
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("+publishSigned"),
    setNextVersion,
    commitNextVersion,
    pushChanges
  ),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { x =>
    false
  },
  publishTo := Some(
    if (version.value.trim.toUpperCase.endsWith("SNAPSHOT"))
      Opts.resolver.sonatypeSnapshots
    else
      Opts.resolver.sonatypeStaging
  ),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/twitter/chill"),
      "scm:git:git@github.com:twitter/chill.git"
    )
  ),
  pomExtra := (<url>https://github.com/twitter/chill</url>
        <licenses>
      <license>
      <name>Apache 2</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        <distribution>repo</distribution>
      <comments>A business-friendly OSS license</comments>
      </license>
      </licenses>
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
  base = file(".")
).settings(sharedSettings)
  .settings(noPublishSettings)
  .settings(
    mimaPreviousArtifacts := Set.empty
  )
  .aggregate(
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
  FormattingPreferences()
    .setPreference(AlignParameters, false)
    .setPreference(PreserveSpaceBeforeArguments, true)
}

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  test := {},
  publishArtifact := false
)

/**
 * This returns the youngest jar we released that is compatible
 * with the current.
 */
val unreleasedModules = Set[String]("akka")
val javaOnly = Set[String]("storm", "java", "hadoop", "thrift", "protobuf")
val binaryCompatVersion = "0.9.2"

def youngestForwardCompatible(subProj: String) =
  Some(subProj)
    .filterNot(unreleasedModules.contains(_))
    .map { s =>
      if (javaOnly.contains(s))
        "com.twitter" % ("chill-" + s) % binaryCompatVersion
      else
        "com.twitter" %% ("chill-" + s) % binaryCompatVersion
    }

val ignoredABIProblems = {
  import com.typesafe.tools.mima.core._
  import com.typesafe.tools.mima.core.ProblemFilters._
  Seq(
    exclude[MissingTypesProblem]("com.twitter.chill.storm.BlizzardKryoFactory")
  )
}

def module(name: String) = {
  val id = "chill-%s".format(name)
  Project(id = id, base = file(id))
    .settings(sharedSettings)
    .settings(
      Keys.name := id,
      mimaPreviousArtifacts := youngestForwardCompatible(name).toSet,
      mimaBinaryIssueFilters ++= ignoredABIProblems,
      // Disable cross publishing for java artifacts
      publishArtifact :=
        (if (javaOnly.contains(name) && scalaVersion.value.startsWith("2.11")) false else true)
    )
}

// We usually do the pattern of having a core module, but we don't want to cause
// pain for legacy deploys. With this, they can stay the same.
lazy val chill = Project(
  id = "chill",
  base = file("chill-scala")
).settings(sharedSettings)
  .settings(
    name := "chill",
    mimaPreviousArtifacts := Set("com.twitter" %% "chill" % binaryCompatVersion)
  )
  .dependsOn(chillJava)

def akka(scalaVersion: String) =
  (scalaVersion match {
    case s if s.startsWith("2.10.") => "com.typesafe.akka" %% "akka-actor" % "2.3.16"
    case _                          => "com.typesafe.akka" %% "akka-actor" % akkaVersion
  }) % "provided"

def scrooge(scalaVersion: String) = {
  val scroogeBase = "com.twitter" %% "scrooge-serializer"
  scalaVersion match {
    case s if s.startsWith("2.10.") => scroogeBase % "4.7.0" // the last 2.10 version
    case _                          => scroogeBase % scroogeVersion
  }
}

lazy val chillAkka = module("akka")
  .settings(
    resolvers += Resolver.typesafeRepo("releases"),
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.4.0",
      scalaVersion(sv => akka(sv)).value
    )
  )
  .dependsOn(chill % "test->test;compile->compile")

lazy val chillBijection = module("bijection")
  .settings(
    libraryDependencies ++= Seq(
      "com.twitter" %% "bijection-core" % bijectionVersion
    )
  )
  .dependsOn(chill % "test->test;compile->compile")

// This can only have java deps!
lazy val chillJava = module("java").settings(
  crossPaths := false,
  autoScalaLibrary := false
)

// This can only have java deps!
lazy val chillStorm = module("storm")
  .settings(
    crossPaths := false,
    autoScalaLibrary := false,
    libraryDependencies += "org.apache.storm" % "storm-core" % "1.0.6" % "provided"
  )
  .dependsOn(chillJava)

// This can only have java deps!
lazy val chillHadoop = module("hadoop")
  .settings(
    crossPaths := false,
    autoScalaLibrary := false,
    libraryDependencies ++= Seq(
      "org.apache.hadoop" % "hadoop-core" % "1.2.1" % "provided",
      "org.slf4j" % "slf4j-api" % "1.7.29",
      "org.slf4j" % "slf4j-log4j12" % "1.7.29" % "provided"
    )
  )
  .dependsOn(chillJava)

// This can only have java deps!
lazy val chillThrift = module("thrift").settings(
  crossPaths := false,
  autoScalaLibrary := false,
  libraryDependencies ++= Seq(
    "org.apache.thrift" % "libthrift" % "0.12.0" % "provided"
  )
)

lazy val chillScrooge = module("scrooge")
  .settings(
    libraryDependencies ++= Seq(
      ("org.apache.thrift" % "libthrift" % "0.12.0").exclude("junit", "junit"),
      scalaVersion(sv => scrooge(sv)).value
    )
  )
  .dependsOn(chill % "test->test;compile->compile")

// This can only have java deps!
lazy val chillProtobuf = module("protobuf")
  .settings(
    crossPaths := false,
    autoScalaLibrary := false,
    libraryDependencies ++= Seq(
      "com.google.protobuf" % "protobuf-java" % "2.3.0" % "provided"
    )
  )
  .dependsOn(chillJava)

lazy val chillAvro = module("avro")
  .settings(
    libraryDependencies ++= Seq(
      "com.twitter" %% "bijection-avro" % bijectionVersion,
      "junit" % "junit" % "4.12" % "test"
    )
  )
  .dependsOn(chill, chillJava, chillBijection)

lazy val chillAlgebird = module("algebird")
  .settings(
    libraryDependencies ++= Seq(
      "com.twitter" %% "algebird-core" % algebirdVersion
    )
  )
  .dependsOn(chill)
