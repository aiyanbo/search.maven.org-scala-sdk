package org.jmotor.tools

import org.jmotor.tools.dto.MavenSearchRequest
import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class MavenSearchClientSpec extends AnyFunSuite {
  private[this] val client = MavenSearchClient()
  private val orgId = "org.scala-lang"
  private val artId = "scala-library"

  test("latestVersion") {
    val future = client.latestVersion(orgId, artId)
    val result = Await.result(future, 10.seconds)
    assert(result.nonEmpty)
  }

  test("To parameters") {
    val request = MavenSearchRequest(Some(orgId), Some(artId), None)
    assert("""q=g:"org.scala-lang" AND a:"scala-library"&core=gav&rows=20&wt=json&start=0""" == request.toParameter)
  }

  test("Select All") {
    val future = client.selectAll(orgId, artId)
    val results = Await.result(future, 60.seconds)
    results.foreach { artifact ⇒
      assert(artifact.g == orgId)
      assert(artifact.a == artId)
    }
  }

  test("Search") {
    val request = MavenSearchRequest(Some(orgId), Some(artId), None)
    val future = client.search(request)
    val results = Await.result(future, 60.seconds)
    results.foreach { artifact ⇒
      assert(artifact.g == orgId)
      assert(artifact.a == artId)
    }
  }

  test("okhttp") {
    val akkaFuture = client.search(MavenSearchRequest("com.typesafe.akka", "akka-stream_2.13"))
    val zioFuture = client.search(MavenSearchRequest("dev.zio", "zio-streams_2.13"))

    val future = for {
      akka <- akkaFuture
      zio <- zioFuture
    } yield (akka, zio)

    val (akka, zio) = Await.result(future, 60.seconds)
    assert(akka.nonEmpty)
    assert(zio.nonEmpty)

  }

}
