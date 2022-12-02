package com.danielsanrocha.xatu.controllers

import com.twitter.util.logging.Logger
import com.twitter.finagle.context.Contexts
import com.twitter.finatra.http.Controller
import com.twitter.finagle.http.Request
import slick.jdbc.MySQLProfile.api._
import redis.clients.jedis.Jedis
import io.jvm.uuid.UUID

import com.danielsanrocha.xatu.models.responses.ServerMessage
import com.danielsanrocha.xatu.models.internals.RequestId

class HealthcheckController(implicit val cache: Jedis, implicit val client: Database, implicit val ec: scala.concurrent.ExecutionContext) extends Controller {
  private val logging: Logger = Logger(this.getClass)

  get("/healthcheck") { request: Request =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) Healthcheck called, checking redis and mysql...")

    val random = UUID.random.toString

    cache.set("random", random)
    val random2 = cache.get("random")

    if (random != random2) {
      throw new Exception("Problems with redis...")
    }

    client.run(sql"show tables".as[String]) map { _ =>
      logging.info(s"(x-request-id - $requestId) Returning ok...")
      response.ok.body(ServerMessage("Ok", requestId))
    }
  }
}
