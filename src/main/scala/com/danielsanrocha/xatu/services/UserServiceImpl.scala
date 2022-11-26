package com.danielsanrocha.xatu.services

import com.twitter.util.logging.Logger
import scala.concurrent.Future 

import com.danielsanrocha.xatu.repositories.UserRepository
import com.danielsanrocha.xatu.models.internals.{User}
import com.danielsanrocha.xatu.models.responses.UserResponse

class UserServiceImpl(implicit repository: UserRepository, implicit val ec: scala.concurrent.ExecutionContext) extends UserService {
  private val logging: Logger = Logger(this.getClass)

  override def getById(id: Long): Future[Option[UserResponse]] = {
    repository.getById(id) map {
      case Some(user) => Some(UserResponse(user))
      case None       => None
    }
  }
}
