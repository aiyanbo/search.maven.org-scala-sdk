name := "search-maven-org-scala-sdk"

version := "1.0.0-SNAPSHOT"

organization := "org.jmotor.tools"

scalaVersion := "2.12.4"

crossScalaVersions := Seq("2.11.7", scalaVersion.value)

libraryDependencies ++= Seq(
  "org.asynchttpclient" % "async-http-client" % "2.3.0",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.4",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.4",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

Formatting.formatSettings
