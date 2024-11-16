package com.danielsanrocha.xatu.controllers

import com.danielsanrocha.xatu.models.internals.RequestId
import com.danielsanrocha.xatu.models.responses.ServerMessage
import com.twitter.finagle.context.Contexts
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.typesafe.scalalogging.Logger

class IndexController() extends Controller {
  private val logging: Logger = Logger(this.getClass)

  get("/api") { _: Request =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) Index route called, returning ok...")
    response.ok(ServerMessage("Welcome to Xatu Observer!", requestId))
  }
}
