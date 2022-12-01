package com.danielsanrocha.xatu.observers

import com.twitter.util.logging.Logger
import com.github.dockerjava.api.DockerClient

import java.util.concurrent._
import com.danielsanrocha.xatu.models.internals.LogContainer
import com.danielsanrocha.xatu.models.responses.ContainerResponse
import com.danielsanrocha.xatu.services.{ContainerService, LogService}
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.core.command.LogContainerResultCallback

import java.nio.charset.StandardCharsets

class LogContainerObserver(
    c: ContainerResponse,
    implicit val service: ContainerService,
    implicit val logService: LogService,
    implicit val dockerClient: DockerClient,
    implicit val ec: scala.concurrent.ExecutionContext
) extends Observer[ContainerResponse](c) {
  private val logging: Logger = Logger(this.getClass)

  private var now = (System.currentTimeMillis() / 1000).toInt

  protected val task: Runnable = new Runnable {
    def run(): Unit = {
      val response = dockerClient
        .logContainerCmd(_data.info.head.containerId)
        .withFollowStream(true)
        .withStdErr(true)
        .withStdOut(true)
        .withSince(now)
        .withTimestamps(true)

      try {
        response.exec(new LogContainerResultCallback() {
          override def onNext(item: Frame): Unit = {
            val log = LogContainer(_data.id, _data.name, new String(item.getPayload, StandardCharsets.UTF_8), System.currentTimeMillis())
            logging.debug(s"Log found! Container(${_data.id}, ${_data.name}), indexing it ...")
            logService.create(log) recover { case e: Exception =>
              logging.warn(s"Error indexing logs for container ${_data.name} -> Error: ${e.getMessage}")
            }
          }
        })
      } catch {
        case e: Exception => logging.warn(s"Error getting logs of container ${_data.info.head.containerId} - ${c.name}. Error: ${e.getMessage}")
      }

      now = (System.currentTimeMillis() / 1000).toInt
    }
  }
}
