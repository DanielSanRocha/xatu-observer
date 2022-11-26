package com.danielsanrocha.xatu.controllers

import com.twitter.util.logging.Logger
import com.twitter.finagle.context.Contexts
import com.twitter.finatra.http.{Controller}
import com.twitter.finagle.http.{Request}

import com.danielsanrocha.xatu.models.responses.{ServerMessage}
import com.danielsanrocha.xatu.models.internals.{RequestId}

class HealthcheckController() extends Controller {
  private val logging: Logger = Logger(this.getClass)

  get("/healthcheck") { request: Request =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) Healthcheck called, returning Ok...")
    response.ok.body(ServerMessage("Ok", requestId))
  }
}
