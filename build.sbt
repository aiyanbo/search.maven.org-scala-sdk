import Dependencies.Versions
import org.jmotor.sbt.plugin.ComponentSorter
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

name := "search-maven-org-scala-sdk"

organization := "org.jmotor.tools"

scalaVersion := Versions.scala213

crossScalaVersions := Seq(Versions.scala211, Versions.scala212, scalaVersion.value)

dependencyUpgradeComponentSorter := ComponentSorter.ByAlphabetically

releasePublishArtifactsAction := PgpKeys.publishSigned.value

releaseCrossBuild := true

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  pushChanges
)
