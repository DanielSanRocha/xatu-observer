package com.danielsanrocha.xatu.repositories

import com.danielsanrocha.xatu.models.internals.{LogService}

import scala.concurrent.Future

trait LogRepository {
  def createIndex(): Future[Unit]
  def create(documentId: String, log: LogService): Future[Unit]
  def search(query: String): Future[Seq[LogService]]
}
