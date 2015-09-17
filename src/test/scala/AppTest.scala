import java.util.concurrent.Future

import com.ning.http.client.{ AsyncHttpClient, Response }
import org.jmotor.tools.MavenSearchRequest
import org.scalatest._

class AppTest extends FunSuite {
  test("List stats") {
    val rootPath: String = "http://search.maven.org/solrsearch/select"
    val asyncHttpClient: AsyncHttpClient = new AsyncHttpClient()
    val request = MavenSearchRequest(Some("org.scala-lang"), Some("scala-reflect"), None)
    val f: Future[Response] = asyncHttpClient.prepareGet(s"$rootPath?${request.toParameter}").execute()
    val r = f.get()
    if (r.getStatusCode == 200) {
      println(r.getResponseBody)
    }
  }

  test("To parameters") {
    val request = MavenSearchRequest(Some("org.scala-lang"), Some("scala-reflect"), None)
    println(request.toParameter)
  }
}
