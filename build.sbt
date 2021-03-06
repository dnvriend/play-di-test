
name := "play-di-test"

version := "1.0.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= {
  val AkkaVersion = "2.4.12"
  Seq(
    ws,
    "com.typesafe.play" %% "play-slick" % "2.0.2",
    "com.typesafe.play" %% "play-slick-evolutions" % "2.0.2",
    "com.h2database" % "h2" % "1.4.193",
    "com.typesafe.akka" %% "akka-cluster" % AkkaVersion,
    "com.typesafe.akka" %% "akka-cluster-metrics" % AkkaVersion,
    "com.typesafe.akka" %% "akka-cluster-sharding" % AkkaVersion,
    "com.typesafe.akka" %% "akka-cluster-tools" % AkkaVersion,
    "com.github.nscala-time" %% "nscala-time" % "2.14.0",
    "com.pauldijou" %% "jwt-core" % "0.9.2",
    "com.pauldijou" %% "jwt-play-json" % "0.9.2",
    "org.scalaz" %% "scalaz-core" % "7.2.7",
    "org.typelevel" %% "scalaz-outlaws" % "0.2",
    "org.typelevel" %% "scalaz-scalatest" % "1.1.0" % Test,
    "com.github.dnvriend" %% "akka-persistence-inmemory" % "1.3.15" % Test,
    "org.mockito" % "mockito-core" % "2.2.16" % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0-M1" % Test
  )
}

licenses += ("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php"))

fork in Test := true

parallelExecution in Test := false

import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

ScalariformKeys.preferences in Compile := formattingPreferences

ScalariformKeys.preferences in Test := formattingPreferences

def formattingPreferences = {
  import scalariform.formatter.preferences._
  FormattingPreferences()
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
    .setPreference(DoubleIndentClassDeclaration, true)
}

import de.heikoseeberger.sbtheader._
import de.heikoseeberger.sbtheader.HeaderKey._
import de.heikoseeberger.sbtheader.license.Apache2_0

headers := headers.value ++ Map(
  "scala" -> Apache2_0("2016", "Dennis Vriend"),
  "conf" -> Apache2_0("2016", "Dennis Vriend", "#")
)

// Declares endpoints. The default is Map("web" -> Endpoint("http", 0, Set.empty)).
// The endpoint key is used to form a set of environment variables for your components,
// e.g. for the endpoint key "web" ConductR creates the environment variable WEB_BIND_PORT.
BundleKeys.endpoints := Map(
  "play" -> Endpoint(bindProtocol = "http", bindPort = 0, services = Set(URI("http://:9000/play"))),
  "akka-remote" -> Endpoint("tcp")
)

normalizedName in Bundle := "play-test" // the human readable name for your bundle

BundleKeys.system := "PlayTestSystem" // represents the clustered ActorSystem

BundleKeys.startCommand += "-Dhttp.address=$PLAY_BIND_IP -Dhttp.port=$PLAY_BIND_PORT -Dplay.akka.actor-system=$BUNDLE_SYSTEM"

enablePlugins(AutomateHeaderPlugin, SbtScalariform, ConductrPlugin)