package org.jmotor.tools

import java.util.concurrent.{ Future, TimeUnit }

import com.fasterxml.jackson.databind.{ DeserializationFeature, ObjectMapper }
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.ning.http.client.{ AsyncHttpClient, Response }
import org.jmotor.tools.dto.{ MavenSearchRequest, Artifact }

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
      val docs = for (m ← """.*"docs" ?: ?(\[.*\]).*""".r findFirstMatchIn response.getResponseBody) yield m group 1
      mapper.readValue[List[Artifact]](docs.getOrElse("[]"))
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
}
