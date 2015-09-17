name := "search-maevn-org-scala-sdk"

version := "1.0.0-SNAPSHOT"

organization := "org.jmotor.tools"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "dom4j" % "dom4j" % "1.6.1",
  "jaxen" % "jaxen" % "1.1.6",
  "com.typesafe" % "config" % "1.3.0",
  "com.ning" % "async-http-client" % "1.9.22",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.2",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.6.1",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

Formatting.formatSettings