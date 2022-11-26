package com.danielsanrocha.xatu.filters

import io.jvm.uuid.UUID

import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import com.twitter.finagle.context.Contexts
import javax.inject.{Inject, Singleton}

import com.danielsanrocha.xatu.models.internals.{RequestId}

class RequestIdFilter() extends SimpleFilter[Request, Response] {
  def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    val requestId = UUID.random.toString
    Contexts.local.let(RequestId, RequestId(requestId)) { service(request) }
  }
}
