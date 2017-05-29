name := "Akka http"

description := "akka http"

scalaVersion := "2.11.0"

val akkaVersion = "2.5.2"

val sprayVersion = "1.3.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.0.6",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.6",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "io.spray" %% "spray-json" % sprayVersion,
  "org.scalatest" % "scalatest_2.11" % "2.2.2" % "test"
)
