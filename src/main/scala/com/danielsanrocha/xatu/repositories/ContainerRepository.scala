package com.danielsanrocha.xatu.repositories

import scala.concurrent.Future

import com.danielsanrocha.xatu.models.internals.{Container, NewContainer}

trait ContainerRepository {
  def getById(id: Long): Future[Option[Container]]
  def create(service: NewContainer): Future[Long]
  def delete(id: Long): Future[Boolean]
  def getAll(limit: Long, offset: Long): Future[Seq[Container]]
}
