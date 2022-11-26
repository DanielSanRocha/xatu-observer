package com.danielsanrocha.xatu.repositories

import scala.concurrent.Future

import com.danielsanrocha.xatu.models.internals.{Service}

trait ServiceRepository {
  def getById(id: Long): Future[Option[Service]]
}
