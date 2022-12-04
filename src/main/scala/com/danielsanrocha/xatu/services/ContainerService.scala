package com.danielsanrocha.xatu.services

import com.danielsanrocha.xatu.models.internals.{Container, NewContainer}
import com.danielsanrocha.xatu.models.responses.ContainerResponse

import scala.concurrent.Future

trait ContainerService extends Service[ContainerResponse] {
  def getById(id: Long): Future[Option[Container]]
  def create(container: NewContainer): Future[Long]
  def delete(id: Long): Future[Boolean]
  def getAll(limit: Long, offset: Long): Future[Seq[ContainerResponse]]
}
