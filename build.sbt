import Dependencies.Versions
import org.jmotor.sbt.plugin.ComponentSorter

name := "search-maven-org-scala-sdk"

organization := "org.jmotor.tools"

scalaVersion := Versions.scala

crossScalaVersions := Seq(Versions.scala211, Versions.scala212, scalaVersion.value)

dependencyUpgradeComponentSorter := ComponentSorter.ByAlphabetically

dependencyUpgradeModuleNames := Map(
  "scala-library" -> "scala"
)

inThisBuild(List(
  homepage := Some(url("https://github.com/aiyanbo/search.maven.org-scala-sdk")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "aiyanbo",
      "Andy Ai",
      "yanbo.ai@gmail.com",
      url("https://aiyanbo.github.io/")
    )
  )
))

sonatypeProfileName := "org.jmotor"
