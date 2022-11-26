package com.danielsanrocha.xatu.services

import scala.concurrent.Future

import com.danielsanrocha.xatu.repositories.UserRepository
import com.danielsanrocha.xatu.models.internals.{Service}

trait ServiceService {
  def getById(id: Long): Future[Option[Service]]
}
