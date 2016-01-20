import java.util.concurrent.Future

import com.fasterxml.jackson.databind.{ DeserializationFeature, ObjectMapper }
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.ning.http.client.{ AsyncHttpClient, Response }
import org.jmotor.tools.MavenSearchClient
import org.jmotor.tools.dto.{ MavenSearchRequest, Artifact }
import org.scalatest._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import scala.util.{ Failure, Success }

class AppTest extends FunSuite {
  test("JSON") {
    val start = System.currentTimeMillis()
    val request = MavenSearchRequest(Some("org.scala-lang"), Some("scala-library"), None, wt = "json", core = "gav", rows = 50)
    val s = execute(request)
    val docsExpr = """"docs" ?: ?(\[.*\])""".r
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    val docs = for (m ← docsExpr findFirstMatchIn s) yield m group 1
    val results = mapper.readValue[List[Artifact]](docs.getOrElse("[]"))
    println(s"Cost: ${System.currentTimeMillis() - start}, size: ${results.size}")
    println(s)
  }

  test("latestVersion") {
    val future = MavenSearchClient.latestVersion("org.scala-lang", "scala-library")
    val result = Await.result(future, 10.seconds)
    println(result.get)
  }

  test("To parameters") {
    val request = MavenSearchRequest(Some("org.scala-lang"), Some("scala-library"), None)
    println(request.toParameter)
  }

  test("Select All") {
    val future = MavenSearchClient.selectAll("org.scala-lang", "scala-library")
    val result = Await.result(future, 60.seconds)
    println(result)
  }

  test("Search") {
    val request = new MavenSearchRequest(Some("org.scala-lang"), Some("scala-library"), None)
    val future = MavenSearchClient.search(request)
    val result = Await.result(future, 60.seconds)
    println(result)
  }

  def execute(request: MavenSearchRequest): String = {
    val rootPath: String = "http://search.maven.org/solrsearch/select"
    val asyncHttpClient: AsyncHttpClient = new AsyncHttpClient()
    val f: Future[Response] = asyncHttpClient.prepareGet(s"$rootPath?${request.toParameter}").execute()
    f.get().getResponseBody
  }
}
