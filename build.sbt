import org.scoverage.coveralls.Imports.CoverallsKeys._

lazy val precepteVersion = "0.4.2"

lazy val specVersion = "2.4.17"

lazy val doobieVersion = "0.3.0"

lazy val commons =
Seq(
  logLevel in update := Level.Warn,
  organization := "com.mfglabs",
  name := """psug-demo""",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.11.8",
  sources in (Compile,doc) := Seq.empty,
  publishArtifact in (Compile, packageDoc) := false,
  scalacOptions ++= Seq(
    "-Yrangepos",
    "-Xlint",
    "-deprecation",
      // "-Xfatal-warnings",
      "-feature",
      "-encoding", "UTF-8",
      "-unchecked",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",        // N.B. doesn't work well with the ??? hole
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Xfuture",
      "-Ywarn-unused",
      "-Ywarn-unused-import",
      "-Ydelambdafy:method",
      "-Ybackend:GenBCode",
      "-target:jvm-1.8"
      ),
  scalacOptions in (Compile, console) := Seq(),
  javaOptions ++= Seq("-Xmx2048M",  "-Xss1M",  "-XX:+CMSClassUnloadingEnabled",  "-XX:MaxPermSize=512m", "-XX:ReservedCodeCacheSize=64m", "-Dfile.encoding=UTF-8"),
  logLevel in update := Level.Warn,
  // updateOptions := updateOptions.value.withCachedResolution(true),
  resolvers ++= Seq(
    Resolver.bintrayRepo("mfglabs", "maven"),
    "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.8.0"),
  coverageExcludedPackages := """com\.mfglabs\.BuildInfo;router\..*;controllers\.javascript\..*;com\.mfglabs\.controllers\.javascript\..*;com\.mfglabs\.controllers\.Reverse.*;controllers\.Reverse.*"""
  )

lazy val root =
(project in file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(BuildInfoPlugin)
  .settings(commons:_*)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := organization.value,
    buildInfoOptions := Seq(BuildInfoOption.BuildTime),
    scalariformSettings,
    libraryDependencies ++= Seq(
      cache, filters,
      "io.github.jto"   %% "validation-playjson"    % "2.0.1",
      "com.mfglabs"     %% "precepte-core-scalaz"   % precepteVersion,
      "com.mfglabs"     %% "precepte-logback"       % precepteVersion,
      "com.mfglabs"     %% "precepte-influx"        % precepteVersion,
      "com.mfglabs"     %% "precepte-play"          % precepteVersion,
      "com.chuusai" %% "shapeless" % "2.3.1",
      "org.scalanlp" %% "breeze" % "0.12",
      "org.scalanlp" %% "breeze-natives" % "0.12",
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-contrib-postgresql" % doobieVersion,
      "org.flywaydb" %% "flyway-play" % "3.0.1",
      "org.postgresql" % "postgresql" % "9.4.1209",
      "org.influxdb" % "influxdb-java" % "2.2",
      "org.http4s" %% "http4s-blaze-client" % "0.14.2a",
      "org.scalatest" % "scalatest_2.11" % "3.0.0" % "test"
      ),
    routesImport ++= Seq(
      "com.mfglabs.models._",
      "com.mfglabs.controllers.RouteBindings._"),
    javaOptions in Test += "-Dconfig.file=conf/application.test.conf"
    )

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
