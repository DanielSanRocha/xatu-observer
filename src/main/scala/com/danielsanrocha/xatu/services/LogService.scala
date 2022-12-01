package com.danielsanrocha.xatu.services

import com.danielsanrocha.xatu.models.internals.Log.Log
import com.danielsanrocha.xatu.models.internals.{LogContainer, LogService => LogServiceModel}

import scala.concurrent.Future

trait LogService {
  def create(log: LogServiceModel): Future[Unit]
  def create(log: LogContainer): Future[Unit]
  def search(query: String): Future[Seq[Log]]
}
