package com.danielsanrocha.xatu

import com.danielsanrocha.xatu.controllers._
import com.danielsanrocha.xatu.filters.{AuthorizeFilter, ExceptionHandlerFilter, RequestIdFilter}
import com.danielsanrocha.xatu.managers.APIObserverManager
import com.danielsanrocha.xatu.repositories.{APIRepository, APIRepositoryImpl, ServiceRepository, ServiceRepositoryImpl, UserRepository, UserRepositoryImpl}
import com.danielsanrocha.xatu.services.{APIService, APIServiceImpl, ServiceService, ServiceServiceImpl, UserService, UserServiceImpl}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.util.logging.Logger
import com.typesafe.config.{Config, ConfigFactory}
import redis.clients.jedis.{Jedis, JedisPool}
import slick.jdbc.MySQLProfile.api.Database

class XatuServer(implicit val client: Database) extends HttpServer {
  private val logging: Logger = Logger(this.getClass)
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  logging.info("Loading configuration file and acessing it...")
  private implicit val conf: Config = ConfigFactory.load()

  val port = conf.getInt("api.port")
  val appName = conf.getString("api.name")

  override protected def defaultHttpPort: String = s":${port}"

  override protected def defaultHttpServerName: String = appName

  logging.info("Connecting to redis...")
  val redisHost = conf.getString("redis.host")
  val redisPort = conf.getInt("redis.port")
  implicit val cache: Jedis = new JedisPool(redisHost, redisPort).getResource()

  logging.info("Creating repositories...")
  implicit val userRepository: UserRepository = new UserRepositoryImpl()
  implicit val serviceRepository: ServiceRepository = new ServiceRepositoryImpl()
  implicit val apiRepository: APIRepository = new APIRepositoryImpl()

  logging.info("Creating services...")
  private implicit val userService: UserService = new UserServiceImpl()
  private implicit val serviceService: ServiceService = new ServiceServiceImpl()
  private implicit val apiService: APIService = new APIServiceImpl()

  logging.info("Getting api configuration...")
  val authorizationHeader = conf.getString("api.auth.header")

  logging.info("Instatiating controllers...")
  private val healthcheckController = new HealthcheckController()
  private val notFoundController = new NotFoundController()
  private val loginController = new LoginController()
  private val userController = new UserController()
  private val serviceController = new ServiceController()
  private val apiController = new APIController()

  logging.info("Instantiaing filters...")
  private val exceptionHandlerFilter = new ExceptionHandlerFilter()
  private val requestIdFilter = new RequestIdFilter()
  private val authorizeFilter = new AuthorizeFilter(authorizationHeader, cache)

  logging.info("Instantiating Observer Managers...")
  private val apiObserverManager = new APIObserverManager()

  override protected def configureHttp(router: HttpRouter): Unit = {
    router
      .filter(requestIdFilter)
      .filter(exceptionHandlerFilter)
      .add(authorizeFilter, userController)
      .add(authorizeFilter, serviceController)
      .add(authorizeFilter, apiController)
      .add(loginController)
      .add(healthcheckController)
      .add(notFoundController)
  }
}
