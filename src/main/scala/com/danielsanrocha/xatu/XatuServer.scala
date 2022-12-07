package com.danielsanrocha.xatu

import com.danielsanrocha.xatu.controllers._
import com.danielsanrocha.xatu.filters.{AuthorizeFilter, CORSFilter, ExceptionHandlerFilter, RequestIdFilter, TimeoutFilter}
import com.danielsanrocha.xatu.managers.{APIObserverManager, LogContainerObserverManager, LogServiceObserverManager, ServiceObserverManager}
import com.danielsanrocha.xatu.repositories._
import com.danielsanrocha.xatu.services._
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DockerClientBuilder
import com.twitter.finagle.util.DefaultTimer
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.util.logging.Logger
import com.typesafe.config.{Config, ConfigFactory}
import redis.clients.jedis.{JedisPool, JedisPoolConfig}
import slick.jdbc.MySQLProfile.api.Database

import java.util.concurrent.TimeUnit
import com.twitter.util._

class XatuServer(implicit val client: Database, implicit val ec: scala.concurrent.ExecutionContext, implicit val logRepository: LogRepository, implicit val greatManager: TheGreatManager)
    extends HttpServer {
  private val logging: Logger = Logger(this.getClass)

  logging.info("Loading configuration file and accessing it...")
  private implicit val conf: Config = ConfigFactory.load()

  private val port = conf.getInt("api.port")
  private val appName = conf.getString("api.name")

  override protected def defaultHttpPort: String = s":${port}"

  override protected def defaultHttpServerName: String = appName

  logging.info("Connecting to redis...")
  private val redisHost = conf.getString("redis.host")
  private val redisPort = conf.getInt("redis.port")

  private val jedisPoolConfig = new JedisPoolConfig();
  jedisPoolConfig.setMaxTotal(400)
  jedisPoolConfig.setMaxIdle(400)
  jedisPoolConfig.setMinIdle(200)
  jedisPoolConfig.setMaxWaitMillis(2)
  jedisPoolConfig.setBlockWhenExhausted(true)
  jedisPoolConfig.setTestOnBorrow(true)
  jedisPoolConfig.setTestOnReturn(true)
  jedisPoolConfig.setTestWhileIdle(true)
  jedisPoolConfig.setNumTestsPerEvictionRun(3)
  implicit val cache: JedisPool = new JedisPool(jedisPoolConfig, redisHost, redisPort, 10000)

  logging.info("Instantiating docker client...")
  implicit val dockerClient: DockerClient = DockerClientBuilder.getInstance.build

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

  logging.info("Instantiating controllers...")
  private val healthcheckController = new HealthcheckController()
  private val notFoundController = new NotFoundController()
  private val loginController = new LoginController()
  private val userController = new UserController()
  private val serviceController = new ServiceController()
  private val apiController = new APIController()
  private val logController = new LogController()
  private val containerController = new ContainerController()
  private val corsController = new CORSController()

  logging.info("Instantiating filters...")
  private val exceptionHandlerFilter = new ExceptionHandlerFilter()
  private val requestIdFilter = new RequestIdFilter()
  private val authorizeFilter = new AuthorizeFilter(authorizationHeader, cache)
  private val corsFilter = new CORSFilter()

  private val timeout = conf.getLong("api.timeout")
  private val timeoutFilter = new TimeoutFilter(Duration(timeout, TimeUnit.MILLISECONDS), ec)

  override protected def configureHttp(router: HttpRouter): Unit = {
    router
      .filter(corsFilter)
      .filter(requestIdFilter)
      .filter(exceptionHandlerFilter)
      .filter(timeoutFilter)
      .add(loginController)
      .add(authorizeFilter, userController)
      .add(authorizeFilter, serviceController)
      .add(authorizeFilter, apiController)
      .add(authorizeFilter, logController)
      .add(authorizeFilter, containerController)
      .add(authorizeFilter, greatManager.statusController)
      .add(healthcheckController)
      .add(notFoundController)
      .add(corsController)
  }
}
