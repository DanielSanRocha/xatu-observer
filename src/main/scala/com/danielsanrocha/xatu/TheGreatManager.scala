package com.danielsanrocha.xatu

import com.danielsanrocha.xatu.controllers.StatusController
import com.danielsanrocha.xatu.managers.{APIObserverManager, LogContainerObserverManager, LogServiceObserverManager, ServiceObserverManager}
import com.danielsanrocha.xatu.repositories.{
  APIRepository,
  APIRepositoryImpl,
  ContainerRepository,
  ContainerRepositoryImpl,
  LogRepository,
  LogRepositoryImpl,
  ServiceRepository,
  ServiceRepositoryImpl,
  UserRepository,
  UserRepositoryImpl
}
import com.danielsanrocha.xatu.services.{APIService, APIServiceImpl, ContainerService, ContainerServiceImpl, LogService, LogServiceImpl, ServiceService, ServiceServiceImpl}
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DockerClientBuilder
import com.twitter.util.logging.Logger
import com.typesafe.config.{Config, ConfigFactory}
import slick.jdbc.JdbcBackend.Database

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

class TheGreatManager {
  private val logging: Logger = Logger(this.getClass)

  implicit val client: Database = Database.forConfig("mysql")
  private val executorService = Executors.newFixedThreadPool(8)
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(executorService)

  logging.info("Creating repositories...")
  implicit val userRepository: UserRepository = new UserRepositoryImpl()
  implicit val serviceRepository: ServiceRepository = new ServiceRepositoryImpl()
  implicit val apiRepository: APIRepository = new APIRepositoryImpl()
  implicit val containerRepository: ContainerRepository = new ContainerRepositoryImpl()
  implicit val logRepository: LogRepository = new LogRepositoryImpl("elasticsearch", ec)

  logging.info("Instantiating docker client...")
  implicit val dockerClient: DockerClient = DockerClientBuilder.getInstance.build

  logging.info("Creating services...")
  private implicit val serviceService: ServiceService = new ServiceServiceImpl()
  private implicit val apiService: APIService = new APIServiceImpl()
  private implicit val logService: LogService = new LogServiceImpl()
  private implicit val containerService: ContainerService = new ContainerServiceImpl()

  logging.info("Loading configuration file and accessing it...")
  private implicit val conf: Config = ConfigFactory.load()

  logging.info("Instantiating Observers Managers...")
  private implicit val apiObserverManager: APIObserverManager = new APIObserverManager()
  private implicit val logServiceManager: LogServiceObserverManager = new LogServiceObserverManager()
  private implicit val serviceObserverManager: ServiceObserverManager = new ServiceObserverManager()
  private implicit val logContainerManager: LogContainerObserverManager = new LogContainerObserverManager()

  private val managersEnable = conf.getBoolean("managers.enabled")

  if (managersEnable) {
    logging.info("Starting managers...")
    apiObserverManager.start()
    logServiceManager.start()
    serviceObserverManager.start()
    logContainerManager.start()
  }
  private val token = conf.getString("telegram.bot_token")
  if (token != "inactive") {
    logging.info("Starting TelegramNotifier...")
    val chatId = conf.getString("telegram.chat_id")
    val telegramNotifier = new TelegramNotifier(token = token, chatId = chatId, containerService, apiService, serviceService, ec)
    telegramNotifier.start()
  }

  val statusController = new StatusController()
}
