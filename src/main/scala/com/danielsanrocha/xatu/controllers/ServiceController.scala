package com.danielsanrocha.xatu.controllers

import com.danielsanrocha.xatu.models.internals.{NewService, RequestId}
import com.danielsanrocha.xatu.models.requests.{Id, ServiceRequest}
import com.danielsanrocha.xatu.models.responses.{Created, Deleted, ServerMessage}
import com.danielsanrocha.xatu.services.ServiceService
import com.twitter.finagle.context.Contexts
import com.twitter.finatra.http.Controller
import com.twitter.util.logging.Logger

class ServiceController(implicit service: ServiceService, implicit val ec: scala.concurrent.ExecutionContext) extends Controller {
  private val logging: Logger = Logger(this.getClass)

  get("/service/:id") { id: Id =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) GET service route called...")
    service.getById(id.id) map {
      case Some(service) => response.ok(service)
      case None => response.notFound(ServerMessage(s"Service with ${id.id} not found", requestId))
    }
  }

  post("/service") { s: NewService =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) POST service route called...")
    service.create(s) map { id => response.ok(Created(id, requestId)) }
  }

  put("/service/:id") { s: ServiceRequest =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) PUT service/:id route called...")

    service.update(s.id, NewService(s.name, s.logFileDirectory, s.logFileDirectory, s.pidFile)) map {
      case true => response.ok(ServerMessage(s"Updated service with id ${s.id}", requestId))
      case false => response.notFound(ServerMessage(s"Service with id ${s.id} not found", requestId)) ::::::
    }
  }

  delete("/service/:id") { id: Id =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) Delete service route called...")

    service.delete(id.id) map {
      case true => response.ok(Deleted(id.id, requestId))
      case false => response.notFound(ServerMessage(s"Service with id ${id.id} not found", requestId))
    }
  }
}
