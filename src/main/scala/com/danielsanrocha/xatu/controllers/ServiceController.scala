package com.danielsanrocha.xatu.controllers

import com.twitter.util.logging.Logger
import com.twitter.finagle.context.Contexts
import com.twitter.finatra.http.{Controller}
import com.twitter.finagle.http.{Request}

import com.danielsanrocha.xatu.models.internals.{RequestId, TimedCredential}
import com.danielsanrocha.xatu.models.requests.{Id}
import com.danielsanrocha.xatu.services.{ServiceService}

class ServiceController(implicit service: ServiceService, implicit val ec: scala.concurrent.ExecutionContext) extends Controller {
  private val logging: Logger = Logger(this.getClass)

  get("/service/:id") { id: Id =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) User route called, returning user info...")

    logging.debug(s"(x-request-id - $requestId) Getting credentials...")
    val credential = Contexts.local.get(TimedCredential).head

    service.getById(credential.id) map {
      case Some(service) => response.ok(service)
      case None       => throw new Exception(s"Logged user with id ${credential.id} not found, this is strange!")
    }
  }
}
