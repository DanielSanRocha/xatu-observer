package com.danielsanrocha.xatu.services

import scala.concurrent.Future
import io.jvm.uuid.UUID

import com.danielsanrocha.xatu.repositories.LogRepository
import com.danielsanrocha.xatu.models.internals.{LogService => LogServiceModel}

class LogServiceImpl(implicit val repository: LogRepository) extends LogService {
  override def create(log: LogServiceModel): Future[Unit] = {
    val uuid = UUID.random.toString
    val name = s"service-$uuid"

    repository.create(name, log)
  }

  override def searchServiceLog(query: String): Future[Seq[LogServiceModel]] = {
    repository.search(query)
  }
}
