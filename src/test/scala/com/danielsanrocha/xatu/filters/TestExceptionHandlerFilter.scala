package com.danielsanrocha.xatu.filters

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

class TestExceptionHandlerFilter() extends SimpleFilter[Request, Response] {
  def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    service(request) rescue { case e: Exception =>
      e.printStackTrace()
      val errorResponse = Response()
      errorResponse.statusCode = 500
      errorResponse.setContentString(e.getMessage)
      Future(errorResponse)
    }
  }
}
