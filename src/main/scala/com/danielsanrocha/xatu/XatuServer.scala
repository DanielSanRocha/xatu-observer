package com.danielsanrocha.xatu

import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.api.DockerClient
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.util.logging.Logger
import com.typesafe.config.{Config, ConfigFactory}
import redis.clients.jedis.{Jedis, JedisPool}
import slick.jdbc.MySQLProfile.api.Database
import com.danielsanrocha.xatu.controllers._
import com.danielsanrocha.xatu.filters.{AuthorizeFilter, CORSFilter, ExceptionHandlerFilter, RequestIdFilter}
import com.danielsanrocha.xatu.managers.{APIObserverManager, LogContainerObserverManager, LogServiceObserverManager, ServiceObserverManager}
import com.danielsanrocha.xatu.repositories.{
  APIRepository,
  APIRepositoryImpl,
  ContainerRepository,
  ContainerRepositoryImpl,
  LogRepository,
  ServiceRepository,
  ServiceRepositoryImpl,
  UserRepository,
  UserRepositoryImpl
}
import com.danielsanrocha.xatu.services.{
  APIService,
  APIServiceImpl,
  ContainerService,
  ContainerServiceImpl,
  LogService,
  LogServiceImpl,
  ServiceService,
  ServiceServiceImpl,
  UserService,
  UserServiceImpl
}

class XatuServer(implicit val client: Database, implicit val ec: scala.concurrent.ExecutionContext, implicit val logRepository: LogRepository) extends HttpServer {
  private val logging: Logger = Logger(this.getClass)

  logging.info("Loading configuration file and acessing it...")
  private implicit val conf: Config = ConfigFactory.load()

  val port = conf.getInt("api.port")
  val appName = conf.getString("api.name")

  override protected def defaultHttpPort: String = s":${port}"

  override protected def defaultHttpServerName: String = appName

  logging.info("Connecting to redis...")
  private val redisHost = conf.getString("redis.host")
  private val redisPort = conf.getInt("redis.port")
  implicit val cache: Jedis = new JedisPool(redisHost, redisPort).getResource

  logging.info("Instantiating docker client...")
  private implicit val dockerClient: DockerClient = DockerClientBuilder.getInstance.build

  logging.info("Creating repositories...")
  implicit val userRepository: UserRepository = new UserRepositoryImpl()
  implicit val serviceRepository: ServiceRepository = new ServiceRepositoryImpl()
  implicit val apiRepository: APIRepository = new APIRepositoryImpl()
  implicit val containerRepository: ContainerRepository = new ContainerRepositoryImpl()

  logging.info("Creating services...")
  private implicit val userService: UserService = new UserServiceImpl()
  private implicit val serviceService: ServiceService = new ServiceServiceImpl()
  private implicit val apiService: APIService = new APIServiceImpl()
  private implicit val logService: LogService = new LogServiceImpl()
  private implicit val containerService: ContainerService = new ContainerServiceImpl()

  logging.info("Getting api configuration...")
  private val authorizationHeader = conf.getString("api.auth.header")

  logging.info("Instatiating controllers...")
  private val healthcheckController = new HealthcheckController()
  private val notFoundController = new NotFoundController()
  private val loginController = new LoginController()
  private val userController = new UserController()
  private val serviceController = new ServiceController()
  private val apiController = new APIController()
  private val logController = new LogController()
  private val containerController = new ContainerController()
  private val corsController = new CORSController()

  logging.info("Instantiaing filters...")
  private val exceptionHandlerFilter = new ExceptionHandlerFilter()
  private val requestIdFilter = new RequestIdFilter()
  private val authorizeFilter = new AuthorizeFilter(authorizationHeader, cache)
  private val corsFilter = new CORSFilter()

  logging.info("Instantiating Observers Managers...")
  private implicit val apiObserverManager: APIObserverManager = new APIObserverManager()
  private implicit val logServiceManager: LogServiceObserverManager = new LogServiceObserverManager()
  private implicit val serviceObserverManager: ServiceObserverManager = new ServiceObserverManager()
  private implicit val logContainerManager: LogContainerObserverManager = new LogContainerObserverManager()

  private val statusController = new StatusController()

  override protected def configureHttp(router: HttpRouter): Unit = {
    router
      .filter(corsFilter)
      .filter(requestIdFilter)
      .filter(exceptionHandlerFilter)
      .add(authorizeFilter, userController)
      .add(authorizeFilter, serviceController)
      .add(authorizeFilter, apiController)
      .add(authorizeFilter, logController)
      .add(authorizeFilter, containerController)
      .add(authorizeFilter, statusController)
      .add(loginController)
      .add(healthcheckController)
      .add(corsController)
      .add(notFoundController)
  }
}
