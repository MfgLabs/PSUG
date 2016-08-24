name := "psugdemo-gatling"

version := "1.0"

scalaVersion := "2.11.8"

lazy val gatlingVersion = "2.2.2"

enablePlugins(GatlingPlugin)

libraryDependencies ++= Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "test",
  "io.gatling"            % "gatling-test-framework"    % gatlingVersion % "test"
)

scalacOptions := Seq(
  "-encoding",
  "UTF-8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions",
  "-language:postfixOps"
 )
