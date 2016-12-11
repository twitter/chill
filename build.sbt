import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings

val kryoVersion = "4.0.0"
val bijectionVersion = "0.9.4"
val algebirdVersion = "0.12.0"
val akkaVersion = "2.3.6"

val sharedSettings = mimaDefaultSettings ++ scalariformSettings ++ Seq(
  organization := "com.twitter",
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.1"),
  scalacOptions ++= Seq("-unchecked", "-deprecation"),
  ScalariformKeys.preferences := formattingPreferences,

  // Twitter Hadoop needs this, sorry 1.7 fans
  javacOptions ++= Seq("-target", "1.6", "-source", "1.6", "-Xlint:-options"),
  javacOptions in doc := Seq("-source", "1.6"),

  resolvers ++= Seq(
    Opts.resolver.sonatypeSnapshots,
    Opts.resolver.sonatypeReleases
  ),
  libraryDependencies ++= Seq(
    "org.scalacheck" %% "scalacheck" % "1.11.6" % "test",
    "org.scalatest" %% "scalatest" % "3.0.0" % "test",
    "com.esotericsoftware" % "kryo-shaded" % kryoVersion
  ),

  parallelExecution in Test := true,

  // Publishing options:
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { x => false },

  publishTo := Some(
      if (version.value.trim.toUpperCase.endsWith("SNAPSHOT"))
        Opts.resolver.sonatypeSnapshots
      else
        Opts.resolver.sonatypeStaging
    ),
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
 ).enablePlugins(CrossPerProjectPlugin)
  .settings(noPublishSettings)
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
 FormattingPreferences().
   setPreference(AlignParameters, false).
   setPreference(PreserveSpaceBeforeArguments, true)
}

lazy val noPublishSettings = Seq(
    publish := (),
    publishLocal := (),
    test := (),
    publishArtifact := false
  )

/**
  * This returns the youngest jar we released that is compatible
  * with the current.
  */
val unreleasedModules = Set[String]("akka")
val javaOnly = Set[String]("storm", "java", "hadoop", "thrift", "protobuf")
val binaryCompatVersion = "0.8.0"

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
  Project(id = id, base = file(id), settings = sharedSettings ++ Seq(
    Keys.name := id,
    mimaPreviousArtifacts := youngestForwardCompatible(name).toSet,
    mimaBinaryIssueFilters ++= ignoredABIProblems,
    // Disable cross publishing for java artifacts
    publishArtifact :=
      (if (javaOnly.contains(name) && scalaVersion.value.startsWith("2.11")) false else true)
  ))
}

// We usually do the pattern of having a core module, but we don't want to cause
// pain for legacy deploys. With this, they can stay the same.
lazy val chill = Project(
  id = "chill",
  base = file("chill-scala"),
  settings = sharedSettings
).settings(
  name := "chill",
  mimaPreviousArtifacts := Set("com.twitter" %% "chill" % binaryCompatVersion)
).dependsOn(chillJava)

lazy val chillAkka = module("akka").settings(
  resolvers += Resolver.typesafeRepo("releases"),
  crossScalaVersions := crossScalaVersions.value.filterNot(_.startsWith("2.12")),
  libraryDependencies ++= Seq(
    "com.typesafe" % "config" % "1.2.1",
    "com.typesafe.akka" %% "akka-actor" % akkaVersion % "provided"
  )
).dependsOn(chill % "test->test;compile->compile")

lazy val chillBijection = module("bijection").settings(
  libraryDependencies ++= Seq(
    "com.twitter" %% "bijection-core" % bijectionVersion
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
  libraryDependencies += "org.apache.storm" % "storm-core" % "1.0.2" % "provided"
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

// This can only have java deps!
lazy val chillThrift = module("thrift").settings(
  crossPaths := false,
  autoScalaLibrary := false,
  libraryDependencies ++= Seq(
    "org.apache.thrift" % "libthrift" % "0.6.1" % "provided"
  )
)

lazy val chillScrooge = module("scrooge").settings(
  crossScalaVersions := crossScalaVersions.value.filterNot(_.startsWith("2.12")),
  libraryDependencies ++= Seq(
    "org.apache.thrift" % "libthrift" % "0.6.1" exclude("junit", "junit"),
    "com.twitter" %% "scrooge-serializer" % "3.20.0"
  )
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
    "com.twitter" %% "bijection-avro" % bijectionVersion,
    "junit" % "junit" % "4.5" % "test"
  )
).dependsOn(chill,chillJava, chillBijection)

lazy val chillAlgebird = module("algebird").settings(
  crossScalaVersions := crossScalaVersions.value.filterNot(_.startsWith("2.12")),
  libraryDependencies ++= Seq(
    "com.twitter" %% "algebird-core" % algebirdVersion
  )
).dependsOn(chill)
