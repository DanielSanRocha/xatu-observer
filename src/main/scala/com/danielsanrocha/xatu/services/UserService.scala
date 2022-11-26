package com.danielsanrocha.xatu.services

import scala.concurrent.Future

import com.danielsanrocha.xatu.repositories.UserRepository
import com.danielsanrocha.xatu.models.internals.{User}
import com.danielsanrocha.xatu.models.responses.{UserResponse}

trait UserService {
  def getById(id: Long): Future[Option[UserResponse]]
}