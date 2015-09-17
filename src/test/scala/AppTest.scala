import java.util.concurrent.Future

import com.fasterxml.jackson.databind.{ DeserializationFeature, ObjectMapper }
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.ning.http.client.{ AsyncHttpClient, Response }
import org.jmotor.tools.{ Artifact, MavenSearchRequest }
import org.scalatest._

class AppTest extends FunSuite {
  test("JSON") {
    val request = MavenSearchRequest(Some("org.scala-lang"), Some("scala-reflect"), None, wt = "json")
    val s = execute(request)
    val docsExpr = """.*"docs" ?: ?(\[.*\]).*""".r
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    val docs = for (m ← docsExpr findFirstMatchIn s) yield m group 1
    println(mapper.readValue[List[Artifact]](docs.getOrElse("[]")))
  }

  test("To parameters") {
    val request = MavenSearchRequest(Some("org.scala-lang"), Some("scala-reflect"), None)
    println(request.toParameter)
  }

  def execute(request: MavenSearchRequest): String = {
    val rootPath: String = "http://search.maven.org/solrsearch/select"
    val asyncHttpClient: AsyncHttpClient = new AsyncHttpClient()
    val f: Future[Response] = asyncHttpClient.prepareGet(s"$rootPath?${request.toParameter}").execute()
    f.get().getResponseBody
  }
}
