package org.jmotor.tools

import org.asynchttpclient.{AsyncHttpClient, Response}
import org.jmotor.tools.dto.{Artifact, MavenSearchRequest}
import org.jmotor.tools.http.AsyncHttpClientConversions._

import scala.concurrent.{ExecutionContext, Future}

/**
 * Component: Description: Date: 15/9/18
 *
 * @author
 *   Andy.Ai
 */
class MavenSearchClient(path: String, httpClient: AsyncHttpClient) {

  def search(request: MavenSearchRequest)(implicit executor: ExecutionContext): Future[Seq[Artifact]] =
    httpClient.prepareGet(s"$path?${request.toParameter}").toFuture.map(unpacking)

  def selectAll(groupId: String, artifactId: String)(implicit executor: ExecutionContext): Future[Seq[Artifact]] = {
    val totalRequest = MavenSearchRequest(Some(groupId), Some(artifactId), None, core = "ga")
    httpClient.prepareGet(s"$path?${totalRequest.toParameter}").toFuture.flatMap {
      case response if response.getStatusCode == 200 =>
        val rows = 50
        val count = (for (m <- """"versionCount": ?(\d+),""".r findFirstMatchIn response.getResponseBody)
          yield m group 1).getOrElse("0").toInt
        if (count > 0) {
          Future
            .sequence((0 to pages(count, rows)).map { index =>
              val request = MavenSearchRequest(Some(groupId), Some(artifactId), None, rows = rows, start = index * rows)
              httpClient.prepareGet(s"$path?${request.toParameter}").toFuture
            })
            .map(responses => responses.flatMap(unpacking))
        } else {
          Future.successful(Seq.empty[Artifact])
        }
      case _ => Future.successful(Seq.empty[Artifact])
    }
  }

  def latestVersion(groupId: String, artifactId: String)(implicit
    executor: ExecutionContext
  ): Future[Option[String]] = {
    val request = MavenSearchRequest(Some(groupId), Some(artifactId), None, core = "ga", rows = 1)
    httpClient.prepareGet(s"$path?${request.toParameter}").toFuture.map {
      case response if response.getStatusCode == 200 =>
        for (m <- """"latestVersion": ?"(.*)"""".r findFirstMatchIn response.getResponseBody) yield m group 1
      case _ => None
    }
  }

  private def pages(count: Int, rows: Int): Int =
    if (count % rows == 0) {
      count / rows
    } else {
      (count / rows) + 1
    }

  private def unpacking(response: Response): Seq[Artifact] =
    if (response.getStatusCode == 200) {
      val docs = for (m <- """"docs" ?: ?(.*)""".r findFirstMatchIn response.getResponseBody) yield m group 1
      Jackson.mapper.readValue[Seq[Artifact]](docs.getOrElse("[]"), Jackson.tr)
    } else {
      Seq.empty[Artifact]
    }

}

object MavenSearchClient {

  lazy val MAX_CONNECTIONS: Int = 50

  def apply(): MavenSearchClient = {
    import org.asynchttpclient.Dsl._
    MavenSearchClient(asyncHttpClient(config().setMaxConnectionsPerHost(MAX_CONNECTIONS)))
  }

  def apply(httpClient: AsyncHttpClient): MavenSearchClient =
    MavenSearchClient("https://search.maven.org/solrsearch/select", httpClient)

  def apply(path: String, httpClient: AsyncHttpClient): MavenSearchClient =
    new MavenSearchClient(path, httpClient)

}
