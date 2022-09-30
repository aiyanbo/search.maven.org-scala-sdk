package org.jmotor.tools

import okhttp3.{Dispatcher, OkHttpClient, Response}
import org.jmotor.tools.Conversions._
import org.jmotor.tools.dto.{Artifact, MavenSearchRequest}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Component: Description: Date: 15/9/18
 *
 * @author
 * Andy.Ai
 */
class MavenSearchClient(path: String, client: OkHttpClient) {

  def search(request: MavenSearchRequest)(implicit executor: ExecutionContext): Future[Seq[Artifact]] = {
    client.newCall(request.toHttpRequest(path)).toFuture.map(unpacking)
  }

  def selectAll(groupId: String, artifactId: String)(implicit executor: ExecutionContext): Future[Seq[Artifact]] = {
    val totalRequest = MavenSearchRequest(Some(groupId), Some(artifactId), None, core = "ga")
    client.newCall(totalRequest.toHttpRequest(path)).toFuture.flatMap {
      case response if response.isSuccessful =>
        val rows = 50
        val count = (for (m <- """"versionCount": ?(\d+),""".r findFirstMatchIn response.body().string())
          yield m group 1).getOrElse("0").toInt
        if (count > 0) {
          Future
            .sequence((0 to pages(count, rows)).map { index =>
              val request = MavenSearchRequest(Some(groupId), Some(artifactId), None, rows = rows, start = index * rows)
              client.newCall(request.toHttpRequest(path)).toFuture
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
    client.newCall(request.toHttpRequest(path)).toFuture.map {
      case response if response.isSuccessful =>
        for (m <- """"latestVersion": ?"(.*)"""".r findFirstMatchIn response.body().string()) yield m group 1
      case _ => None
    }
  }

  private def pages(count: Int, rows: Int): Int =
    if (count % rows == 0) {
      count / rows
    } else {
      (count / rows) + 1
    }

  private def unpacking(response: Response): Seq[Artifact] = {
    if (response.isSuccessful) {
      val docs = for (m <- """"docs" ?: ?(.*)""".r findFirstMatchIn response.body().string()) yield m group 1
      Jackson.mapper.readValue[Seq[Artifact]](docs.getOrElse("[]"), Jackson.tr)
    } else {
      Seq.empty[Artifact]
    }
  }

}

object MavenSearchClient {

  def apply(): MavenSearchClient = {
    val dispatcher = new Dispatcher()
    val concurrent = 1000
    dispatcher.setMaxRequests(concurrent)
    dispatcher.setMaxRequestsPerHost(concurrent)
    val client = new OkHttpClient.Builder().dispatcher(dispatcher).build()
    MavenSearchClient(client)
  }

  def apply(client: OkHttpClient): MavenSearchClient = {
    MavenSearchClient("https://search.maven.org/solrsearch/select", client)
  }

  def apply(path: String, client: OkHttpClient): MavenSearchClient = {
    new MavenSearchClient(path, client)
  }

}
