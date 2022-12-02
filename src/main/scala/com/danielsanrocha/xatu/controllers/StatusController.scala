package com.danielsanrocha.xatu.controllers

import com.danielsanrocha.xatu.managers._
import com.danielsanrocha.xatu.models.internals.{ManagerStatus, RequestId, Status, TimedCredential}
import com.danielsanrocha.xatu.services.UserService
import com.twitter.finagle.context.Contexts
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.util.logging.Logger

class StatusController(
    implicit val apiObserverManager: APIObserverManager,
    implicit val logServiceObserverManager: LogServiceObserverManager,
    implicit val serviceObserverManager: ServiceObserverManager,
    implicit val logContainerManager: LogContainerObserverManager
) extends Controller {
  private val logging: Logger = Logger(this.getClass)

  get("/status") { _: Request =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) Status route called, returning managers info...")

    response.ok(ManagerStatus(apiObserverManager.status(), logServiceObserverManager.status(), serviceObserverManager.status(), logContainerManager.status()))
  }
}
