import sbt.Keys.libraryDependencies
import sbt.{ Def, _ }

object Dependencies extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Def.Setting[_]] = Seq(libraryDependencies ++= dependencies)

  object Versions {
    val asyncHttpClient = "2.10.1"
    val jacksonModuleScala = "2.9.9"
    val scala211 = "2.11.12"
    val scala212 = "2.12.8"
    val scala213 = "2.13.0"
    val scalatest = "3.0.8"
  }

  object Compiles {
    val asyncHttpClient = "org.asynchttpclient" % "async-http-client" % Versions.asyncHttpClient
    val jackson = "com.fasterxml.jackson.module" %% "jackson-module-scala" % Versions.jacksonModuleScala
  }

  object Tests {
    val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % Versions.scalatest % Test
  }

  import Compiles._

  lazy val dependencies: Seq[ModuleID] = Seq(asyncHttpClient, jackson, Tests.scalaTest)

}
