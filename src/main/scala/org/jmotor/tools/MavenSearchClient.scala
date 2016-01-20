package org.jmotor.tools

import com.fasterxml.jackson.databind.{ DeserializationFeature, ObjectMapper }
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.ning.http.client.{ AsyncCompletionHandler, AsyncHttpClient, Response }
import org.jmotor.tools.dto.{ Artifact, MavenSearchRequest }

import scala.concurrent.{ ExecutionContext, Future, Promise }

/**
 * Component:
 * Description:
 * Date: 15/9/18
 *
 * @author Andy.Ai
 */
object MavenSearchClient {
  private val client: AsyncHttpClient = new AsyncHttpClient()
  private val rootPath: String = "http://search.maven.org/solrsearch/select"
  private val mapper = new ObjectMapper() with ScalaObjectMapper

  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  def search(request: MavenSearchRequest)(implicit executor: ExecutionContext): Future[List[Artifact]] = {
    execute(client.prepareGet(s"$rootPath?${request.toParameter}")).map(unpacking)
  }

  def selectAll(groupId: String, artifactId: String)(implicit executor: ExecutionContext): Future[List[Artifact]] = {
    val totalRequest = MavenSearchRequest(Some(groupId), Some(artifactId), None, core = "ga")
    execute(client.prepareGet(s"$rootPath?${totalRequest.toParameter}")).flatMap {
      case response if response.getStatusCode == 200 ⇒
        val rows = 50
        val count = (for (m ← """"versionCount": ?(\d+),""".r findFirstMatchIn response.getResponseBody) yield m group 1).getOrElse("0").toInt
        if (count > 0) {
          Future.sequence((0 to pages(count, rows)).map(index ⇒ {
            val request = MavenSearchRequest(Some(groupId), Some(artifactId), None, rows = rows, start = index * rows)
            execute(client.prepareGet(s"$rootPath?${request.toParameter}"))
          })).map(responses ⇒ responses.flatMap(unpacking).toList)
        } else {
          Future.successful(List.empty[Artifact])
        }
      case _ ⇒ Future.successful(List.empty[Artifact])
    }
  }

  def latestVersion(groupId: String, artifactId: String)(implicit executor: ExecutionContext): Future[Option[String]] = {
    val request = MavenSearchRequest(Some(groupId), Some(artifactId), None, core = "ga", rows = 1)
    execute(client.prepareGet(s"$rootPath?${request.toParameter}")).map {
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

  private def unpacking(response: Response): List[Artifact] = {
    if (response.getStatusCode == 200) {
      val docs = for (m ← """"docs" ?: ?(\[.*\])""".r findFirstMatchIn response.getResponseBody) yield m group 1
      mapper.readValue[List[Artifact]](docs.getOrElse("[]"))
    } else {
      List.empty[Artifact]
    }
  }

  private def execute(request: AsyncHttpClient#BoundRequestBuilder): Future[Response] = {
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
