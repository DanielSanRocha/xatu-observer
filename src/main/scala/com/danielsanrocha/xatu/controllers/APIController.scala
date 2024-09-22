package com.danielsanrocha.xatu.controllers

import com.danielsanrocha.xatu.models.internals.{NewAPI, RequestId}
import com.danielsanrocha.xatu.models.requests.{GetAll, Id, APIRequest}
import com.danielsanrocha.xatu.models.responses.{Created, Deleted, HitsResult, ServerMessage}
import com.danielsanrocha.xatu.services.{APIService}
import com.twitter.finagle.context.Contexts
import com.twitter.finatra.http.Controller
import com.typesafe.scalalogging.Logger

class APIController(implicit service: APIService, implicit val ec: scala.concurrent.ExecutionContext) extends Controller {
  private val logging: Logger = Logger(this.getClass)

  get("/api/:id") { id: Id =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) GET api route called...")
    service.getById(id.id) map {
      case Some(api) => response.ok(api)
      case None      => response.notFound(ServerMessage(s"API with ${id.id} not found", requestId))
    }
  }

  post("/api") { api: NewAPI =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) POST service route called...")
    service.create(api) map { id => response.ok(Created(id, requestId)) }
  }

  put("/api/:id") { s: APIRequest =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) PUT api/:id route called...")

    service.update(s.id, NewAPI(s.name, s.host, s.port, s.healthcheckRoute)) map {
      case true  => response.ok(ServerMessage(s"Updated api with id ${s.id}", requestId))
      case false => response.notFound(ServerMessage(s"API with id ${s.id} not found", requestId))
    }
  }

  delete("/api/:id") { id: Id =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) Delete api route called...")

    service.delete(id.id) map {
      case true  => response.ok(Deleted(id.id, requestId))
      case false => response.notFound(ServerMessage(s"API with id ${id.id} not found", requestId))
    }
  }

  get("/apis") { request: GetAll =>
    service.getAll(request.limit, request.offset) map { apis =>
      {
        response.ok(HitsResult(apis.length, apis))
      }
    }
  }
}
