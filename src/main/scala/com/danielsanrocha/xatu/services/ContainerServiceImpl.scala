package com.danielsanrocha.xatu.services

import com.twitter.util.logging.Logger

import java.util.{List => JavaList}
import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.language.postfixOps
import com.spotify.docker.client.DockerClient

import com.danielsanrocha.xatu.models.internals.{Container, ContainerInfo, NewContainer}
import com.danielsanrocha.xatu.models.responses.ContainerResponse
import com.danielsanrocha.xatu.repositories.ContainerRepository

class ContainerServiceImpl(implicit repository: ContainerRepository, implicit val dockerClient: DockerClient, implicit val ec: scala.concurrent.ExecutionContext) extends ContainerService {
  private val logging: Logger = Logger(this.getClass)

  override def getById(id: Long): Future[Option[Container]] = {
    repository.getById(id) map {
      case Some(c) => Some(c)
      case None    => None
    }
  }

  override def create(c: NewContainer): Future[Long] = {
    repository.create(c)
  }

  override def delete(id: Long): Future[Boolean] = {
    repository.delete(id)
  }

  override def getAll(limit: Long, offset: Long): Future[Seq[ContainerResponse]] = {
    repository.getAll(limit, offset) map { containers =>
      val containersActive = dockerClient.listContainers()
      val containersMap = (containersActive.asScala map { cont => (cont.names().get(0), cont) }).groupMap(_._1)(_._2)

      logging.info(s"All docker running containers: ${containersMap.keySet}")

      containers map { container =>
        val name = container.name

        val cont = containersMap.get(s"/$name")

        val status = cont match {
          case Some(_) => 'W'
          case None    => 'F'
        }

        val info = cont match {
          case Some(c) => Some(ContainerInfo(c.head.image(), c.head.id()))
          case None    => None
        }

        ContainerResponse(container.id, name, info, status, container.createDate, container.updateDate)
      }
    }
  }
}
