package com.danielsanrocha.xatu.controllers

import com.danielsanrocha.xatu.models.internals.RequestId
import com.danielsanrocha.xatu.models.responses.ServerMessage
import com.twitter.finagle.context.Contexts
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.typesafe.scalalogging.Logger

class NotFoundController() extends Controller {
  val logging: Logger = Logger(this.getClass)

  get("/api/:*") { request: Request =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) Fallback GET route called, returning 404...")
    response.notFound(ServerMessage("Route not found", requestId))
  }

  post("/:*") { request: Request =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) Fallback POST route called, returning 404...")
    response.notFound(ServerMessage("Route not found", requestId))
  }

  put("/:*") { request: Request =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) Fallback PUT route called, returning 404...")
    response.notFound(ServerMessage("Route not found", requestId))
  }

  patch("/:*") { request: Request =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) Fallback PATCH route called, returning 404...")
    response.notFound(ServerMessage("Route not found", requestId))
  }

  delete("/:*") { request: Request =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) Fallback DELETE route called, returning 404...")
    response.notFound(ServerMessage("Route not found", requestId))
  }
}
