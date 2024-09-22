package com.danielsanrocha.xatu.managers

import com.github.dockerjava.api.DockerClient
import com.typesafe.scalalogging.Logger

import com.danielsanrocha.xatu.models.responses.ContainerResponse
import com.danielsanrocha.xatu.observers.LogContainerObserver
import com.danielsanrocha.xatu.services.{ContainerService, LogService}

class LogContainerObserverManager(
    implicit val service: ContainerService,
    override implicit val ec: scala.concurrent.ExecutionContext,
    implicit val logService: LogService,
    implicit val dockerClient: DockerClient
) extends Manager[ContainerResponse, LogContainerObserver](service, ec) {
  private val logging: Logger = Logger(this.getClass)
  logging.info("Starting LogContainerObserverManager")
  override protected def createObserver(data: ContainerResponse): LogContainerObserver = {
    try {
      new LogContainerObserver(data, service, logService, dockerClient, ec)
    } catch {
      case e: Exception =>
        logging.error(s"Error creating LogContainerObserver. Message: ${e.getMessage}")
        null
    }
  }
}
