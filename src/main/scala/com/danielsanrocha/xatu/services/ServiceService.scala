package com.danielsanrocha.xatu.services

import com.danielsanrocha.xatu.models.internals.{NewService, Service}

import scala.concurrent.Future

trait ServiceService {
  def getById(id: Long): Future[Option[Service]]
  def create(service: NewService): Future[Long]
  def delete(id: Long): Future[Boolean]
  def update(id: Long, s: NewService): Future[Boolean]
  def getAll(limit: Long, offset: Long): Future[Seq[Service]]
}
