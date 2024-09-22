package com.danielsanrocha.xatu.observers

import com.danielsanrocha.xatu.models.internals.LogContainer
import com.danielsanrocha.xatu.models.responses.ContainerResponse
import com.danielsanrocha.xatu.services.{ContainerService, LogService}
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.core.command.LogContainerResultCallback
import com.typesafe.scalalogging.Logger

import java.io.Closeable

class LogContainerObserver(
    c: ContainerResponse,
    implicit val service: ContainerService,
    implicit val logService: LogService,
    implicit val dockerClient: DockerClient,
    implicit val ec: scala.concurrent.ExecutionContext
) extends Observer[ContainerResponse](c) {
  private val logging: Logger = Logger(this.getClass)
  var now = (System.currentTimeMillis() / 1000).toInt

  logging.info(s"Starting LogContainerObserver with now = ${now}")

  private var stream = dockerClient
    .logContainerCmd(_data.info.head.containerId)
    .withStdErr(true)
    .withStdOut(true)
    .withSince(now)
    .withFollowStream(true)

  override lazy val task: Runnable = () => {
    try
      stream
        .exec(new LogContainerResultCallback() {
          override def onNext(item: Frame): Unit = {
            logService.create(LogContainer(_data.id, _data.name, item.toString, System.currentTimeMillis()))
          }
        })
        .awaitCompletion
    catch {
      case e: InterruptedException =>
        logging.error(s"Interrupted Exception! ${e.getMessage}.")
      case e: Exception =>
        logging.error(s"Exception: ${e.getMessage}")
    }
  }

  override def reload(data: ContainerResponse): Unit = {
    logging.info(s"Container ${_data.name} reloaded!")
    super.reload(data)
    now = (System.currentTimeMillis() / 1000).toInt
    stream = dockerClient
      .logContainerCmd(_data.info.head.containerId)
      .withStdErr(true)
      .withStdOut(true)
      .withSince(now)
  }
}
