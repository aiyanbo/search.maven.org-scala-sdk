package org.jmotor.tools

import okhttp3.{Call, Callback, Request, Response}
import org.jmotor.tools.dto.MavenSearchRequest

import java.io.IOException
import scala.concurrent.{Future, Promise}
import scala.language.implicitConversions

object Conversions {

  implicit class OkHttpCallWrapper(call: Call) {

    implicit def toFuture: Future[Response] = {
      val promise = Promise[Response]
      call.enqueue(new Callback {
        override def onFailure(call: Call, e: IOException): Unit = {
          promise.failure(e)
        }

        override def onResponse(call: Call, response: Response): Unit = {
          promise.success(response)
        }
      })
      promise.future
    }
  }

  implicit class MavenSearchRequestWrapper(request: MavenSearchRequest) {

    implicit def toHttpRequest(path: String): Request = {
      new Request.Builder().url(s"$path?${request.toParameter}").build()
    }

  }

}
