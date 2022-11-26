package com.danielsanrocha.xatu.repositories

import com.twitter.util.Future

import com.danielsanrocha.xatu.models.internals.{User}

trait UserRepository {
  def getById(id: Long): Future[Option[User]]
  def getByEmail(email: String): Future[Option[User]]
}
