package com.danielsanrocha.xatu.controllers

import com.danielsanrocha.xatu.models.internals.RequestId
import com.danielsanrocha.xatu.models.responses.ServerStatus
import com.danielsanrocha.xatu.repositories.LogRepository
import com.github.dockerjava.api.DockerClient
import com.twitter.finagle.context.Contexts
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.util.logging.Logger
import io.jvm.uuid.UUID
import redis.clients.jedis.Jedis
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future

class HealthcheckController(
    implicit val cache: Jedis,
    implicit val client: Database,
    implicit val dockerClient: DockerClient,
    implicit val logRepository: LogRepository,
    implicit val ec: scala.concurrent.ExecutionContext
) extends Controller {
  private val logging: Logger = Logger(this.getClass)

  get("/healthcheck") { request: Request =>
    val requestId = Contexts.local.get(RequestId).head.requestId
    logging.info(s"(x-request-id - $requestId) Healthcheck called, checking redis and mysql...")

    val random = UUID.random.toString

    val redisFuture = Future[Option[Exception]] {
      cache.set("random", random)
      val random2 = cache.get("random")

      if (random != random2) Some(new Exception("Problems with redis..."))
      else None
    } recover { case e: Exception => Some(e) }

    val mysqlFuture = client.run(sql"show tables".as[String]) map { _ =>
      logging.info(s"(x-request-id - $requestId) Returning ok...")
      None
    } recover { case e: Exception => Some(e) }

    val dockerFuture = Future[Option[Exception]] {
      dockerClient.pingCmd().exec()
      None
    } recover { case e: Exception => Some(e) }

    val elasticsearchFuture = Future[Option[Exception]] {
      logRepository.status()
      None
    } recover { case e: Exception => Some(e) }

    Future.sequence(Seq(redisFuture, mysqlFuture, dockerFuture, elasticsearchFuture)) map { result =>
      var flag = false

      val redis = result.head match {
        case Some(e) => flag = true; e.getMessage
        case None    => "Ok"
      }
      val mysql = result(1) match {
        case Some(e) => flag = true; e.getMessage
        case None    => "Ok"
      }
      val docker = result(2) match {
        case Some(e) => flag = true; e.getMessage
        case None    => "Ok"
      }

      val elasticsearch = result(3) match {
        case Some(e) => flag = true; e.getMessage
        case None    => "Ok"
      }

      val status = ServerStatus(redis, mysql, docker, elasticsearch)
      if (flag) response.internalServerError(status)
      else response.ok(status)
    }
  }
}
