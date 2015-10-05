package org.jmotor.tools

import java.util.concurrent.{ Future, TimeUnit }

import com.fasterxml.jackson.databind.{ DeserializationFeature, ObjectMapper }
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.ning.http.client.{ AsyncHttpClient, Response }
import org.jmotor.tools.dto.{ MavenSearchRequest, Artifact }

import scala.collection.mutable

/**
 * Component:
 * Description:
 * Date: 15/9/18
 *
 * @author Andy.Ai
 */
object MavenSearchClient {
  private val httpClient: AsyncHttpClient = new AsyncHttpClient()
  private val rootPath: String = "http://search.maven.org/solrsearch/select"
  private val mapper = new ObjectMapper() with ScalaObjectMapper

  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  def search(request: MavenSearchRequest)(implicit timeout: Int = 5000): List[Artifact] = {
    val f: Future[Response] = httpClient.prepareGet(s"$rootPath?${request.toParameter}").execute()
    val response = f.get(timeout, TimeUnit.MILLISECONDS)
    if (response.getStatusCode == 200) {
      unpacking(response)
    } else {
      List.empty[Artifact]
    }
  }

  def selectAll(groupId: String, artifactId: String)(implicit timeout: Int = 5000): List[Artifact] = {
    val rows = 50
    val totalRequest = MavenSearchRequest(Some(groupId), Some(artifactId), None, core = "ga")
    val f: Future[Response] = httpClient.prepareGet(s"$rootPath?${totalRequest.toParameter}").execute()
    val response = f.get(timeout, TimeUnit.MILLISECONDS)
    if (response.getStatusCode == 200) {
      val versionCount = for (m ← """"versionCount": ?(\d+),""".r findFirstMatchIn response.getResponseBody) yield m group 1
      val count = versionCount.getOrElse("0").toInt
      if (count > 0) {
        val result: mutable.MutableList[Artifact] = new mutable.MutableList()
        val futures: mutable.MutableList[Future[Response]] = new mutable.MutableList()
        for (i ← 0 to pages(count, rows)) {
          val request = MavenSearchRequest(Some(groupId), Some(artifactId), None, rows = rows, start = i * rows)
          futures += httpClient.prepareGet(s"$rootPath?${request.toParameter}").execute()
        }
        for (f ← futures) {
          result ++= unpacking(f.get(timeout, TimeUnit.MILLISECONDS))
        }
        result.toList
      } else {
        List.empty[Artifact]
      }
    } else {
      List.empty[Artifact]
    }
  }

  def latestVersion(groupId: String, artifactId: String)(implicit timeout: Int = 5000): String = {
    val request = MavenSearchRequest(Some(groupId), Some(artifactId), None, core = "ga", rows = 1)
    val f: Future[Response] = httpClient.prepareGet(s"$rootPath?${request.toParameter}").execute()
    val response = f.get(timeout, TimeUnit.MILLISECONDS)
    if (response.getStatusCode == 200) {
      val latest = for (m ← """"latestVersion": ?"([\d|\w|.|-]*)"""".r findFirstMatchIn response.getResponseBody) yield m group 1
      latest.orNull
    } else {
      null
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
    val docs = for (m ← """"docs" ?: ?(\[.*\])""".r findFirstMatchIn response.getResponseBody) yield m group 1
    mapper.readValue[List[Artifact]](docs.getOrElse("[]"))
  }
}
