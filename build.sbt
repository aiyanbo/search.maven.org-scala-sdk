name := "search-maevn-org-scala-sdk"

version := "1.0.0-SNAPSHOT"

organization := "org.jmotor.tools"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.ning" % "async-http-client" % "1.9.32",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.3",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.6.3",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

Formatting.formatSettings