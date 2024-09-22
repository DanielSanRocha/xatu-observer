package com.danielsanrocha.xatu.controllers

import com.typesafe.scalalogging.Logger
import com.twitter.finagle.context.Contexts
import com.twitter.finatra.http.{Controller}
import com.twitter.finagle.http.{Request}

import com.danielsanrocha.xatu.models.responses.{UserResponse}
import com.danielsanrocha.xatu.models.internals.{RequestId, TimedCredential}
import com.danielsanrocha.xatu.services.{UserService}

class UserController(implicit service: UserService, implicit val ec: scala.concurrent.ExecutionContext) extends Controller {
  private val logging: Logger = Logger(this.getClass)

  get("/user") { request: Request =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) User route called, returning user info...")

    logging.debug(s"(x-request-id - $requestId) Getting credentials...")
    val credential = Contexts.local.get(TimedCredential).head

    service.getById(credential.id) map {
      case Some(user) => response.ok(user)
      case None       => throw new Exception(s"Logged user with id ${credential.id} not found, this is strange!")
    }
  }
}
