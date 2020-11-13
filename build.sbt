name := """reactive-lab5"""

version := "1.2"

scalaVersion := "2.13.3"
   
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.6.10",
  "io.netty" % "netty" % "3.10.6.Final", // for deprecated classic remoting
  "com.typesafe.akka" %% "akka-actor-typed" % "2.6.10",
  "com.typesafe.akka" %% "akka-remote" % "2.6.10",
  "com.typesafe.akka" %% "akka-testkit" % "2.6.10" % "test",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test")

