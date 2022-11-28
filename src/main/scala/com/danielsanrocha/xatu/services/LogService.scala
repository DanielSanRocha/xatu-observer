package com.danielsanrocha.xatu.services

import com.danielsanrocha.xatu.models.internals.{LogService => LogServiceModel}

import scala.concurrent.Future

trait LogService {
  def create(log: LogServiceModel): Future[Unit]
  def searchServiceLog(query: String): Future[Seq[LogServiceModel]]
}
