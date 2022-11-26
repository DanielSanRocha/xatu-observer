package com.danielsanrocha.xatu

import com.typesafe.config.{Config, ConfigFactory}
import com.twitter.util.logging.Logger
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.routing.HttpRouter
import redis.clients.jedis.{Jedis, JedisPool}
import slick.jdbc.MySQLProfile.api.Database
import com.danielsanrocha.xatu.repositories.{UserRepository, UserRepositoryImpl}
import com.danielsanrocha.xatu.services.{UserService, UserServiceImpl}
import com.danielsanrocha.xatu.controllers.{HealthcheckController, LoginController, NotFoundController, UserController}
import com.danielsanrocha.xatu.filters.{AuthorizeFilter, ExceptionHandlerFilter, RequestIdFilter}

object XatuApp extends XatuServer

class XatuServer extends HttpServer {
  private val logging: Logger = Logger(this.getClass)
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  logging.info("Loading configuration file and acessing it...")
  private implicit val conf: Config = ConfigFactory.load()

  val port = conf.getInt("api.port")
  val appName = conf.getString("api.name")

  override protected def defaultHttpPort: String = s":${port}"
  override protected def defaultHttpServerName: String = appName

  logging.info("Loading slick MySQLClient...")
  implicit val client: Database = Database.forConfig("mysql")

  logging.info("Connecting to redis...")
  val redisHost = conf.getString("redis.host")
  val redisPort = conf.getInt("redis.port")
  implicit val cache: Jedis = new JedisPool(redisHost, redisPort).getResource()

  logging.info("Creating repositories...")
  implicit val userRepository: UserRepository = new UserRepositoryImpl()

  logging.info("Creating services...")
  private implicit val userService: UserService = new UserServiceImpl()

  logging.info("Getting api configuration...")
  val authorizationHeader = conf.getString("api.auth.header")

  logging.info("Instatiating controllers...")
  private val healthcheckController = new HealthcheckController()
  private val notFoundController = new NotFoundController()
  private val loginController = new LoginController()
  private val userController = new UserController()

  logging.info("Instantiaing filters...")
  private val exceptionHandlerFilter = new ExceptionHandlerFilter()
  private val requestIdFilter = new RequestIdFilter()
  private val authorizeFilter = new AuthorizeFilter(authorizationHeader, cache)

  override protected def configureHttp(router: HttpRouter): Unit = {
    router
      .filter(requestIdFilter)
      .filter(exceptionHandlerFilter)
      .add(authorizeFilter, userController)
      .add(loginController)
      .add(healthcheckController)
      .add(notFoundController)
  }
}