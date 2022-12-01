package com.danielsanrocha.xatu.managers

import com.danielsanrocha.xatu.models.internals.Service
import com.danielsanrocha.xatu.observers.LogServiceObserver
import com.danielsanrocha.xatu.services.{LogService, ServiceService}
import com.twitter.util.logging.Logger

class LogServiceObserverManager(implicit val service: ServiceService, override implicit val ec: scala.concurrent.ExecutionContext, implicit val logService: LogService)
    extends Manager[Service, LogServiceObserver](service, ec) {
  private val logging: Logger = Logger(this.getClass)

  override protected def createObserver(data: Service): LogServiceObserver = {
    new LogServiceObserver(data, service, logService)
  }
}
