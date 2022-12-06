package com.danielsanrocha.xatu.controllers

import com.twitter.util.logging.Logger
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.twitter.finagle.context.Contexts
import com.twitter.finatra.http.Controller
import redis.clients.jedis.JedisPool
import com.danielsanrocha.xatu.commons.Security
import com.danielsanrocha.xatu.models.internals.{RequestId, TimedCredential}
import com.danielsanrocha.xatu.models.responses.{ServerMessage, Token}
import com.danielsanrocha.xatu.models.requests.Credential
import com.danielsanrocha.xatu.services.UserService
import com.twitter.finagle.http.Request

class LoginController(implicit val service: UserService, implicit val cachePool: JedisPool, implicit val ec: scala.concurrent.ExecutionContext) extends Controller {
  private val logging: Logger = Logger(this.getClass)
  private val jsonMapper = JsonMapper.builder().addModule(DefaultScalaModule).build()

  post("/login") { credential: Credential =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) Login route called, searching for user...")

    service.getByEmail(credential.email) map {
      case Some(user) =>
        logging.debug(s"(x-request-id - $requestId) User with email ${credential.email} found, validating credential...")
        Security.hash(credential.password) match {
          case user.password =>
            logging.debug(s"(x-request-id - $requestId) Passwords did match! Generating credential and token...")
            val timedCredential = TimedCredential(id = user.id, email = credential.email, timestamp = System.currentTimeMillis())

            val credentialJson = jsonMapper.writeValueAsString(timedCredential)
            val token = Security.hash(credentialJson)
            val cache = cachePool.getResource
            val r1 = cache.set(s"token:$token", credentialJson)
            val r2 = cache.expire(s"token:${token}", 4 * 60 * 60)
            cache.close()

            logging.debug(s"(x-request-id - $requestId) Returning token")
            response.ok(Token(s"Cache server returned: ${r1} - ${r2}", token, requestId))
          case _ => response.forbidden(ServerMessage(s"Passwords did not match!", requestId))
        }

      case None =>
        logging.debug(s"(x-request-id - $requestId) User with email ${credential.email} not found, returning 404...")
        response.notFound(ServerMessage(s"User with email ${credential.email} not found", requestId))
    }
  }
}
