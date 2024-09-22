package com.danielsanrocha.xatu.controllers

import com.twitter.finagle.context.Contexts
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.typesafe.scalalogging.Logger

import com.danielsanrocha.xatu.models.internals.{NewContainer, RequestId}
import com.danielsanrocha.xatu.models.requests.{GetAll, Id, ServiceRequest}
import com.danielsanrocha.xatu.models.responses.{Created, Deleted, HitsResult, ServerMessage}
import com.danielsanrocha.xatu.services.ContainerService

class ContainerController(implicit service: ContainerService, implicit val ec: scala.concurrent.ExecutionContext) extends Controller {
  private val logging: Logger = Logger(this.getClass)

  get("/container/:id") { id: Id =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) GET container route called...")
    service.getById(id.id) map {
      case Some(c) => response.ok(c)
      case None    => response.notFound(ServerMessage(s"Container with ${id.id} not found", requestId))
    }
  }

  post("/container") { s: NewContainer =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) POST container route called...")
    service.create(s) map { id => response.ok(Created(id, requestId)) }
  }

  delete("/container/:id") { id: Id =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) Delete container route called...")

    service.delete(id.id) map {
      case true  => response.ok(Deleted(id.id, requestId))
      case false => response.notFound(ServerMessage(s"Container with id ${id.id} not found", requestId))
    }
  }

  get("/containers") { request: GetAll =>
    service.getAll(request.limit, request.offset) map { containers =>
      {
        response.ok(HitsResult(containers.length, containers))
      }
    }
  }
}
