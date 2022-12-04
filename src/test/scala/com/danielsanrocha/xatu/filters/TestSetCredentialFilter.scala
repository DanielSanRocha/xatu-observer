package com.danielsanrocha.xatu.filters

import com.danielsanrocha.xatu.models.internals.TimedCredential
import com.twitter.finagle.context.Contexts
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

class TestSetCredentialFilter(credential: TimedCredential) extends SimpleFilter[Request, Response] {
  def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    Contexts.local.let(TimedCredential, credential) { service(request) }
  }
}
