name := """reactive-lab5"""

version := "1.2"

scalaVersion := "2.12.7"
   
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.18",
  "com.typesafe.akka" %% "akka-remote" % "2.5.18",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.18" % "test",
  "org.scalatest" % "scalatest_2.12" % "3.0.0" % "test")

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

