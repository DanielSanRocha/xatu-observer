package com.danielsanrocha.xatu.services

import com.twitter.util.logging.Logger
import scala.concurrent.Future

import com.danielsanrocha.xatu.repositories.ServiceRepository
import com.danielsanrocha.xatu.models.internals.{Service}

class ServiceServiceImpl(implicit repository: ServiceRepository, implicit val ec: scala.concurrent.ExecutionContext) extends ServiceService {
  private val logging: Logger = Logger(this.getClass)

  override def getById(id: Long): Future[Option[Service]] = {
    repository.getById(id) map {
      case Some(service) => Some(service)
      case None          => None 
    }
  }
}
