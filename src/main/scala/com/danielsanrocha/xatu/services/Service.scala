package com.danielsanrocha.xatu.services

import scala.concurrent.Future

trait Service[T] {
  def getAll(limit: Long, offset: Long): Future[Seq[T]]
}
