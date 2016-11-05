name := "play-di-test"

version := "1.0.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "com.github.dnvriend" %% "akka-persistence-inmemory" % "1.3.14"
)

licenses += ("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php"))


