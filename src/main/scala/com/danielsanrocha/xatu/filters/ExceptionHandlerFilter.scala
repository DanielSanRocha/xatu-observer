package com.danielsanrocha.xatu.filters

import com.twitter.util.logging.Logger
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.twitter.finatra.jackson.caseclass.exceptions.CaseClassMappingException
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.context.Contexts
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finatra.http.exceptions.ExceptionManager
import com.twitter.util.Future
import com.danielsanrocha.xatu.models.internals.RequestId
import com.danielsanrocha.xatu.models.responses.ServerMessage
import com.danielsanrocha.xatu.exceptions.{BadArgumentException, ForbiddenException, NotFoundException, TimeoutException}

class ExceptionHandlerFilter() extends SimpleFilter[Request, Response] {
  private val logging: Logger = Logger(this.getClass)
  private val jsonMapper = JsonMapper.builder().addModule(DefaultScalaModule).build();

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.debug(s"(x-request-id - $requestId) Checking for exceptions...")

    service(request) map { response =>
      logging.debug(s"(x-request-id - $requestId) Response: ${response.getContentString}")
      response
    } rescue { case e: Throwable =>
      logging.error(s"(x-request-id - $requestId) ${e.getMessage}")
      e.printStackTrace()

      val statusCode = e match {
        case _: ForbiddenException        => 403
        case _: NotFoundException         => 404
        case _: BadArgumentException      => 400
        case _: JsonParseException        => 400
        case _: CaseClassMappingException => 400
        case _: TimeoutException          => 500
        case _: Exception                 => 500
      }

      val errorResponse = Response()
      errorResponse.statusCode = statusCode
      errorResponse.setContentTypeJson()
      errorResponse.setContentString(jsonMapper.writeValueAsString(ServerMessage(e.getMessage, requestId)))
      Future(errorResponse)
    }
  }
}
