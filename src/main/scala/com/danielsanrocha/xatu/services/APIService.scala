package com.danielsanrocha.xatu.services

import com.danielsanrocha.xatu.models.internals.{API, NewAPI}

import scala.concurrent.Future

trait APIService {
  def getById(id: Long): Future[Option[API]]
  def create(service: NewAPI): Future[Long]
  def delete(id: Long): Future[Boolean]
  def update(id: Long, s: NewAPI): Future[Boolean]
  def getAll(limit: Long, offset: Long): Future[Seq[API]]
  def setStatus(id: Long, status: Char): Future[Unit]
}
