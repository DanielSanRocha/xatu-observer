package com.danielsanrocha.xatu.filters

import com.danielsanrocha.xatu.models.internals.RequestId
import com.twitter.finagle.context.Contexts
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

class TestSetRequestIdFilter(requestId: String) extends SimpleFilter[Request, Response] {
  def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    Contexts.local.let(RequestId, RequestId(requestId)) { service(request) }
  }
}
