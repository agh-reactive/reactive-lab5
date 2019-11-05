name := """reactive-lab5"""

version := "1.2"

scalaVersion := "2.13.1"
   
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.26",
  "com.typesafe.akka" %% "akka-remote" % "2.5.26",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.26" % "test",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test")

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

