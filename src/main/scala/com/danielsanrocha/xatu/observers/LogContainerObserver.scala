package com.danielsanrocha.xatu.observers

import com.twitter.util.logging.Logger
import com.spotify.docker.client.DockerClient

import java.util.concurrent._
import com.danielsanrocha.xatu.models.internals.LogContainer
import com.danielsanrocha.xatu.models.responses.ContainerResponse
import com.danielsanrocha.xatu.services.{ContainerService, LogService}

import java.nio.charset.StandardCharsets

class LogContainerObserver(
    c: ContainerResponse,
    implicit val service: ContainerService,
    implicit val logService: LogService,
    implicit val dockerClient: DockerClient,
    implicit val ec: scala.concurrent.ExecutionContext
) extends Observer[ContainerResponse](c) {
  private val logging: Logger = Logger(this.getClass)

  protected var stream = dockerClient.logs(_data.info.head.containerId)
  while (stream.hasNext) stream.next()

  override protected lazy val task: Runnable = () => {
    while (stream.hasNext) {
      val line = stream.next()
      val log = LogContainer(_data.id, _data.name, line.toString, System.currentTimeMillis())
      logging.debug(s"Log found! Container(${_data.id}, ${_data.name}), indexing it ...")
      logService.create(log) recover { case e: Exception =>
        logging.warn(s"Error indexing logs for container ${_data.name} -> Error: ${e.getMessage}")
      }
    }
  }

  override def reload(data: ContainerResponse): Unit = {
    super.reload(data)
    stream = dockerClient.logs(_data.info.head.containerId)
  }
}
