package org.jmotor.tools.http

import org.asynchttpclient.{AsyncCompletionHandler, AsyncHttpClient, BoundRequestBuilder, Response}

import scala.concurrent.{ExecutionContext, Future, Promise}

/**
 * Component: Description: Date: 2018/2/8
 *
 * @author
 *   AI
 */
object AsyncHttpClientConversions {

  implicit class BoundRequestBuilderWrapper(request: BoundRequestBuilder) {

    implicit def toFuture: Future[Response] = {
      val result = Promise[Response]
      request.execute(new AsyncCompletionHandler[Response]() {
        override def onCompleted(response: Response): Response = {
          result.success(response)
          response
        }

        override def onThrowable(t: Throwable): Unit =
          result.failure(t)
      })
      result.future
    }

  }

}
