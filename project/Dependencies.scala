import sbt.Keys.libraryDependencies
import sbt.{Def, _}

object Dependencies extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Def.Setting[_]] = Seq(libraryDependencies ++= dependencies)

  object Versions {
    val asyncHttpClient = "2.12.3"
    val jacksonModuleScala = "2.13.4"
    val okhttp = "5.0.0-alpha.10"
    val scala = "2.13.9"
    val scala211 = "2.11.12"
    val scala212 = "2.12.8"
    val scalatest = "3.2.14"
  }

  object Compiles {
    val asyncHttpClient = "org.asynchttpclient" % "async-http-client" % Versions.asyncHttpClient
    val jackson = "com.fasterxml.jackson.module" %% "jackson-module-scala" % Versions.jacksonModuleScala
    val okhttp = "com.squareup.okhttp3" % "okhttp" % Versions.okhttp
  }

  object Tests {
    val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % Versions.scalatest % Test
  }

  import Compiles._

  lazy val dependencies: Seq[ModuleID] = Seq(okhttp, jackson, Tests.scalaTest)

}
