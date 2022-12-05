package com.danielsanrocha.xatu.filters

import com.twitter.util.logging.Logger
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.context.Contexts
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finatra.http.exceptions.ExceptionManager
import com.twitter.util.Future
import javax.inject.{Inject, Singleton}
import redis.clients.jedis.JedisPool

import com.danielsanrocha.xatu.models.internals.{RequestId, TimedCredential}
import com.danielsanrocha.xatu.models.responses.{ServerMessage}

@Singleton
class AuthorizeFilter(authorizationHeader: String, implicit val cachePool: JedisPool) extends SimpleFilter[Request, Response] {
  val logging: Logger = Logger(this.getClass)
  val jsonMapper = JsonMapper.builder().addModule(DefaultScalaModule).build();

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.warn(s"(x-request-id - ${requestId}) Checking for Authorization header...")

    request.headerMap.get(authorizationHeader) match {
      case None => {
        logging.warn(s"(x-request-id - ${requestId}) $authorizationHeader header missing, forbidden!")
        val forbiddenResponse = Response()
        forbiddenResponse.statusCode = 403
        forbiddenResponse.setContentTypeJson()
        forbiddenResponse.setContentString(jsonMapper.writeValueAsString(ServerMessage(s"Missing $authorizationHeader header", requestId)))

        Future(forbiddenResponse)
      }
      case Some(header) =>
        val cache = cachePool.getResource
        cache.get(s"token:$header") match {
          case null =>
            cachePool.returnResource(cache)
            logging.warn(s"(x-request-id - ${requestId}) User sent header Authorization: ${header} which do not exist on the cache server!")
            val errorResponse = Response()
            errorResponse.statusCode = 403
            errorResponse.setContentTypeJson()
            errorResponse.setContentString(jsonMapper.writeValueAsString(ServerMessage(s"Token ${header} was not found in the cache server! Try to login again", requestId)))
            Future(errorResponse)

          case credentialJson =>
            cachePool.returnResource(cache)
            logging.debug(s"(x-request-id - $requestId) Token found on cache server, decoding...")
            val credential = jsonMapper.readValue(credentialJson, classOf[TimedCredential])
            Contexts.local.let(TimedCredential, credential) { service(request) }

        }
    }
  }
}
