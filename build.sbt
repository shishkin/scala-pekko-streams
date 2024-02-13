name := "scala-pekko-streams"

version := "1.0"

scalaVersion := "2.13.12"

lazy val pekkoVersion = "1.0.2"

fork := true

libraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-actor-typed" % pekkoVersion,
  "org.apache.pekko" %% "pekko-stream" % pekkoVersion,
  "org.apache.pekko" %% "pekko-connectors-file" % pekkoVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.13"
)
