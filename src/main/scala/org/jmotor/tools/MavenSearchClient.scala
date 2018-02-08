package org.jmotor.tools

import com.fasterxml.jackson.databind.{ DeserializationFeature, ObjectMapper }
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import org.asynchttpclient.{ AsyncCompletionHandler, AsyncHttpClient, BoundRequestBuilder, Response }
import org.jmotor.tools.dto.{ Artifact, MavenSearchRequest }

import scala.concurrent.{ ExecutionContext, Future, Promise }

/**
 * Component:
 * Description:
 * Date: 15/9/18
 *
 * @author Andy.Ai
 */
class MavenSearchClient(path: String, httpClient: AsyncHttpClient) {
  private val mapper = new ObjectMapper() with ScalaObjectMapper

  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  def search(request: MavenSearchRequest)(implicit executor: ExecutionContext): Future[Seq[Artifact]] = {
    execute(httpClient.prepareGet(s"$path?${request.toParameter}")).map(unpacking)
  }

  def selectAll(groupId: String, artifactId: String)(implicit executor: ExecutionContext): Future[Seq[Artifact]] = {
    val totalRequest = MavenSearchRequest(Some(groupId), Some(artifactId), None, core = "ga")
    execute(httpClient.prepareGet(s"$path?${totalRequest.toParameter}")).flatMap {
      case response if response.getStatusCode == 200 ⇒
        val rows = 50
        val count = (for (m ← """"versionCount": ?(\d+),""".r findFirstMatchIn response.getResponseBody) yield m group 1).getOrElse("0").toInt
        if (count > 0) {
          Future.sequence((0 to pages(count, rows)).map(index ⇒ {
            val request = MavenSearchRequest(Some(groupId), Some(artifactId), None, rows = rows, start = index * rows)
            execute(httpClient.prepareGet(s"$path?${request.toParameter}"))
          })).map(responses ⇒ responses.flatMap(unpacking))
        } else {
          Future.successful(Seq.empty[Artifact])
        }
      case _ ⇒ Future.successful(Seq.empty[Artifact])
    }
  }

  def latestVersion(groupId: String, artifactId: String)(implicit executor: ExecutionContext): Future[Option[String]] = {
    val request = MavenSearchRequest(Some(groupId), Some(artifactId), None, core = "ga", rows = 1)
    execute(httpClient.prepareGet(s"$path?${request.toParameter}")).map {
      case response if response.getStatusCode == 200 ⇒
        for (m ← """"latestVersion": ?"([\d|\w|.|-]*)"""".r findFirstMatchIn response.getResponseBody) yield m group 1
      case _ ⇒ None
    }
  }

  private def pages(count: Int, rows: Int): Int = {
    if (count % rows == 0) {
      count / rows
    } else {
      (count / rows) + 1
    }
  }

  private def unpacking(response: Response): Seq[Artifact] = {
    if (response.getStatusCode == 200) {
      val docs = for (m ← """"docs" ?: ?(\[.*\])""".r findFirstMatchIn response.getResponseBody) yield m group 1
      mapper.readValue[Seq[Artifact]](docs.getOrElse("[]"))
    } else {
      Seq.empty[Artifact]
    }
  }

  private def execute(request: BoundRequestBuilder): Future[Response] = {
    val result = Promise[Response]
    request.execute(new AsyncCompletionHandler[Response]() {
      override def onCompleted(response: Response): Response = {
        result.success(response)
        response
      }

      override def onThrowable(t: Throwable): Unit = {
        result.failure(t)
      }
    })
    result.future
  }
}

object MavenSearchClient {

  lazy val MAX_CONNECTIONS: Int = 50

  def apply(): MavenSearchClient = {
    import org.asynchttpclient.Dsl._
    MavenSearchClient(asyncHttpClient(config().setMaxConnectionsPerHost(MAX_CONNECTIONS)))
  }

  def apply(httpClient: AsyncHttpClient): MavenSearchClient = {
    MavenSearchClient("https://search.maven.org/solrsearch/select", httpClient)
  }

  def apply(path: String, httpClient: AsyncHttpClient): MavenSearchClient = {
    new MavenSearchClient(path, httpClient)
  }

}
