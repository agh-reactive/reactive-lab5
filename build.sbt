name := """reactive-lab5"""

version := "1.3"

scalaVersion := "2.13.6"
val akkaVersion = "2.6.16"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.6",
  "org.scalatest" %% "scalatest" % "3.2.9" % "test"
)
